package com.taibiex.stakingservice.common.bean;

import com.taibiex.stakingservice.common.constant.ErrorConstant;
import com.taibiex.stakingservice.common.constant.ResultEnum;

import java.io.Serializable;
import java.util.Arrays;

public class ResponseResult<T> implements Serializable {

    private boolean success;
    private int code;
    private String message;
    private T data;

    public ResponseResult() {
        this.code = ResultEnum.SUCCESS.getCode();
        this.message = ResultEnum.SUCCESS.getMessage();
        this.success = true;
    }

    public ResponseResult(int code, String message) {
        this.code = code;
        this.message = message;
        this.success = getDefaultSuccess();
    }

    public ResponseResult(ResultEnum resultEnum) {
        this.code = resultEnum.getCode();
        this.message = resultEnum.getMessage();
        this.success = getDefaultSuccess();
    }

    public ResponseResult(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
        this.success = getDefaultSuccess();
    }

    public ResponseResult(ErrorConstant errorConstant) {
        this.code = errorConstant.getCode();
        this.message = errorConstant.getMsg();
        this.data = null;
        this.success = false;
    }

    public ResponseResult(ErrorConstant errorConstant, T data) {
        this.code = errorConstant.getCode();
        this.message = errorConstant.getMsg();
        this.data = data;
        this.success = false;
    }

    public ResponseResult(ResultEnum resultEnum, T data) {
        this.code = resultEnum.getCode();
        this.message = resultEnum.getMessage();
        this.data = data;
        this.success = getDefaultSuccess();
    }

    public ResponseResult(T data) {
        this.code = ResultEnum.SUCCESS.getCode();
        this.message = ResultEnum.SUCCESS.getMessage();
        this.data = data;
        this.success = true;
    }

    public static <T> ResponseResult<T> success(T data) {
        return new ResponseResult<>(ResultEnum.SUCCESS, data);
    }

    public static <T> ResponseResult<T> fail(int code, String message) {
        return new ResponseResult<>(code, message);
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public boolean getSuccess() {
        return success;
    }

    private boolean getDefaultSuccess() {
        return Arrays.asList(new Integer[]{200, 201}).contains(code);
    }

    public void setCode(boolean success) {
        this.success = success;
    }

}
