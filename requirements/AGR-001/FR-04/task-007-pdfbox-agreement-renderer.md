<task_file_template>

# Task 007: Implement `PdfBoxAgreementRenderer` (infrastructure)

> **Applied Skill:** `spring-boot-modulith` (infrastructure implements a domain port) · `java-best-practices` /
> `.claude/rules/java-style.md` (constructor-free `@Component`, `final` fields where used, zero inline comments).
> The renderer is pure — it never reads a `Clock`; all timestamps arrive inside `AgreementPdfData`.

## 1. Objective

Implement the `AgreementPdfRenderer` port with Apache PDFBox 3.x: embed the DejaVu Sans TTF (Cyrillic-capable), lay out
the title, word-wrapped Cyrillic body text with automatic pagination, a data block (customer, phone, start date/time,
duration), an equipment table with a computed total, a signature image or a placeholder rectangle, and a metadata
footer. Any `IOException` is wrapped into `AgreementPdfRenderingException`. Depends on Task 003 (data), Task 004
(port), Task 005 (exception), Task 002 (PDFBox dependency).

## 2. File to Modify / Create

* **File Path:** `service/src/main/java/com/github/jenkaby/bikerental/agreement/infrastructure/pdf/PdfBoxAgreementRenderer.java`
* **Action:** Create New File

## 3. Code Implementation

Create the file with EXACTLY this content:

```java
package com.github.jenkaby.bikerental.agreement.infrastructure.pdf;

import com.github.jenkaby.bikerental.agreement.domain.exception.AgreementPdfRenderingException;
import com.github.jenkaby.bikerental.agreement.domain.model.AgreementPdfData;
import com.github.jenkaby.bikerental.agreement.domain.service.AgreementPdfRenderer;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Component
class PdfBoxAgreementRenderer implements AgreementPdfRenderer {

    private static final String FONT_LOCATION = "fonts/DejaVuSans.ttf";
    private static final float MARGIN = 50f;
    private static final float BODY_FONT_SIZE = 11f;
    private static final float TITLE_FONT_SIZE = 16f;
    private static final float LEADING_FACTOR = 1.4f;
    private static final float SIGNATURE_WIDTH = 180f;
    private static final float SIGNATURE_HEIGHT = 80f;
    private static final String SIGNATURE_PLACEHOLDER_LABEL = "место подписи";
    private static final DateTimeFormatter DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    @Override
    public byte[] render(AgreementPdfData data) {
        try (PDDocument document = new PDDocument();
             ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            PDType0Font font = loadFont(document);
            RenderCursor cursor = new RenderCursor(document, font);

            cursor.writeLine(data.title(), TITLE_FONT_SIZE);
            cursor.blankLine(TITLE_FONT_SIZE);
            writeParagraphs(cursor, font, data.content());
            cursor.blankLine(BODY_FONT_SIZE);
            writeDataBlock(cursor, data);
            cursor.blankLine(BODY_FONT_SIZE);
            writeSignature(cursor, document, font, data.signaturePng());
            cursor.blankLine(BODY_FONT_SIZE);
            writeMetadata(cursor, data);

            cursor.close();
            document.save(output);
            return output.toByteArray();
        } catch (IOException ex) {
            throw new AgreementPdfRenderingException(ex);
        }
    }

    private PDType0Font loadFont(PDDocument document) throws IOException {
        try (InputStream fontStream = new ClassPathResource(FONT_LOCATION).getInputStream()) {
            return PDType0Font.load(document, fontStream, true);
        }
    }

    private void writeParagraphs(RenderCursor cursor, PDType0Font font, String content) throws IOException {
        String[] paragraphs = content.split("\n", -1);
        for (String paragraph : paragraphs) {
            if (paragraph.isBlank()) {
                cursor.blankLine(BODY_FONT_SIZE);
                continue;
            }
            for (String line : wrap(font, paragraph)) {
                cursor.writeLine(line, BODY_FONT_SIZE);
            }
        }
    }

    private List<String> wrap(PDType0Font font, String paragraph) throws IOException {
        float maxWidth = PDRectangle.A4.getWidth() - 2 * MARGIN;
        List<String> lines = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        for (String word : paragraph.split(" ")) {
            String candidate = current.isEmpty() ? word : current + " " + word;
            if (stringWidth(font, candidate) <= maxWidth || current.isEmpty()) {
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

    private float stringWidth(PDType0Font font, String text) throws IOException {
        return font.getStringWidth(text) / 1000f * BODY_FONT_SIZE;
    }

    private void writeDataBlock(RenderCursor cursor, AgreementPdfData data) throws IOException {
        AgreementPdfData.CustomerData customer = data.customer();
        AgreementPdfData.RentalData rental = data.rental();
        cursor.writeLine("Клиент: " + customer.firstName() + " " + customer.lastName(), BODY_FONT_SIZE);
        cursor.writeLine("Телефон: " + customer.phone(), BODY_FONT_SIZE);
        cursor.writeLine("Дата начала: " + rental.startedAt().format(DATE_TIME_FORMAT), BODY_FONT_SIZE);
        cursor.writeLine("Длительность: " + formatDuration(rental.plannedDuration()), BODY_FONT_SIZE);
        cursor.writeLine("Оборудование:", BODY_FONT_SIZE);
        BigDecimal total = BigDecimal.ZERO;
        for (AgreementPdfData.EquipmentLine line : rental.equipments()) {
            cursor.writeLine("  " + line.uid() + " — " + line.name() + " — " + line.estimatedCost(), BODY_FONT_SIZE);
            total = total.add(line.estimatedCost());
        }
        cursor.writeLine("Итого: " + total, BODY_FONT_SIZE);
    }

    private String formatDuration(Duration duration) {
        long hours = duration.toHours();
        long minutes = duration.toMinutesPart();
        return String.format("%02d:%02d", hours, minutes);
    }

    private void writeSignature(RenderCursor cursor, PDDocument document, PDType0Font font, byte[] signaturePng) throws IOException {
        cursor.writeLine("Подпись:", BODY_FONT_SIZE);
        if (signaturePng != null) {
            PDImageXObject image = PDImageXObject.createFromByteArray(document, signaturePng, "signature");
            cursor.drawImage(image, SIGNATURE_WIDTH, SIGNATURE_HEIGHT);
        } else {
            cursor.drawPlaceholder(font, SIGNATURE_WIDTH, SIGNATURE_HEIGHT, SIGNATURE_PLACEHOLDER_LABEL);
        }
    }

    private void writeMetadata(RenderCursor cursor, AgreementPdfData data) throws IOException {
        cursor.writeLine("Дата подписания: " + data.rental().startedAt().format(DATE_TIME_FORMAT)
                + "   Аренда №: " + data.rental().rentalId(), BODY_FONT_SIZE);
    }

    private static final class RenderCursor {

        private final PDDocument document;
        private final PDType0Font font;
        private PDPage page;
        private PDPageContentStream stream;
        private float y;

        private RenderCursor(PDDocument document, PDType0Font font) throws IOException {
            this.document = document;
            this.font = font;
            newPage();
        }

        private void newPage() throws IOException {
            if (stream != null) {
                stream.close();
            }
            page = new PDPage(PDRectangle.A4);
            document.addPage(page);
            stream = new PDPageContentStream(document, page);
            y = page.getMediaBox().getHeight() - MARGIN;
        }

        private void ensureSpace(float needed) throws IOException {
            if (y - needed < MARGIN) {
                newPage();
            }
        }

        private void writeLine(String text, float fontSize) throws IOException {
            float leading = fontSize * LEADING_FACTOR;
            ensureSpace(leading);
            stream.beginText();
            stream.setFont(font, fontSize);
            stream.newLineAtOffset(MARGIN, y);
            stream.showText(text);
            stream.endText();
            y -= leading;
        }

        private void blankLine(float fontSize) throws IOException {
            float leading = fontSize * LEADING_FACTOR;
            ensureSpace(leading);
            y -= leading;
        }

        private void drawImage(PDImageXObject image, float width, float height) throws IOException {
            ensureSpace(height);
            y -= height;
            stream.drawImage(image, MARGIN, y, width, height);
        }

        private void drawPlaceholder(PDType0Font placeholderFont, float width, float height, String label) throws IOException {
            ensureSpace(height);
            y -= height;
            stream.addRect(MARGIN, y, width, height);
            stream.stroke();
            stream.beginText();
            stream.setFont(placeholderFont, BODY_FONT_SIZE);
            stream.newLineAtOffset(MARGIN + 10f, y + height / 2f);
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
```

> Constraints that MUST hold:
> - PDFBox 3.x API only: `new PDDocument()`, `new PDPage(PDRectangle.A4)`, `new PDPageContentStream(document, page)`,
>   `PDType0Font.load(document, inputStream, true)`, `PDImageXObject.createFromByteArray(document, bytes, "signature")`.
> - The renderer NEVER injects or reads a `Clock` — timestamps come only from `AgreementPdfData`.
> - The font is loaded from classpath `fonts/DejaVuSans.ttf`; this file already exists — do NOT create it.
> - The class is package-private `@Component` with no explicit constructor (no dependencies to inject).
> - Zero inline comments.

## 4. Validation Steps

Execute the following command. Do NOT run the full application server.

```bash
./gradlew :service:compileJava "-Dspring.profiles.active=test"
```

</task_file_template>
