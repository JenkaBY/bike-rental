package com.github.jenkaby.bikerental.agreement.application.usecase;

public interface GetSignaturePdfUseCase {

    byte[] execute(Long rentalId);
}
