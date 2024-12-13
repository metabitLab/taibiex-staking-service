// SPDX-License-Identifier: UNLICENSED
pragma solidity ^0.8.0;

interface IStakingPool {
    // 设置奖励分发池
    function setRewardDistributor(address _rewardDistributor) external;
    // 设置每个区块奖励
    function setRewardPerBlock(uint256 _newPerBlock) external;
    // 查询冻结中的解质押数量
    function freezing(address _user) external view returns (uint256);
    // 查询已解锁的解质押数量
    function pendingClaim(address _user) external view returns (uint256);
    // 查询待领取奖励
    function pendingReward(address _user) external view returns (uint256);
    // 更新奖励
    function update() external;
    // 质押
    function stake(uint256 _amount) external;
    // 解质押
    function unstake(uint256 _amount) external;
    // 领取已解锁的解质押
    function claim() external;
}
