package com.taibiex.stakingservice.service;

import com.taibiex.stakingservice.entity.ContractOffset;
import com.taibiex.stakingservice.repository.ContractOffsetRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigInteger;

@Slf4j
@Service
public class ContractOffsetService {

    private final ContractOffsetRepository contractOffsetRepository;

    public ContractOffsetService(ContractOffsetRepository contractOffsetRepository) {
        this.contractOffsetRepository = contractOffsetRepository;
    }

    public ContractOffset findByContractAddress(String contractAddress){
        return contractOffsetRepository.findByContractAddress(contractAddress);
    }

    @Transactional
    public void update(ContractOffset contractOffset){
        contractOffsetRepository.save(contractOffset);
    }

    public BigInteger findMinBlockOffset(){
        return contractOffsetRepository.findMinBlockOffset();
    }
}
