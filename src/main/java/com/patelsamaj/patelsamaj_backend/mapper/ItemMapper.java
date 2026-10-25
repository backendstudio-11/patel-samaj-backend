package com.patelsamaj.patelsamaj_backend.mapper;

import com.patelsamaj.patelsamaj_backend.dto.ItemDTO;
import com.patelsamaj.patelsamaj_backend.model.Item;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ItemMapper extends BaseMapper<ItemDTO, Item> {
}
