package com.patelsamaj.patelsamaj_backend.service.impl;

import com.patelsamaj.patelsamaj_backend.dto.ItemDTO;
import com.patelsamaj.patelsamaj_backend.exception.ResourceNotFoundException;
import com.patelsamaj.patelsamaj_backend.mapper.ItemMapper;
import com.patelsamaj.patelsamaj_backend.model.Item;
import com.patelsamaj.patelsamaj_backend.repository.ItemRepository;
import com.patelsamaj.patelsamaj_backend.service.ItemService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;
    private final ItemMapper itemMapper;

    @Override
    @Transactional
    public ItemDTO upsertItem(ItemDTO dto, int userId) {
        Item item;
        if (dto.getItemId() != null && dto.getItemId() != 0) {
            item = itemRepository.findById(dto.getItemId())
                    .orElseThrow(() -> new ResourceNotFoundException("Item not found"));
            itemMapper.updateEntityFromDto(dto, item);
        } else {
            item = itemMapper.toEntity(dto);
            item.setIsDeleted(false);
            item.setCreatedBy(userId);
        }

        Item savedItem = itemRepository.save(item);
        return itemMapper.toDto(savedItem);
    }

    @Override
    public List<ItemDTO> getListOfItems() {
        return itemMapper.toDtoList(itemRepository.findByIsDeletedFalse(Sort.by(Sort.Direction.ASC, "itemName")));
    }
    @Override
    public ItemDTO getItemById(int itemId) {
        return itemRepository.findById(itemId)
                .map(itemMapper::toDto)
                .orElseThrow(() -> new ResourceNotFoundException("Item not found with id: " + itemId));
    }

    @Override
    @Transactional
    public void deleteItemById(int itemId) {
        if (!itemRepository.existsById(itemId)) {
            throw new ResourceNotFoundException("Item not found");
        }
        itemRepository.softDeleteItem(itemId);
    }
}
