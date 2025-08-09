package com.projectmaster.app.invoice.exception;

import com.projectmaster.app.common.exception.ProjectMasterException;

public class InvoiceException extends ProjectMasterException {
    
    public InvoiceException(String message) {
        super(message, "INVOICE_ERROR");
    }
    
    public InvoiceException(String message, Throwable cause) {
        super(message, "INVOICE_ERROR", cause);
    }
    
    public InvoiceException(String message, String errorCode) {
        super(message, errorCode);
    }
    
    public InvoiceException(String message, String errorCode, Throwable cause) {
        super(message, errorCode, cause);
    }
}