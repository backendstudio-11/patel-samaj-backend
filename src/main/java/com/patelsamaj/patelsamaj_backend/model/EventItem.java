package com.patelsamaj.patelsamaj_backend.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "event_items")
@Data
public class EventItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "event_item_id")
    private Integer eventItemId;

    @ManyToOne
    @JoinColumn(name = "event_id")
    @JsonIgnore
    private Event event;

    @ManyToOne
    @JoinColumn(name = "item_id")
    private Item item;

    private Integer quantity = 1;

    @Column(name = "price_at_booking", nullable = false)
    private Integer priceAtBooking;
}
