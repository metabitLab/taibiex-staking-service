package com.taibiex.stakingservice.common.chain.contract.listener.filter;

import org.springframework.util.ObjectUtils;
import org.web3j.abi.EventEncoder;
import org.web3j.abi.datatypes.Event;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.methods.request.EthFilter;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Monitor {

    public static EthFilter getFilter(DefaultBlockParameter start, DefaultBlockParameter end, Event event, String address) {
        return new EthFilter(start, end, address).addSingleTopic(EventEncoder.encode(event));
    }

    public static EthFilter getFilter(BigInteger start, BigInteger end, Event event, String address) {
        return new EthFilter(DefaultBlockParameter.valueOf(start), DefaultBlockParameter.valueOf(end), address).addSingleTopic(EventEncoder.encode(event));
    }

    public static EthFilter getFilter(BigInteger start, BigInteger end, Event event, List<String> address) {
        return new EthFilter(DefaultBlockParameter.valueOf(start), DefaultBlockParameter.valueOf(end), address).addSingleTopic(EventEncoder.encode(event));
    }

    // andi add 20230219
    public static EthFilter getFilter(DefaultBlockParameter start, DefaultBlockParameter end, List<Event> events, List<String> addresses) {
        //表示 addresses 中每一个地址 都订阅 events 中所有的事件(对应的topic),
        // 即 address1 订阅 所有event( event1,event2, ... eventN); address2订阅 所有event( event1,event2, ... eventN)  ..... addressN 订阅 所有event( event1,event2, ... eventN)
        EthFilter ethFilter = new EthFilter(start, end, addresses);
        // String[] evs = new String[events.size()];

        //去除 null 元素
        events = events.stream().filter(e -> !ObjectUtils.isEmpty(e)).collect(Collectors.toList());
        List<String> eventTopics = new ArrayList<>();
        for (int i = 0; i < events.size(); i++) {
            Event event = events.get(i);
            try {
                if (ObjectUtils.isEmpty(event)) {
                    continue;
                }
                String topic = EventEncoder.encode(event);
                if (ObjectUtils.isEmpty(topic)) {
                    continue;
                }
                eventTopics.add(topic);
            } catch (Exception e) {
                System.out.println("getFilter add to eventTopics failed" + e.getMessage());
            }
        }
        String[] evs = eventTopics.stream().toArray(String[]::new);

        // don't use the addSingleTopics, It doesn't work
        //String... equals a String[]
        ethFilter.addOptionalTopics(evs);

        return ethFilter;
    }

    public static EthFilter getFilter(BigInteger start, BigInteger end, List<Event> events, List<String> addresses) {
        return getFilter(DefaultBlockParameter.valueOf(start), DefaultBlockParameter.valueOf(end), events, addresses);
    }

}
