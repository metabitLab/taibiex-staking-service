package com.taibiex.stakingservice.common.handler;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;

import com.taibiex.stakingservice.common.bean.ResponseResult;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import java.lang.reflect.Method;
import java.util.Date;
import java.util.Map;

@Slf4j
@ControllerAdvice
public class AppResponseInterceptor implements ResponseBodyAdvice {

    @Override
    public boolean supports(MethodParameter methodParameter, Class aClass) {

        return true;
    }

    @Override
    public Object beforeBodyWrite(Object o, MethodParameter methodParameter, MediaType mediaType, Class aClass,
                                  ServerHttpRequest serverHttpRequest, ServerHttpResponse serverHttpResponse) {
        // 响应结果执行
        if (mediaType != null && o != null
                && mediaType.includes(MediaType.APPLICATION_JSON)
                && o instanceof ResponseResult) {

            if (serverHttpRequest instanceof ServletServerHttpRequest) {

                ServletServerHttpRequest request = (ServletServerHttpRequest) serverHttpRequest;

                HttpServletRequest httpServletRequest = request.getServletRequest();


                Method method = methodParameter.getMethod();

                log.debug("request controller:" + method.getDeclaringClass() + " request method:" + method.getName());

                Date requestTime = (Date) httpServletRequest.getAttribute("http_request_time");

                if (requestTime != null) {
                    long useTime = System.currentTimeMillis() - requestTime.getTime();

                    log.debug("request link:" + serverHttpRequest.getURI() + " times:" + useTime);
                }

                Map<String, String> tokenMap = (Map<String, String>) httpServletRequest.getAttribute("newTokenMap");
                if (null != tokenMap) {
                    JSONObject jsonObject = ((JSONObject) ((ResponseResult<?>) o).getData());
                    if (null != jsonObject) {
                        jsonObject.put("newTokenMap", tokenMap);
                    } else {
                        JSONObject result = new JSONObject();
                        result.put("newTokenMap", tokenMap);
                        ((ResponseResult) o).setData(result);
                    }
                }
            }

            log.debug("response content:" + JSON.toJSONString(o, SerializerFeature.WriteNullStringAsEmpty));

        }

        return o;
    }
}
