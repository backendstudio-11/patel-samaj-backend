package com.patelsamaj.patelsamaj_backend.service;

import com.patelsamaj.patelsamaj_backend.dto.EventDTO;

public interface EmailService {
    void sendBillReceipt(EventDTO event, byte[] pdfBytes);
    void sendResetPasswordEmail(String toEmail, String plainTextPassword, String userName);
    void sendWelcomeEmail(String toEmail, String userName, String mobileNumber, String plainTextPassword);
}
