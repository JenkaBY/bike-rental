package com.github.jenkaby.bikerental.agreement.infrastructure.pdf;

import com.github.jenkaby.bikerental.agreement.domain.exception.AgreementPdfRenderingException;
import com.github.jenkaby.bikerental.agreement.domain.model.AgreementPdfData;
import com.github.jenkaby.bikerental.agreement.domain.model.AgreementTemplateVariable;
import com.github.jenkaby.bikerental.agreement.domain.service.AgreementPdfRenderer;
import com.github.jenkaby.bikerental.shared.application.service.MessageService;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.*;
import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Component
class PdfBoxAgreementRenderer implements AgreementPdfRenderer {

    private final AgreementPdfProperties properties;
    private final MessageService messageService;
    private final byte[] fontBytes;
    private final DateTimeFormatter dateTimeFormat;
    private final DateTimeFormatter titleDateFormat;
    private final ZoneId zoneId;

    PdfBoxAgreementRenderer(AgreementPdfProperties properties, MessageService messageService) {
        this.properties = properties;
        this.messageService = messageService;
        this.fontBytes = readFontBytes(properties.fontLocation());
        this.dateTimeFormat = DateTimeFormatter.ofPattern(properties.dateTimePattern());
        this.titleDateFormat = DateTimeFormatter.ofPattern(properties.datePattern());
        this.zoneId = ZoneId.of(properties.zoneId());
    }

    private static byte[] readFontBytes(String fontLocation) {
        try (InputStream fontStream = new ClassPathResource(fontLocation).getInputStream()) {
            return fontStream.readAllBytes();
        } catch (IOException ex) {
            throw new UncheckedIOException("Unable to read agreement font " + fontLocation, ex);
        }
    }

    @Override
    public byte[] render(AgreementPdfData data) {
        try (PDDocument document = new PDDocument();
             ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            PDType0Font font = PDType0Font.load(document, new ByteArrayInputStream(fontBytes), true);
            RenderCursor cursor = new RenderCursor(document, font, properties);
            ZonedDateTime startedAt = ZonedDateTime.ofInstant(data.rental().startedAt(), zoneId);

            writeTitle(cursor, font, data);
            cursor.blankLine(properties.titleFontSize());
            writeParagraphs(cursor, font, substitutePlaceholders(data.template().getContent(), data, startedAt));
            cursor.blankLine(properties.bodyFontSize());
            writeDataBlock(cursor, data, startedAt);
            cursor.blankLine(properties.bodyFontSize());
            writeSignature(cursor, document, font, data.signaturePng());
            cursor.blankLine(properties.bodyFontSize());
            writeMetadata(cursor, data, startedAt);

            cursor.close();
            document.save(output);
            return output.toByteArray();
        } catch (IOException ex) {
            throw new AgreementPdfRenderingException(ex);
        }
    }

    private void writeTitle(RenderCursor cursor, PDType0Font font, AgreementPdfData data) throws IOException {
        ZonedDateTime activatedAt = ZonedDateTime.ofInstant(data.template().getActivatedAt(), zoneId);
        String composedTitle = data.template().getTitle() + " " + label("agreement.pdf.label.title-from") + " " + activatedAt.format(titleDateFormat);
        for (String line : wrap(font, composedTitle, properties.titleFontSize())) {
            cursor.writeLineCentered(line, properties.titleFontSize());
        }
    }

    private String substitutePlaceholders(String content, AgreementPdfData data, ZonedDateTime startedAt) {
        String result = content;
        for (AgreementTemplateVariable variable : AgreementTemplateVariable.values()) {
            result = result.replace(variable.placeholder(), resolveVariable(variable, data, startedAt));
        }
        return result;
    }

    private String resolveVariable(AgreementTemplateVariable variable, AgreementPdfData data, ZonedDateTime startedAt) {
        AgreementPdfData.RentalData rental = data.rental();
        return switch (variable) {
            case CUSTOMER_FIRST_NAME -> data.customer().firstName();
            case CUSTOMER_LAST_NAME -> data.customer().lastName();
            case CUSTOMER_PHONE -> data.customer().phone();
            case RENTAL_STARTED_AT -> startedAt.format(dateTimeFormat);
            case RENTAL_DURATION -> formatDuration(rental.plannedDuration());
            case RENTAL_TOTAL -> rental.estimatedTotal() + " " + properties.currency();
            case RENTAL_NUMBER -> String.valueOf(rental.rentalId());
        };
    }

    private void writeParagraphs(RenderCursor cursor, PDType0Font font, String content) throws IOException {
        String[] paragraphs = content.split("\n", -1);
        for (String paragraph : paragraphs) {
            if (paragraph.isBlank()) {
                cursor.blankLine(properties.bodyFontSize());
                continue;
            }
            for (String line : wrap(font, paragraph, properties.bodyFontSize())) {
                cursor.writeLine(line, properties.bodyFontSize());
            }
        }
    }

    private List<String> wrap(PDType0Font font, String paragraph, float fontSize) throws IOException {
        float maxWidth = PDRectangle.A4.getWidth() - 2 * properties.margin();
        List<String> lines = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        for (String word : paragraph.split(" ")) {
            String candidate = current.isEmpty() ? word : current + " " + word;
            if (stringWidth(font, candidate, fontSize) <= maxWidth || current.isEmpty()) {
                current.setLength(0);
                current.append(candidate);
            } else {
                lines.add(current.toString());
                current.setLength(0);
                current.append(word);
            }
        }
        if (!current.isEmpty()) {
            lines.add(current.toString());
        }
        return lines;
    }

    private static float stringWidth(PDType0Font font, String text, float fontSize) throws IOException {
        return font.getStringWidth(text) / 1000f * fontSize;
    }

    private void writeDataBlock(RenderCursor cursor, AgreementPdfData data, ZonedDateTime startedAt) throws IOException {
        AgreementPdfData.CustomerData customer = data.customer();
        AgreementPdfData.RentalData rental = data.rental();
        float bodyFontSize = properties.bodyFontSize();
        String currency = properties.currency();
        cursor.writeLine(label("agreement.pdf.label.customer") + " " + customer.firstName() + " " + customer.lastName(), bodyFontSize);
        cursor.writeLine(label("agreement.pdf.label.phone") + " " + customer.phone(), bodyFontSize);
        cursor.writeLine(label("agreement.pdf.label.started-at") + " " + startedAt.format(dateTimeFormat), bodyFontSize);
        cursor.writeLine(label("agreement.pdf.label.duration") + " " + formatDuration(rental.plannedDuration())
                + " " + label("agreement.pdf.label.duration-unit"), bodyFontSize);
        cursor.writeLine(label("agreement.pdf.label.equipment"), bodyFontSize);
        int equipmentNumber = 1;
        for (AgreementPdfData.EquipmentLine line : rental.equipments()) {
            cursor.writeLine("  " + equipmentNumber + ". " + line.name()  + "(" + line.uid() + ")" + " — " + line.estimatedCost() + " " + currency, bodyFontSize);
            equipmentNumber++;
        }
        if (rental.discountPercent() != null) {
            cursor.writeLine(label("agreement.pdf.label.discount") + " " + rental.discountPercent() + "%", bodyFontSize);
        }
        if (rental.specialPrice() != null) {
            cursor.writeLine(label("agreement.pdf.label.special-price") + " " + rental.specialPrice() + " " + currency, bodyFontSize);
        }
        cursor.writeLine(label("agreement.pdf.label.total") + " " + rental.estimatedTotal() + " " + currency, bodyFontSize);
    }

    private String label(String code) {
        return messageService.getMessage(code);
    }

    private String formatDuration(Duration duration) {
        long hours = duration.toHours();
        long minutes = duration.toMinutesPart();
        return String.format("%02d:%02d", hours, minutes);
    }

    private void writeSignature(RenderCursor cursor, PDDocument document, PDType0Font font, byte[] signaturePng) throws IOException {
        cursor.writeLine(label("agreement.pdf.label.signature"), properties.bodyFontSize());
        if (signaturePng != null) {
            PDImageXObject image = PDImageXObject.createFromByteArray(document, signaturePng, "signature");
            cursor.drawImage(image, properties.signatureWidth(), properties.signatureHeight());
        } else {
            cursor.drawPlaceholder(font, properties.signatureWidth(), properties.signatureHeight(),
                    properties.signaturePlaceholderLabel());
        }
    }

    private void writeMetadata(RenderCursor cursor, AgreementPdfData data, ZonedDateTime startedAt) throws IOException {
        cursor.writeLine(label("agreement.pdf.label.signed-at") + " " + startedAt.format(dateTimeFormat)
                + "   " + label("agreement.pdf.label.rental-number") + " " + data.rental().rentalId()
                + "  " + label("agreement.pdf.label.template-sha") + " " + data.template().getContentSha256(), properties.smallFontSize());
    }

    private static final class RenderCursor {

        private final PDDocument document;
        private final PDType0Font font;
        private final AgreementPdfProperties properties;
        private PDPage page;
        private PDPageContentStream stream;
        private float y;

        private RenderCursor(PDDocument document, PDType0Font font, AgreementPdfProperties properties) throws IOException {
            this.document = document;
            this.font = font;
            this.properties = properties;
            newPage();
        }

        private void newPage() throws IOException {
            if (stream != null) {
                stream.close();
            }
            page = new PDPage(PDRectangle.A4);
            document.addPage(page);
            stream = new PDPageContentStream(document, page);
            y = page.getMediaBox().getHeight() - properties.margin();
        }

        private void ensureSpace(float needed) throws IOException {
            if (y - needed < properties.margin()) {
                newPage();
            }
        }

        private void writeLine(String text, float fontSize) throws IOException {
            float leading = fontSize * properties.leadingFactor();
            ensureSpace(leading);
            stream.beginText();
            stream.setFont(font, fontSize);
            stream.newLineAtOffset(properties.margin(), y);
            stream.showText(text);
            stream.endText();
            y -= leading;
        }

        private void writeLineCentered(String text, float fontSize) throws IOException {
            float leading = fontSize * properties.leadingFactor();
            ensureSpace(leading);
            float maxWidth = PDRectangle.A4.getWidth() - 2 * properties.margin();
            float textWidth = stringWidth(font, text, fontSize);
            float x = properties.margin() + Math.max(0f, (maxWidth - textWidth) / 2f);
            stream.beginText();
            stream.setFont(font, fontSize);
            stream.newLineAtOffset(x, y);
            stream.showText(text);
            stream.endText();
            y -= leading;
        }

        private void blankLine(float fontSize) throws IOException {
            float leading = fontSize * properties.leadingFactor();
            ensureSpace(leading);
            y -= leading;
        }

        private void drawImage(PDImageXObject image, float width, float height) throws IOException {
            ensureSpace(height);
            y -= height;
            stream.drawImage(image, properties.margin(), y, width, height);
        }

        private void drawPlaceholder(PDType0Font placeholderFont, float width, float height, String label) throws IOException {
            ensureSpace(height);
            y -= height;
            stream.addRect(properties.margin(), y, width, height);
            stream.stroke();
            stream.beginText();
            stream.setFont(placeholderFont, properties.bodyFontSize());
            stream.newLineAtOffset(properties.margin() + 10f, y + height / 2f);
            stream.showText(label);
            stream.endText();
        }

        private void close() throws IOException {
            if (stream != null) {
                stream.close();
                stream = null;
            }
        }
    }
}
