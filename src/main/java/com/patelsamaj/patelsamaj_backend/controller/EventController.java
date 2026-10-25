package com.patelsamaj.patelsamaj_backend.controller;

import com.patelsamaj.patelsamaj_backend.dto.EventDTO;
import com.patelsamaj.patelsamaj_backend.dto.PaginationDTO;
import com.patelsamaj.patelsamaj_backend.helper.SecurityUtils;
import com.patelsamaj.patelsamaj_backend.service.EventService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
public class EventController {
    private final EventService eventService;

    @PostMapping("/list")
    public ResponseEntity<Page<EventDTO>> getEvents(
            @RequestParam Integer statusId,
            @RequestBody PaginationDTO request) {

        request.setSortBy("createdAt");

        return ResponseEntity.ok(
            eventService.getEventsByStatusId(statusId, request)
        );
    }

    @GetMapping("/{eventId}")
    public ResponseEntity<EventDTO> getEventById(@PathVariable int eventId){
        return ResponseEntity.ok(eventService.getEventById(eventId));
    }

    @PostMapping
    public ResponseEntity<EventDTO> createEvent(@Valid @RequestBody EventDTO eventDTO) {
        int adminId = SecurityUtils.getAuthenticatedUserId();
        return ResponseEntity.ok(eventService.upsertEvent(eventDTO, adminId));
    }

    @PutMapping
    public ResponseEntity<EventDTO> updateEvent(@Valid @RequestBody EventDTO eventDTO) {
        int adminId = SecurityUtils.getAuthenticatedUserId();
        return ResponseEntity.ok(eventService.upsertEvent(eventDTO, adminId));
    }

    @PutMapping("/{eventId}/close")
    public ResponseEntity<EventDTO> closeBill(@PathVariable Integer eventId) {
        return ResponseEntity.ok(eventService.closeEvent(eventId));
    }

    @GetMapping("/{eventId}/pdf")
    public ResponseEntity<byte[]> generateBillPdf(@PathVariable Integer eventId) {
        EventDTO event = eventService.getEventById(eventId);
        byte[] pdfBytes = eventService.generateBillPdfForEvent(event);

        String fileName = (event.getFamilyHeadName() != null ? event.getFamilyHeadName() : "bill")
                + "_" + (event.getEventType() != null ? event.getEventType() : "event") + ".pdf";

        return ResponseEntity.ok()
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename(fileName, StandardCharsets.UTF_8).build().toString()
                )
                .contentType(MediaType.APPLICATION_PDF)
                .contentLength(pdfBytes.length)
                .body(pdfBytes);
    }

    @GetMapping("/calendar-counts")
    public ResponseEntity<java.util.Map<String, Integer>> getCalendarEventCounts() {
        return ResponseEntity.ok(eventService.getCalendarEventCounts());
    }
}
