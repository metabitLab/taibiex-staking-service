package com.taibiex.stakingservice.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "activity_user", indexes = {
        @Index(name = "user_idx", columnList = "user_address", unique = true),
})
@Data
public class ActivityUser extends BaseEntity{

    private String userAddress;

}
