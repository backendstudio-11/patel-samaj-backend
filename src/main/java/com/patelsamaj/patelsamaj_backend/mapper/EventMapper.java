package com.patelsamaj.patelsamaj_backend.mapper;

import com.patelsamaj.patelsamaj_backend.dto.EventDTO;
import com.patelsamaj.patelsamaj_backend.dto.ItemSelectionDTO;
import com.patelsamaj.patelsamaj_backend.model.Event;
import com.patelsamaj.patelsamaj_backend.model.EventItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;


@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface EventMapper extends BaseMapper<EventDTO, Event> {
    @Mapping(source = "item.itemId", target = "itemId")
    @Mapping(source = "item.itemName", target = "itemName")
    ItemSelectionDTO eventItemToItemSelectionDTO(EventItem eventItem);

    @Override
    @Mapping(source = "bookedItems", target = "items")
    EventDTO toDto(Event entity);
}
