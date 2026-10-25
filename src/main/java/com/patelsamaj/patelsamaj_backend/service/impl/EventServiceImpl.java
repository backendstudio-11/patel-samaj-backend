package com.patelsamaj.patelsamaj_backend.service.impl;

import com.patelsamaj.patelsamaj_backend.dto.EventDTO;
import com.patelsamaj.patelsamaj_backend.dto.ItemSelectionDTO;
import com.patelsamaj.patelsamaj_backend.dto.PaginationDTO;
import com.patelsamaj.patelsamaj_backend.exception.ResourceNotFoundException;
import com.patelsamaj.patelsamaj_backend.mapper.EventMapper;
import com.patelsamaj.patelsamaj_backend.model.*;
import com.patelsamaj.patelsamaj_backend.repository.*;
import com.patelsamaj.patelsamaj_backend.service.EmailService;
import com.patelsamaj.patelsamaj_backend.service.EventService;
import com.patelsamaj.patelsamaj_backend.service.PdfGenerationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;

@Service
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;
    private final ItemRepository itemRepository;
    private final EventMapper eventMapper;
    private final EmailService emailService;
    private final PdfGenerationService pdfGenerationService;

    @Override
    public EventDTO getEventById(int eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Bill not found"));
        return eventMapper.toDto(event);
    }

    @Override
    public Page<EventDTO> getEventsByStatusId(int statusId, PaginationDTO request){
        Pageable pageable = PageRequest.of(
                request.getPage(),
                request.getSize(),
                Sort.by(request.getDirection(), request.getSortBy())
        );

        Page<Event> eventPage = eventRepository.findByStatusIdAndSearch(statusId, request.getSearch(), pageable);

        return eventPage.map(eventMapper::toDto);
    }

    @Override
    @Transactional
    public EventDTO upsertEvent(EventDTO dto, int userId) {
        Event event;
        if(dto.getEventId() != null && dto.getEventId() != 0){
            event = eventRepository.findById(dto.getEventId()).orElseThrow(() -> new ResourceNotFoundException("Bill not found"));
            if (Boolean.TRUE.equals(event.getIsClosed())) {
                throw new IllegalStateException("Bill is finalized and locked.");
            }

            if (event.getBookedItems() != null) {
                event.getBookedItems().clear();
            }
        }
        else{
            event = new Event();
            event.setIsClosed(false);
            event.setStatusId(1);
            event.setCreatedBy(userId);
        }

        eventMapper.updateEntityFromDto(dto, event);

        int grandTotal = 0;

        for (ItemSelectionDTO selection : dto.getItems()) {
            Item masterItem = itemRepository.findById(selection.getItemId())
                    .orElseThrow(() -> new ResourceNotFoundException("Item " + selection.getItemId() + " not found"));

            EventItem snapshotItem = new EventItem();
            snapshotItem.setEvent(event);
            snapshotItem.setItem(masterItem);
            snapshotItem.setQuantity(selection.getQuantity());
            snapshotItem.setPriceAtBooking(masterItem.getCurrentPrice());

            int lineTotal = snapshotItem.getPriceAtBooking() * selection.getQuantity();
            grandTotal += lineTotal;

            event.getBookedItems().add(snapshotItem);
        }

        event.setTotalAmount(grandTotal);
        eventRepository.save(event);
        return eventMapper.toDto(event);
    }

    @Override
    @Transactional
    public EventDTO closeEvent(int eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Bill not found"));

        if (Boolean.TRUE.equals(event.getIsClosed())) {
            throw new IllegalStateException("This bill is locked and cannot be edited.");
        }

        event.setIsClosed(true);
        event.setStatusId(4); // Set to 'Closed'
        eventRepository.save(event);

        return eventMapper.toDto(event);
    }

    @Override
    public byte[] generateBillPdfForEvent(EventDTO eventDTO) {
        // Fetch all active catalog items sorted by itemId
        List<Item> allMasterItems = itemRepository.findByIsDeletedFalse(Sort.by(Sort.Direction.ASC, "itemId"));

        Map<Integer, ItemSelectionDTO> bookedMap = new HashMap<>();
        if (eventDTO.getItems() != null) {
            for (ItemSelectionDTO item : eventDTO.getItems()) {
                if (item.getItemId() != null) {
                    bookedMap.put(item.getItemId(), item);
                }
            }
        }

        List<ItemSelectionDTO> completeItemList = new ArrayList<>();
        for (Item masterItem : allMasterItems) {
            if (bookedMap.containsKey(masterItem.getItemId())) {
                completeItemList.add(bookedMap.get(masterItem.getItemId()));
            } else {
                ItemSelectionDTO unbookedItem = new ItemSelectionDTO();
                unbookedItem.setItemId(masterItem.getItemId());
                unbookedItem.setItemName(masterItem.getItemName());
                unbookedItem.setPriceAtBooking(masterItem.getCurrentPrice());
                unbookedItem.setQuantity(0);
                completeItemList.add(unbookedItem);
            }
        }
        eventDTO.setItems(completeItemList);

        byte[] pdfBytes = pdfGenerationService.generateBillPdf(eventDTO);

        emailService.sendBillReceipt(eventDTO, pdfBytes);
        return pdfBytes;
    }

    @Override
    public Map<String, Integer> getCalendarEventCounts() {
        LocalDate today = LocalDate.now();
        List<Event> events = eventRepository.findFutureEventsByStatusIds();
        Map<String, Integer> countsMap = new TreeMap<>();

        for (Event event : events) {
            LocalDate start = event.getStartDate().toLocalDate();
            LocalDate end = event.getEndDate() != null ? event.getEndDate().toLocalDate() : start;

            if (end.isBefore(start)) {
                end = start;
            }

            LocalDate effectiveStart = start.isBefore(today) ? today : start;

            for (LocalDate d = effectiveStart; !d.isAfter(end); d = d.plusDays(1)) {
                String dateKey = d.toString();
                countsMap.put(dateKey, countsMap.getOrDefault(dateKey, 0) + 1);
            }
        }

        return countsMap;
    }
}