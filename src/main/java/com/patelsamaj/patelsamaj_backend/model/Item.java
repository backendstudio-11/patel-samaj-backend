package com.patelsamaj.patelsamaj_backend.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "items")
@Data
public class Item {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "item_id")
    private Integer itemId;

    @Column(name = "item_name", nullable = false, unique = true)
    private String itemName;

    @Column(name = "current_price", nullable = false)
    private Integer currentPrice;

    private String description;

    @Column(name = "created_by")
    private Integer createdBy;

    @Column(name="is_deleted")
    private Boolean isDeleted;
}
