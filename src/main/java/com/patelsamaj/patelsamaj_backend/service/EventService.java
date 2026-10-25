package com.patelsamaj.patelsamaj_backend.service;

import com.patelsamaj.patelsamaj_backend.dto.EventDTO;
import com.patelsamaj.patelsamaj_backend.dto.PaginationDTO;
import org.springframework.data.domain.Page;
import java.util.Map;

public interface EventService {

    EventDTO getEventById(int eventId);

    Page<EventDTO> getEventsByStatusId(int statusId, PaginationDTO request);

    EventDTO upsertEvent(EventDTO eventDTO, int userId);

    EventDTO closeEvent(int eventId);

    byte[] generateBillPdfForEvent(EventDTO eventDTO);

    Map<String, Integer> getCalendarEventCounts();
}
