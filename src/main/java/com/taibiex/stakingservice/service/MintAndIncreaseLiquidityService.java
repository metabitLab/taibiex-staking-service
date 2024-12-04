package com.taibiex.stakingservice.service;

import com.taibiex.stakingservice.common.chain.contract.utils.CustomBeanUtils;
import com.taibiex.stakingservice.entity.MintAndIncreaseLiquidity;
import com.taibiex.stakingservice.repository.MintAndIncreaseLiquidityRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class MintAndIncreaseLiquidityService {

    private final MintAndIncreaseLiquidityRepository mintAndIncreaseLiquidityRepository;

    public MintAndIncreaseLiquidityService(MintAndIncreaseLiquidityRepository mintAndIncreaseLiquidityRepository) {
        this.mintAndIncreaseLiquidityRepository = mintAndIncreaseLiquidityRepository;
    }

    @Transactional
    public void save(MintAndIncreaseLiquidity mintAndIncreaseLiquidity) {
        //注意：这里数据库存的交易hash中不能和logIndex一起存，因为情况2时这里有可能Pool和NonfungiblePositionManager添加流动性事件共同维护此记录(记录才能完全)
        MintAndIncreaseLiquidity m = mintAndIncreaseLiquidityRepository.findByTxHash(mintAndIncreaseLiquidity.getTxHash());
        if (m != null) {


            //for update by id
            mintAndIncreaseLiquidity.setId(m.getId());

            //copyProperties(Object source, Object target)
            CustomBeanUtils.copyNonNullAndNonEmptyProperties(mintAndIncreaseLiquidity, m);

            mintAndIncreaseLiquidityRepository.save(m);
            return;

//            if(StringUtils.isEmpty(mintAndIncreaseLiquidity.getTokenId()) ||
//                    StringUtils.isBlank(mintAndIncreaseLiquidity.getTokenId()))
//            {
//                if(StringUtils.isNotEmpty(m.getTokenId()) && StringUtils.isNotBlank(m.getTokenId()))
//                {
//                    mintAndIncreaseLiquidity.setTokenId(m.getTokenId());
//                }
//            }

            //log.info("MintAndIncreaseLiquidity save ignore:  record existed! txHash: {}", mintAndIncreaseLiquidity.getTxHash());
            //return;
        }

        mintAndIncreaseLiquidityRepository.save(mintAndIncreaseLiquidity);
    }

    public long countBySender(String sender) {
        return mintAndIncreaseLiquidityRepository.countAllBySender(sender);
    }
}
