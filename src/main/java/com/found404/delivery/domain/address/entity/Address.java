package com.found404.delivery.domain.address.entity;

import com.found404.delivery.domain.address.dto.AddressRequestDto;
import com.found404.delivery.domain.address.dto.AddressUpdateRequestDto;
import com.found404.delivery.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.annotations.UuidGenerator;

import java.util.UUID;

@Entity
@Table(
        name = "p_address",
        indexes = @Index(name = "idx_p_address_user_id", columnList = "user_id")
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SQLRestriction("deleted_at IS NULL")
public class Address extends BaseEntity {

    @Id
    @UuidGenerator
    @Column(name = "address_id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "address_name", length = 50)
    private String addressName;

    @Column(nullable = false, length = 255)
    private String address;

    @Column(name = "detail_address", length = 255)
    private String detailAddress;

    @Column(name = "receiver_name", nullable = false, length = 50)
    private String receiverName;

    @Column(nullable = false, length = 20)
    private String phone;

    @Column(name = "is_default", nullable = false)
    private boolean isDefault = false;

    public static Address create(Long userId, AddressRequestDto request) {
        Address address = new Address();
        address.userId = userId;
        address.addressName = request.getAddressName();
        address.address = request.getAddress();
        address.detailAddress = request.getDetailAddress();
        address.receiverName = request.getReceiverName();
        address.phone = request.getPhone();
        return address;
    }

    public void setDefaultAddress() {
        this.isDefault = true;
    }

    public void unsetDefaultAddress() {
        this.isDefault = false;
    }
    }
}
