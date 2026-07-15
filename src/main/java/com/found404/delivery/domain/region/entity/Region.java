package com.found404.delivery.domain.region.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

@Table( name = "p_region" )
@Getter
@Entity
@NoArgsConstructor
public class Region {

    @Id
    @UuidGenerator
    @GeneratedValue( strategy = GenerationType.IDENTITY)
    private UUID regionId;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "is_active")
    private Boolean isActive;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "created_by")
    private Integer createdBy;

    @Column(name = "updated_at")
    private  LocalDateTime updatedAt;

    @Column(name = "updated_by")
    private Integer updatedBy;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "deleted_by")
    private Integer deletedBy;





}
