package com.taibiex.stakingservice.common.exception;

import com.taibiex.stakingservice.common.constant.ResultEnum;

public class AppWebException extends RuntimeException {

    private static final long serialVersionUID = -8198281171334131008L;

    private int errCode;

    public AppWebException() {
    }

    public AppWebException(int errCode, String message,
                           Exception exception) {
        super(message, exception);

        this.errCode = errCode;
    }

    public AppWebException(int errCode, String message) {
        this(errCode, message, null);
    }

    public AppWebException(ResultEnum resultEnum) {
        this(resultEnum.getCode(), resultEnum.getMessage(), null);
    }

    public int getErrCode() {
        return this.errCode;
    }
}
