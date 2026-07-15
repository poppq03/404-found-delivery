package com.found404.delivery.domain.store.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Table( name = "p_category" )
@Getter
@Entity
@NoArgsConstructor
public class Category {

    @Id
    private UUID categoryId;

    private String name;


    @OneToMany(mappedBy = "category")
    private List<Store> stores = new ArrayList<>();

}