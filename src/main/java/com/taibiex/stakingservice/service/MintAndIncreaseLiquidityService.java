package com.taibiex.stakingservice.service;

import com.taibiex.stakingservice.entity.MintAndIncreaseLiquidity;
import com.taibiex.stakingservice.repository.MintAndIncreaseLiquidityRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MintAndIncreaseLiquidityService {

    private final MintAndIncreaseLiquidityRepository mintAndIncreaseLiquidityRepository;

    public MintAndIncreaseLiquidityService(MintAndIncreaseLiquidityRepository mintAndIncreaseLiquidityRepository) {
        this.mintAndIncreaseLiquidityRepository = mintAndIncreaseLiquidityRepository;
    }

    @Transactional
    public void save(MintAndIncreaseLiquidity mintAndIncreaseLiquidity) {
        MintAndIncreaseLiquidity m = mintAndIncreaseLiquidityRepository.findByTxHash(mintAndIncreaseLiquidity.getTxHash());
        if (m != null) {
            return;
        }
        mintAndIncreaseLiquidityRepository.save(mintAndIncreaseLiquidity);
    }

    public long countBySender(String sender) {
        return mintAndIncreaseLiquidityRepository.countAllBySender(sender);
    }
}
