package com.patelsamaj.patelsamaj_backend.service;

import com.patelsamaj.patelsamaj_backend.dto.EventDTO;

public interface PdfGenerationService {
    byte[] generateBillPdf(EventDTO event);
}
