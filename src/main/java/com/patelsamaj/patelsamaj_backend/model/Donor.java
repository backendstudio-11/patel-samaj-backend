package com.patelsamaj.patelsamaj_backend.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "donors")
@Data
public class Donor {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "donor_id")
    private Integer donorId;

    @Column(name = "full_name", nullable = false, length = 150)
    private String fullName;

    @Column(name = "amount", nullable = false)
    private Integer amount;

    @Column(name = "donation_date", nullable = false)
    private LocalDateTime donationDate = LocalDateTime.now();

    @Column(name = "created_by")
    private Integer createdBy;

    @Column(name = "remarks", columnDefinition = "TEXT")
    private String remarks;
}
