package com.taibiex.stakingservice.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;

import java.math.BigInteger;
import java.sql.Timestamp;

@Entity
@Table(name = "contract_offset", indexes = {
        @Index(name = "contract_address_idx", columnList = "contract_address", unique = true),
})
public class ContractOffset {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    protected long id;
    @Column(name = "contract_address", nullable = false, length = 100)
    private String contractAddress;
    @Column(name = "contract_name", nullable = false, length = 255)
    private String contractName;
    @Column(name = "block_offset", columnDefinition = "bigint", nullable = false)
    private BigInteger blockOffset;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Column(name = "recorded_at", nullable = false, columnDefinition = "timestamp DEFAULT CURRENT_TIMESTAMP")
    private Timestamp recordedAt;

    public ContractOffset() {
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getContractAddress() {
        return contractAddress;
    }

    public void setContractAddress(String contractAddress) {
        this.contractAddress = contractAddress;
    }

    public String getContractName() {
        return contractName;
    }

    public void setContractName(String contractName) {
        this.contractName = contractName;
    }

    public BigInteger getBlockOffset() {
        return blockOffset;
    }

    public void setBlockOffset(BigInteger blockOffset) {
        this.blockOffset = blockOffset;
    }

    public Timestamp getRecordedAt() {
        return recordedAt;
    }

    public void setRecordedAt(Timestamp recordedAt) {
        this.recordedAt = recordedAt;
    }


    public ContractOffset(String contractAddress) {
        this.contractAddress = contractAddress;
    }
}
