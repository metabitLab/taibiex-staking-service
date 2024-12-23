package com.taibiex.stakingservice.service;

import com.taibiex.stakingservice.dto.ClaimStakingDTO;
import com.taibiex.stakingservice.entity.ClaimEvent;
import com.taibiex.stakingservice.entity.ClaimEvent;
import com.taibiex.stakingservice.repository.ClaimRepository;
import jakarta.annotation.Resource;
import jakarta.persistence.criteria.Predicate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.List;

/**
 * 单币质押领取记录
 * claim是领取解锁的token，不是领取奖励. claim那个得等7天才能解锁才能claim
 * claim是一次性领取所有解锁了的本金，claimIndex是选定哪一期解锁. 和奖励没关系
 * (已解锁的)解质押事件(一笔提取所有的解质押事件) 或 提取某一笔质押事件
 */
@Slf4j
@Service
public class ClaimService {

    @Resource
    private ClaimRepository claimRepository;

    @Resource
    private SpStakingService spStakingService;

    @Transactional
    public void save(ClaimEvent claimEvent) {
        //注意：这里数据库存的交易hash中不能和logIndex一起存，因为情况2时这里有可能Pool和NonfungiblePositionManager添加流动性事件共同维护此记录(记录才能完全)
        ClaimEvent m = claimRepository.findByTxHashAndUserAddressAndClaimIndex(claimEvent.getTxHash(), claimEvent.getUserAddress(), claimEvent.getClaimIndex());

        if (m != null) {
            log.info("ClaimService save ignore:  record existed! txHash: {} ,user: {}, claimIndex: {}", claimEvent.getTxHash(), claimEvent.getUserAddress(), claimEvent.getClaimIndex());
            return;
        }

        claimRepository.save(claimEvent);
    }


    @Transactional
    public void userClaimHandler(ClaimEvent claimEvent) {

        this.save(claimEvent);

        String claimIndexListString = claimEvent.getClaimIndex();
        // 使用 String.split 将逗号连接的字符串转换为 List<String>
        List<String> claimIndexList = Arrays.asList(claimIndexListString.split(","));
        //更新Ustake表的claimed字段
        spStakingService.updateUnStakeInfoByClaimIndexList(claimIndexList);
    }

    @Transactional
    public void userClaimIndexHandler(ClaimEvent claimEvent) {

        this.save(claimEvent);


        String claimIndex = claimEvent.getClaimIndex();

        // 使用 String.split 将逗号连接的字符串转换为 List<String>
        List<String> claimIndexList = Arrays.asList(claimIndex.split(","));
        //更新Ustake表的claimed字段
        spStakingService.updateUnStakeInfoByClaimIndexList(claimIndexList);
    }

    /**
     * 获取已领取(已解锁本金的)的claim列表
     */
    public Page<ClaimEvent> getClaimedList(ClaimStakingDTO claimStakingDTO){
        Specification<ClaimEvent> specification = (root, query, criteriaBuilder) -> {
            Predicate predicate = criteriaBuilder.conjunction();
            if (StringUtils.hasLength(claimStakingDTO.getTxHash())) {
                predicate = criteriaBuilder.and(predicate, criteriaBuilder.equal(root.get("txHash"), claimStakingDTO.getTxHash()));
            }
            if (StringUtils.hasLength(claimStakingDTO.getUserAddress())) {
                predicate = criteriaBuilder.and(predicate, criteriaBuilder.equal(root.get("userAddress"), claimStakingDTO.getUserAddress()));
            }

            if (StringUtils.hasLength(claimStakingDTO.getClaimIndex())) {
                predicate = criteriaBuilder.and(predicate, criteriaBuilder.equal(root.get("claimIndex"), claimStakingDTO.getClaimIndex() ));
            }
            return predicate;
        };
        Page<ClaimEvent> actorPage = claimRepository.findAll(specification, claimStakingDTO.getPageable());
        //log.info("分页查询第:[{}]页,pageSize:[{}],共有:[{}]数据,共有:[{}]页", claimStakingDTO.getPageNumber(), claimStakingDTO.getPageablePageSize(), actorPage.getTotalElements(), actorPage.getTotalPages());
        //List<ClaimEvent> stakingListBySpecification = actorPage.getContent();

        return actorPage;
    }
}
