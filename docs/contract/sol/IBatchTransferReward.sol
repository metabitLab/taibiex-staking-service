// SPDX-License-Identifier: MIT
pragma solidity ^0.8.0;

import "@openzeppelin/contracts/token/ERC20/utils/SafeERC20.sol";

interface IBatchTransferReward {
    function getRewardBalance() external view returns (uint256);
    function batchTransferReward(address[] calldata users, address rewardToken, uint256[] calldata amounts) external;
    function setRewardToken(IERC20 _rewardToken) external;
    function emergencyWithdraw(uint256 _amount) external;
}
