package com.github.jenkaby.bikerental.finance.application.service;

import com.github.f4b6a3.uuid.UuidCreator;
import com.github.jenkaby.bikerental.finance.application.usecase.RecordPaymentUseCase;
import com.github.jenkaby.bikerental.finance.domain.event.PaymentReceived;
import com.github.jenkaby.bikerental.finance.domain.model.Payment;
import com.github.jenkaby.bikerental.finance.domain.repository.PaymentRepository;
import com.github.jenkaby.bikerental.shared.domain.model.vo.Money;
import com.github.jenkaby.bikerental.shared.infrastructure.messaging.MessagePublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class RecordPaymentService implements RecordPaymentUseCase {

    public static final String PAYMENT_EXCHANGER = "payment-exchanger";

    private final PaymentRepository repository;
    private final ReceiptNumberGenerationService receiptService;
    private final MessagePublisher messagePublisher;
    private final Clock clock;

    @Override
    @Transactional
    public RecordPaymentResponse execute(RecordPaymentCommand command) {
        UUID id = UuidCreator.getTimeOrderedEpoch();

        String receipt = receiptService.generate();

        Instant now = clock.instant();

        Payment payment = Payment.builder()
                .id(id)
                .rentalId(command.rentalId())
                .amount(Money.of(command.amount()))
                .paymentType(command.paymentType())
                .paymentMethod(command.paymentMethod())
                .createdAt(now)
                .operatorId(command.operatorId())
                .receiptNumber(receipt)
                .build();

        repository.save(payment);


        messagePublisher.publish(PAYMENT_EXCHANGER, new PaymentReceived(id, command.rentalId(), Money.of(command.amount()), command.paymentType(), now));

        return new RecordPaymentResponse(id, receipt);
    }
}
