package com.github.jenkaby.bikerental.agreement.application.service;

import com.github.jenkaby.bikerental.agreement.domain.exception.InvalidSignatureImageException;
import org.springframework.stereotype.Component;

import java.util.Base64;

@Component
class SignatureImageDecoder {

    private static final String PNG_DATA_URI_PREFIX = "data:image/png;base64,";

    byte[] decode(String signaturePngBase64) {
        String base64 = signaturePngBase64.startsWith(PNG_DATA_URI_PREFIX)
                ? signaturePngBase64.substring(PNG_DATA_URI_PREFIX.length())
                : signaturePngBase64;
        try {
            return Base64.getDecoder().decode(base64);
        } catch (IllegalArgumentException e) {
            throw new InvalidSignatureImageException();
        }
    }
}
