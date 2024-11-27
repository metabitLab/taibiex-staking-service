package com.taibiex.stakingservice.common.exception;

import com.taibiex.stakingservice.common.bean.ErrorEntity;

public class BusinessException extends RuntimeException {
    private static final long serialVersionUID = -2964851871181700073L;
    private int code;
    private Object data;
    private Integer level;
    
    public BusinessException(){
      super();
    }

    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }
    
    public BusinessException(Integer code, String message) {
    	super(message);
    	this.code = code;
    }

    public BusinessException(Integer code, String message, Integer level) {
        super(message);
        this.code = code;
        this.level = level;
    }

    public BusinessException(ErrorEntity error) {
        super(error.getMessage());
        this.code = error.getCode();
    }

    public BusinessException(int code, String message, Object data, Integer level) {
        super(message);
        this.code = code;
        this.level = level;
        this.data = data;
    }

    public BusinessException(int code, String message, Throwable t, Integer level) {
        super(message, t);
        this.code = code;
        this.level = level;
    }
    
    public BusinessException(int code, String message, Throwable t) {
    	super(message, t);
    	this.code = code;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public Integer getLevel() {
        return level;
    }

    public void setLevel(Integer level) {
        this.level = level;
    }
}
