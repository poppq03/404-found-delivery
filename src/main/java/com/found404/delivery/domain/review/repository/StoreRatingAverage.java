package com.found404.delivery.domain.review.repository;

import java.util.UUID;

public interface StoreRatingAverage {

    UUID getStoreId();

    Double getAverageRating();
}