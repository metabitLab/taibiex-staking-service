// SPDX-License-Identifier: MIT

pragma solidity ^0.8.0;

import "@openzeppelin/contracts/token/ERC20/utils/SafeERC20.sol";

interface IRewardDistributor {

    function getBalance() external view returns(uint256);
    function claimReward(uint256 _amount) external returns(uint256);
    function pendingReward(uint256 _amount) external view returns(uint256);
    function rewardToken() external view returns(IERC20);
}
