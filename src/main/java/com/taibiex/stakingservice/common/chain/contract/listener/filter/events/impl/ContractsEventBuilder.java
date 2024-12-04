package com.taibiex.stakingservice.common.chain.contract.listener.filter.events.impl;

import com.taibiex.stakingservice.common.chain.contract.listener.filter.events.ContractsEventEnum;
import com.taibiex.stakingservice.common.chain.contract.listener.filter.events.EventBuilder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.*;
import org.web3j.abi.datatypes.generated.*;

import java.util.Arrays;


public class ContractsEventBuilder implements EventBuilder<ContractsEventEnum> {

    @Override
    public Event build(ContractsEventEnum type) {
        switch (type) {
            case SWAP:
                return getSwapEvent();
            case SWAP_DESC:
                return getSwapEventDesc();
            case MINT:
                return getMintEvent();
            case MINT_DESC:
                return getMintEventDesc();
            case BURN:
                return getBurnEvent();
            case BURN_DESC:
                return getBurnEventDesc();
            case INCREASE_LIQUIDITY:
                return getIncreaseLiquidityEvent();
            case INCREASE_LIQUIDITY_DESC:
                return getIncreaseLiquidityEventDesc();
            case DECREASE_LIQUIDITY:
                return getDecreaseLiquidityEvent();
            case DECREASE_LIQUIDITY_DESC:
                return getDecreaseLiquidityEventDesc();
            case POOL_CREATED:
                return getPoolCreatedEvent();
            case POOL_CREATED_DESC:
                return getPoolCreatedEventDesc();
            default:
                return null;
        }
    }

    private Event getSwapEvent() {
        Event event = new Event("Swap",
                Arrays.<TypeReference<?>>asList(
                        new TypeReference<Address>(true) {//sender address
                        },
                        new TypeReference<Address>(true) {//recipient address
                        },
                        // 表示这次 Swap 交易中 token0 的数量变化。
                        // 如果是正值,表示 token0 被输入到流动性池中;如果是负值,表示 token0 被取出。
                        new TypeReference<Int256>(false) { //amount0
                        },
                        // 表示这次 Swap 交易中 token1 的数量变化。
                        // 如果是正值,表示 token1 被输入到流动性池中;如果是负值,表示 token1 被取出。
                        new TypeReference<Int256>(false) { //amount1
                        },
                        // 表示 Swap 交易完成后,流动性池的价格平方根乘以 2^96。
                        // 这个值用于计算资产的实际价格。
                        new TypeReference<Uint160>(false) { //sqrtPriceX96
                        },
                        // 表示 Swap 交易完成后,该流动性池中的总流动性数量
                        new TypeReference<Uint128>(false) { //liquidity
                        },
                        // 表示 Swap 交易完成后,流动性池的当前价格 ticks。
                        new TypeReference<Int24>(false) { //tick
                        }
                ));
        return event;
    }

    private Event getSwapEventDesc() {
        Event event = new Event("Swap",
                Arrays.<TypeReference<?>>asList(
                        // 表示这次 Swap 交易中 token0 的数量变化。
                        // 如果是正值,表示 token0 被输入到流动性池中;如果是负值,表示 token0 被取出。
                        new TypeReference<Int256>(false) { //amount0
                        },
                        // 表示这次 Swap 交易中 token1 的数量变化。
                        // 如果是正值,表示 token1 被输入到流动性池中;如果是负值,表示 token1 被取出。
                        new TypeReference<Int256>(false) { //amount1
                        },
                        // 表示 Swap 交易完成后,流动性池的价格平方根乘以 2^96。
                        // 这个值用于计算资产的实际价格。
                        new TypeReference<Uint160>(false) { //sqrtPriceX96
                        },
                        // 表示 Swap 交易完成后,该流动性池中的总流动性数量
                        new TypeReference<Uint128>(false) { //liquidity
                        },
                        // 表示 Swap 交易完成后,流动性池的当前价格 ticks。
                        new TypeReference<Int24>(false) { //tick
                        }
                ));
        return event;
    }

    private Event getMintEvent() {
        Event event = new Event("Mint",
                Arrays.<TypeReference<?>>asList(
                        new TypeReference<Address>(false) {//sender address
                        },
                        new TypeReference<Address>(true) {//owner address
                        },
                        new TypeReference<Int24>(true) { //tickLower
                        },
                        new TypeReference<Int24>(true) { //tickUpper
                        },
                        new TypeReference<Uint128>(false) { //amount
                        },
                        new TypeReference<Uint256>(false) { //amount0
                        },
                        new TypeReference<Uint256>(false) { //amount1
                        }
                ));
        return event;
    }

    private Event getMintEventDesc() {
        Event event = new Event("Mint",
                Arrays.<TypeReference<?>>asList(
                        new TypeReference<Address>(false) {//sender address
                        },
                        new TypeReference<Uint128>(false) { //amount
                        },
                        new TypeReference<Uint256>(false) { //amount0
                        },
                        new TypeReference<Uint256>(false) { //amount1
                        }
                ));
        return event;
    }

    private Event getBurnEvent() {
        //Burn(address indexed owner, int24 indexed tickLower, int24 indexed tickUpper, uint128 amount, uint256 amount0, uint256 amount1)
        Event event = new Event("Burn",
                Arrays.<TypeReference<?>>asList(
                        new TypeReference<Address>(true) {//owner address
                        },
                        new TypeReference<Int24>(true) { //tickLower
                        },
                        new TypeReference<Int24>(true) { //tickUpper
                        },
                        new TypeReference<Uint128>(false) { //amount
                        },
                        new TypeReference<Uint256>(false) { //amount0
                        },
                        new TypeReference<Uint256>(false) { //amount1
                        }
                ));
        return event;
    }

    private Event getBurnEventDesc() {
        //Burn(address indexed owner, int24 indexed tickLower, int24 indexed tickUpper, uint128 amount, uint256 amount0, uint256 amount1)
        Event event = new Event("Burn",
                Arrays.<TypeReference<?>>asList(
                        new TypeReference<Uint128>(false) { //amount
                        },
                        new TypeReference<Uint256>(false) { //amount0
                        },
                        new TypeReference<Uint256>(false) { //amount1
                        }
                ));
        return event;
    }

    private Event getIncreaseLiquidityEvent() {
        Event event = new Event("IncreaseLiquidity",
                Arrays.<TypeReference<?>>asList(
                        new TypeReference<Uint256>(true) {//tokenId
                        },
                        new TypeReference<Uint128>(false) { //liquidity
                        },
                        new TypeReference<Uint256>(false) { //amount0
                        },
                        new TypeReference<Uint256>(false) { //amount1
                        }
                ));
        return event;
    }

    private Event getIncreaseLiquidityEventDesc() {
        Event event = new Event("IncreaseLiquidity",
                Arrays.<TypeReference<?>>asList(
                        new TypeReference<Uint128>(false) { //liquidity
                        },
                        new TypeReference<Uint256>(false) { //amount0
                        },
                        new TypeReference<Uint256>(false) { //amount1
                        }
                ));
        return event;
    }


    private Event getDecreaseLiquidityEvent() {
        Event event = new Event("DecreaseLiquidity",
                Arrays.<TypeReference<?>>asList(
                        new TypeReference<Uint256>(true) {//tokenId
                        },
                        new TypeReference<Uint128>(false) { //liquidity
                        },
                        new TypeReference<Uint256>(false) { //amount0
                        },
                        new TypeReference<Uint256>(false) { //amount1
                        }
                ));
        return event;
    }

    private Event getDecreaseLiquidityEventDesc() {
        Event event = new Event("DecreaseLiquidity",
                Arrays.<TypeReference<?>>asList(
                        new TypeReference<Uint128>(false) { //liquidity
                        },
                        new TypeReference<Uint256>(false) { //amount0
                        },
                        new TypeReference<Uint256>(false) { //amount1
                        }
                ));
        return event;
    }

    private Event getPoolCreatedEvent(){
        Event event = new Event("PoolCreated",
                Arrays.<TypeReference<?>>asList(
                        new TypeReference<Address>(true) {//token0
                        },
                        new TypeReference<Address>(true) {// token1
                        },
                        new TypeReference<Uint24>(true) { //fee
                        },
                        new TypeReference<Int24>(false) { //tickSpacing
                        },
                        new TypeReference<Address>(false) { //pool
                        }
                ));
        return event;
    }

    private Event getPoolCreatedEventDesc(){
        Event event = new Event("PoolCreated",
                Arrays.<TypeReference<?>>asList(
                        new TypeReference<Int24>(false) { //tickSpacing
                        },
                        new TypeReference<Address>(false) { //pool
                        }
                ));
        return event;
    }

}
