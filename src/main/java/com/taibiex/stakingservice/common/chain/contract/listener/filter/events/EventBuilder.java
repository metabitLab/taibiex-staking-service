package com.taibiex.stakingservice.common.chain.contract.listener.filter.events;

import org.web3j.abi.datatypes.Event;


public interface EventBuilder<T extends Enum> {
    /**
     * build eth event
     * @return
     */
    Event build(T type);
}
