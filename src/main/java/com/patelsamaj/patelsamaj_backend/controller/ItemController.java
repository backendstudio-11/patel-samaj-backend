package com.patelsamaj.patelsamaj_backend.controller;

import com.patelsamaj.patelsamaj_backend.dto.ItemDTO;
import com.patelsamaj.patelsamaj_backend.helper.SecurityUtils;
import com.patelsamaj.patelsamaj_backend.service.ItemService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/items")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ItemController {

    private final ItemService itemService;

    @GetMapping
    public ResponseEntity<List<ItemDTO>> getAllItems(){
        return ResponseEntity.ok(itemService.getListOfItems());
    }

    @GetMapping("{itemId}")
    public ResponseEntity<ItemDTO> getItemById(@PathVariable int itemId){
        return ResponseEntity.ok(itemService.getItemById(itemId));
    }

    @PostMapping
    public ResponseEntity<ItemDTO> upsertItem(@Valid @RequestBody ItemDTO itemDTO){
        int adminId = SecurityUtils.getAuthenticatedUserId();
        return ResponseEntity.ok(itemService.upsertItem(itemDTO, adminId));
    }

    @DeleteMapping("/{itemId}")
    public ResponseEntity<String> softDeleteItem(@PathVariable int itemId) {
        itemService.deleteItemById(itemId);
        return ResponseEntity.ok("Item moved to trash successfully.");
    }

}
