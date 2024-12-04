package com.taibiex.stakingservice.common.chain.contract.utils;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;

import java.beans.PropertyDescriptor;


public class CustomBeanUtils {

    /**
     * 浅拷贝 拷贝 字段为 非null 和非空字符串 的字段
     * @param source
     * @param target
     */
    public static void copyNonNullAndNonEmptyProperties(Object source, Object target) {
        final BeanWrapper src = new BeanWrapperImpl(source);
        final BeanWrapper tgt = new BeanWrapperImpl(target);
        PropertyDescriptor[] propertyDescriptors = src.getPropertyDescriptors();

        for (PropertyDescriptor propertyDescriptor : propertyDescriptors) {
            String propertyName = propertyDescriptor.getName();
            Object value = src.getPropertyValue(propertyName);
            

            // 检查非 null 和非空字符串
            if (!StringUtils.equalsIgnoreCase(propertyName, "class") &&
                    value != null && !(value instanceof String && ((String) value).isEmpty())) {
                tgt.setPropertyValue(propertyName, value);
            }
        }
    }
}