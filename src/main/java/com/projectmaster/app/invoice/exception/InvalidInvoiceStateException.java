package com.projectmaster.app.invoice.exception;

public class InvalidInvoiceStateException extends InvoiceException {
    
    public InvalidInvoiceStateException(String message) {
        super(message, "INVALID_INVOICE_STATE");
    }
    
    public InvalidInvoiceStateException(String invoiceNumber, String currentState, String attemptedAction) {
        super(String.format("Cannot %s invoice %s in state %s", attemptedAction, invoiceNumber, currentState), 
              "INVALID_INVOICE_STATE");
    }
}