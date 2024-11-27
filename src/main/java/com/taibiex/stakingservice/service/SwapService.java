package com.taibiex.stakingservice.service;

import com.taibiex.stakingservice.entity.Swap;
import com.taibiex.stakingservice.repository.SwapRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SwapService {

    private final SwapRepository swapRepository;

    public SwapService(SwapRepository swapRepository) {
        this.swapRepository = swapRepository;
    }

    @Transactional
    public void save(Swap swap) {
        Swap s = swapRepository.findByTxHash(swap.getTxHash());
        if (s != null) {
            return;
        }
        swapRepository.save(swap);
    }

    public long countAllByRecipient(String sender){
        return swapRepository.countAllByRecipient(sender);
    }
}
