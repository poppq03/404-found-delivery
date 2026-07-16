package com.found404.delivery.domain.category.entity;

import com.found404.delivery.domain.store.entity.Store;
import com.found404.delivery.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UuidGenerator;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Table(name = "p_category")
@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Category extends BaseEntity {

    @Id
    @UuidGenerator
    @Column(name = "category_id", nullable = false, updatable = false)
    private UUID categoryId;

    @Column(name = "name", nullable = false)
    private String name;

    @Column
    private String description;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    @OneToMany(mappedBy = "category")
    private List<Store> stores = new ArrayList<>();

    private Category(String name, String description) {
        this.name = name;
        this.description = description;
        this.isActive = true;
    }

    public static Category create(String name, String description) {
        return new Category(name, description);
    }

    public void update(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public void delete(Long userId) {
        this.isActive = false;
        markDeleted(userId);
    }
}