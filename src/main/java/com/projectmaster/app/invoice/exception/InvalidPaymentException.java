package com.projectmaster.app.invoice.exception;

public class InvalidPaymentException extends InvoiceException {
    
    public InvalidPaymentException(String message) {
        super(message, "INVALID_PAYMENT");
    }
    
    public InvalidPaymentException(String message, Throwable cause) {
        super(message, "INVALID_PAYMENT", cause);
    }
}