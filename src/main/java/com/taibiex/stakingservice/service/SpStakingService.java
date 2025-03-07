package com.taibiex.stakingservice.service;


import com.taibiex.stakingservice.common.hibernate.Comment;
import com.taibiex.stakingservice.dto.SPStakingDTO;
import com.taibiex.stakingservice.entity.SPStaking;
import com.taibiex.stakingservice.repository.SpStakingRepository;
import jakarta.annotation.Resource;
import jakarta.persistence.Column;
import jakarta.persistence.criteria.Predicate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;


/**
 * 单币质押/解质押事件记录
 */
@Slf4j
@Service
public class SpStakingService {

    @Resource
    private SpStakingRepository spStakingRepository;

    @Transactional
    public void save(SPStaking spStaking) {
        //注意：这里数据库存的交易hash中不能和logIndex一起存，因为情况2时这里有可能Pool和NonfungiblePositionManager添加流动性事件共同维护此记录(记录才能完全)
        SPStaking m = spStakingRepository.findByTxHashAndType(spStaking.getTxHash(), spStaking.getType());
        if (m != null) {
            log.info("SpStakingService save ignore:  record existed! txHash: {} ,type: {}", spStaking.getTxHash(), spStaking.getType());
            return;
        }

        spStakingRepository.save(spStaking);
    }


    /**
     * 获取质押/解质押记录
     */
    public Page<SPStaking>  getStakingList(SPStakingDTO spStakingDTO){
        Specification<SPStaking> specification = (root, query, criteriaBuilder) -> {
            Predicate predicate = criteriaBuilder.conjunction();
            if (StringUtils.hasLength(spStakingDTO.getTxHash())) {
                predicate = criteriaBuilder.and(predicate, criteriaBuilder.equal(root.get("txHash"), spStakingDTO.getTxHash()));
            }
            if (StringUtils.hasLength(spStakingDTO.getUserAddress())) {
                predicate = criteriaBuilder.and(predicate, criteriaBuilder.equal(root.get("userAddress"), spStakingDTO.getUserAddress()));
            }

            if (spStakingDTO.getType() != null && spStakingDTO.getType() >= 0) {
                predicate = criteriaBuilder.and(predicate, criteriaBuilder.equal(root.get("type"), spStakingDTO.getType() ));
            }
            return predicate;
        };
        Page<SPStaking> actorPage = spStakingRepository.findAll(specification, spStakingDTO.getPageable());
        //log.info("分页查询第:[{}]页,pageSize:[{}],共有:[{}]数据,共有:[{}]页", spStakingDTO.getPageNumber(), spStakingDTO.getPageablePageSize(), actorPage.getTotalElements(), actorPage.getTotalPages());
        //List<SPStaking> stakingListBySpecification = actorPage.getContent();

        return actorPage;
    }

    /**
     * 更新Ustake表的claimed字段
     */
    void updateUnStakeInfoByClaimIndexList(List<String> claimIndexList){

        spStakingRepository.updateSPStakingByClaimIndexList(claimIndexList);
    }
}
