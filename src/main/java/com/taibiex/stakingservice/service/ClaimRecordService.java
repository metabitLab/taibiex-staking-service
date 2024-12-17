package com.taibiex.stakingservice.service;

import com.taibiex.stakingservice.common.chain.contract.utils.Web3jUtils;
import com.taibiex.stakingservice.common.constant.ResultEnum;
import com.taibiex.stakingservice.common.exception.AppWebException;
import com.taibiex.stakingservice.common.utils.RedisService;
import com.taibiex.stakingservice.entity.ClaimRecord;
import com.taibiex.stakingservice.entity.UserPoolReward;
import com.taibiex.stakingservice.repository.ClaimRecordRepository;
import com.taibiex.stakingservice.repository.UserPoolRewardRepository;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.protocol.exceptions.TransactionException;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

@Log4j2
@Service
public class ClaimRecordService {

    private static final Object lockBatchClaimTaskKey = new Object();
    private static boolean lockBatchClaimTaskFlag = false;

    private static final int MAX_RETRIES = 20; // 动态调整重试次数
    private static final long SLEEP_DURATION_MS = 10000; // 动态调整睡眠时间

    private static final String USER_POOL_REWARD_CLAIMED_KEY = "USER_POOL_REWARD_CLAIMED_";

    private static final String CLAIM_RECORD_LOCK_KEY = "CLAIM_RECORD_LOCK";

    private final RedisService redisService;

    private final Web3jUtils web3jUtils;

    private final UserPoolRewardRepository userPoolRewardRepository;

    private final ClaimRecordRepository claimRecordRepository;

    public ClaimRecordService(RedisService redisService, Web3jUtils web3jUtils,
                              UserPoolRewardRepository userPoolRewardRepository,
                              ClaimRecordRepository claimRecordRepository) {
        this.redisService = redisService;
        this.web3jUtils = web3jUtils;
        this.userPoolRewardRepository = userPoolRewardRepository;
        this.claimRecordRepository = claimRecordRepository;
    }


    public void claim(String userAddress){
        String claimLockKey = USER_POOL_REWARD_CLAIMED_KEY + userAddress;
        try {
            if (redisService.setNx(claimLockKey, "1", 60, TimeUnit.SECONDS)){
                log.info("UserPoolRewardService claim userAddress:{}", userAddress);
                List<UserPoolReward> userPoolRewardList = userPoolRewardRepository.findByUserAddressAndClaimed(userAddress, false);
                if (userPoolRewardList.isEmpty()){
                    return;
                }
                Map<String, Map<String, String>> userRewardMap = new HashMap<>();
                for (UserPoolReward userPoolReward : userPoolRewardList) {
                    Map<String, String> reward = userRewardMap.get(userPoolReward.getTokenAddress());
                    if (reward == null){
                        reward = new HashMap<>();
                        reward.put("amount", userPoolReward.getRewardAmount());
                        reward.put("tokenAddress", userPoolReward.getTokenAddress());
                        reward.put("tokenSymbol", userPoolReward.getTokenSymbol());
                        reward.put("mainNet", String.valueOf(userPoolReward.isMainNet()));
                        reward.put("poolRewardId", String.valueOf(userPoolReward.getId()));
                        userRewardMap.put(userPoolReward.getTokenAddress(), reward);
                    } else {
                        reward.put("amount", new BigInteger(reward.get("amount")).add(new BigInteger(userPoolReward.getRewardAmount())).toString());
                        reward.put("poolRewardId", reward.get("poolRewardId") + "," + userPoolReward.getId());
                    }
                    userPoolReward.setClaimed(true);
                }
                List<ClaimRecord> claimRecords = new ArrayList<>();
                userRewardMap.values().forEach(reward -> {
                    ClaimRecord claimRecord = new ClaimRecord();
                    claimRecord.setUserAddress(userAddress);
                    claimRecord.setTokenAddress(reward.get("tokenAddress"));
                    claimRecord.setTokenSymbol(reward.get("tokenSymbol"));
                    claimRecord.setRewardAmount(reward.get("amount"));
                    claimRecord.setPoolRewardId(reward.get("poolRewardId"));
                    claimRecord.setMainNet(Boolean.parseBoolean(reward.get("mainNet")));
                    claimRecord.setClaimed(false);
                    claimRecords.add(claimRecord);
                });
                claimRecordRepository.saveAll(claimRecords);
                userPoolRewardRepository.saveAll(userPoolRewardList);
            } else {
                throw new AppWebException(ResultEnum.ERROR_PARAMS.getCode(), "userAddress:" + userAddress + " is claimed");
            }
        } catch (Exception e){
            log.error("UserPoolRewardService claim error", e);
        } finally {
            redisService.del(claimLockKey);
        }
    }

    @Scheduled(fixedDelayString = "60000")
    public void claimAll(){
        synchronized (lockBatchClaimTaskKey) {
            if (ClaimRecordService.lockBatchClaimTaskFlag) {
                log.warn("The batch claim task is already in progress");
                return;
            }
            ClaimRecordService.lockBatchClaimTaskFlag = true;
        }
        try {
            if (redisService.setNx(CLAIM_RECORD_LOCK_KEY, "1", 60, TimeUnit.SECONDS)){
                List<ClaimRecord> unclaimed = claimRecordRepository.findAllByClaimed(false);
                if (unclaimed.isEmpty()){
                    return;
                }
                Map<String, List<ClaimRecord>> claimInfoMap = new HashMap<>();
                for (ClaimRecord claimRecord : unclaimed) {
                    List<ClaimRecord> claimRecords = claimInfoMap.get(claimRecord.getTokenAddress());
                    if (claimRecords == null){
                        claimRecords = new ArrayList<>();
                    }
                    claimRecords.add(claimRecord);
                    claimInfoMap.put(claimRecord.getUserAddress(), claimRecords);
                }
                for (String tokenAddress : claimInfoMap.keySet()) {

                    List<ClaimRecord> claimRecords = claimInfoMap.get(tokenAddress);
                    List<Address> users = new ArrayList<>();
                    List<Uint256> amounts = new ArrayList<>();
                    String rewardToken = claimRecords.get(0).getTokenAddress();
                    if (claimRecords.get(0).isMainNet()){
                        rewardToken = "0x01";
                    }
                    for (ClaimRecord claimRecord : claimRecords) {
                        users.add(new Address(claimRecord.getUserAddress()));
                        amounts.add(new Uint256(new BigInteger(claimRecord.getRewardAmount())));
                    }
                    String txHash;
                    try {
                        txHash = batchClaim(users, rewardToken, amounts);
                        log.info("claimAll batchClaim txHash: {}", txHash);
                    } catch (InterruptedException | TransactionException e) {
                        log.error("claimAll batchClaim error: ", e);
                        throw new RuntimeException(e);
                    }
                    claimRecords.forEach(claimRecord -> {
                        claimRecord.setTxHash(txHash);
                        claimRecord.setClaimed(true);
                    });
                    claimRecordRepository.saveAll(claimRecords);
                }
            }
        } catch (Exception e){
            log.error("claim record service claimAll error", e);
        } finally {
            redisService.del(CLAIM_RECORD_LOCK_KEY);
            ClaimRecordService.lockBatchClaimTaskFlag = false;
        }
    }

    private String batchClaim(List<Address> users, String rewardToken, List<Uint256> amounts) throws InterruptedException, TransactionException {
        String txHash;
        int j = 0;
        do {
            try {
                txHash = web3jUtils.sendBatchTransferRewardTransfer(users, rewardToken, amounts);
                log.info("Attempt {}: Transaction hash: {}", j + 1, txHash);
            } catch (Exception e) {
                log.error("Attempt {}: Error sending batch transfer: {}", j + 1, e.getMessage());
                txHash = null;
            }
            j++;
            if (j < MAX_RETRIES && (txHash == null || txHash.isBlank())) {
                TimeUnit.MILLISECONDS.sleep(SLEEP_DURATION_MS);
            }
        } while (j < MAX_RETRIES && (txHash == null || txHash.isBlank()));

        if (txHash == null || txHash.isBlank()) {
            log.error("All attempts failed to get a valid transaction hash.");
            throw new AppWebException(ResultEnum.ERROR_PARAMS.getCode(), "All attempts failed to get a valid transaction hash.");
        }

        TransactionReceipt txReceipt = web3jUtils.waitForTransactionReceipt(txHash);
        // If status in response equals 1 the transaction was successful. If it is equals 0 the transaction was reverted by EVM.
        if (Integer.parseInt(txReceipt.getStatus().substring(2), 16) == 0) {
            log.error("==========>batch claim failed txHash {} revert reason: {}", txHash, txReceipt.getRevertReason());
            throw new AppWebException(ResultEnum.ERROR_PARAMS.getCode(), "batch claim failed txHash:" + txHash + " revert reason:" + txReceipt.getRevertReason());
        }
        return txHash;
    }
}
