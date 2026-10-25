package com.patelsamaj.patelsamaj_backend.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "events")
@Data
public class Event {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "event_id")
    private Integer eventId;

    @Column(name = "family_head_name", nullable = false)
    private String familyHeadName;

    @Column(name = "contact_number", nullable = false)
    private String contactNumber;

    private String email;

    @Column(name = "event_type")
    private String eventType;

    @Column(name = "start_date")
    private LocalDateTime startDate;

    @Column(name = "end_date")
    private LocalDateTime endDate;

    @Column(name = "status_id")
    private Integer statusId;

    @Column(name = "total_amount")
    private Integer totalAmount = 0;

    @Column(name = "is_closed")
    private Boolean isClosed = false;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "created_by")
    private Integer createdBy;

    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<EventItem> bookedItems = new ArrayList<>();
}
