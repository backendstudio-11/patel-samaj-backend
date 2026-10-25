package com.patelsamaj.patelsamaj_backend.service;

import com.patelsamaj.patelsamaj_backend.dto.ItemDTO;

import java.util.List;

public interface ItemService {
    ItemDTO getItemById(int itemId);
    List<ItemDTO> getListOfItems();
    ItemDTO upsertItem(ItemDTO itemDTO, int userId);
    void deleteItemById(int itemId);
}
