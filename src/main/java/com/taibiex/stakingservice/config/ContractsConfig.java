package com.taibiex.stakingservice.config;

import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.ObjectUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Data
@Configuration
@ConfigurationProperties(prefix = "contracts")
public class ContractsConfig {

    private List<ContractInfo> contractList = new ArrayList<>();

    @Data
    public static class ContractInfo {

        private String name;

        public String getName() {
            if (name != null) {
                return name.toLowerCase();
            }
            return null;
        }

        public void setName(String name) {
            if (name != null) {
                this.name = name.toLowerCase();
            }
        }

        private String address;

        private Boolean enabled;

        public String getAddress() {
            if (address != null) {
                return address.toLowerCase();
            }
            return address;
        }

    }

    private Map<String, ContractInfo> mapProps = null;

    public synchronized Map<String, ContractInfo> getContractInfoMap() {

        if (ObjectUtils.isEmpty(mapProps)) {

            mapProps = new HashMap<>(contractList.size());

            for (ContractInfo contractInfo : contractList) {
                mapProps.put(contractInfo.getName(), contractInfo);
            }

        }
        return mapProps;
    }

    public ContractInfo getContractInfo(String contractName) {
        Map<String, ContractInfo> contractInfoMap = getContractInfoMap();
        return contractInfoMap.get(contractName.toLowerCase());
    }

    public synchronized List<String> getContractAddresses() {
        return contractList.stream().map(ContractInfo::getAddress).map(String::toLowerCase).collect(Collectors.toList());
    }


    public synchronized List<String> getEnabledContractAddresses() {
        return contractList.stream().filter(ct -> ct.getEnabled() && StringUtils.isNotBlank(ct.getAddress())).map(ContractInfo::getAddress).map(String::toLowerCase).collect(Collectors.toList());
    }

    /**
     * 动态增加从数据库中获取的合约地址来扫描
     * @param contractInfos
     */
    public synchronized void addContractAddresses(List<ContractInfo> contractInfos)
    {

        // 将 List 的值转换为 Map
        Map<String, ContractInfo> contractInfoMap = contractList.stream()
                .collect(Collectors.toMap(ContractInfo::getAddress, contractInfo1 -> contractInfo1));

        for (ContractInfo contractInfo: contractInfos)
        {
            contractInfoMap.put(contractInfo.getAddress(), contractInfo);
        }

        // 将 Map 的值转换为 List
        List<ContractInfo> newContractList = new ArrayList<>(contractInfoMap.values());

        contractList = newContractList;

    }

    /**
     * 动态增加从数据库中获取的合约地址来扫描
     * @param contractInfo
     */
    public synchronized void addContractAddress(ContractInfo contractInfo)
    {
        // 将 List 的值转换为 Map
        Map<String, ContractInfo> contractInfoMap = contractList.stream()
                .collect(Collectors.toMap(ContractInfo::getAddress, contractInfo1 -> contractInfo1));

        contractInfoMap.put(contractInfo.getAddress(), contractInfo);


        // 将 Map 的值转换为 List
        List<ContractInfo> newContractList = new ArrayList<>(contractInfoMap.values());

        contractList = newContractList;

    }
}
