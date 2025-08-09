package com.projectmaster.app.invoice.exception;

import com.projectmaster.app.common.exception.EntityNotFoundException;

public class InvoiceNotFoundException extends EntityNotFoundException {
    
    public InvoiceNotFoundException(Object id) {
        super("Invoice", id);
    }
    
    public InvoiceNotFoundException(String message) {
        super(message);
    }
}