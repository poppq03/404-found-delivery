package com.found404.delivery.domain.region.entity;

import com.found404.delivery.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UuidGenerator;

import java.util.UUID;

@Table(name = "p_region")
@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Region extends BaseEntity {

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(name = "region_id", nullable = false, updatable = false)
    private UUID regionId;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    private Region(String name) {
        this.name = name;
        this.isActive = true;
    }

    public static Region createRegion(String name) {
        return new Region(name);
    }

    public void update(String name) {
        this.name = name;
    }

    public void delete(Long userId) {
        this.isActive = false;
        markDeleted(userId);
    }
}