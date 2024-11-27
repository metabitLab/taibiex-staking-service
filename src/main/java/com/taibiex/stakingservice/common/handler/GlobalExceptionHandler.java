package com.taibiex.stakingservice.common.handler;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.taibiex.stakingservice.common.bean.ResponseResult;
import com.taibiex.stakingservice.common.constant.ErrorConstant;
import com.taibiex.stakingservice.common.exception.AppWebException;
import com.taibiex.stakingservice.common.exception.BusinessException;
import jakarta.servlet.ServletException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.ConversionNotSupportedException;
import org.springframework.beans.TypeMismatchException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.io.IOException;
import java.net.ConnectException;


@RestControllerAdvice
public class GlobalExceptionHandler {

    protected static Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(Exception.class)
    public ResponseResult<String> error(Exception e) {
        return failed(ErrorConstant.COMMON_EXCEPTION, e);
    }

    //运行时异常
    @ExceptionHandler(RuntimeException.class)
    //@ResponseBody
    public ResponseResult<String> runtimeExceptionHandler(RuntimeException runtimeException) {
        return failed(ErrorConstant.RUNTIME_EXCEPTION, runtimeException);
    }

    //空指针异常
    @ExceptionHandler(NullPointerException.class)
    //@ResponseBody
    public ResponseResult<String> nullPointerExceptionHandler(NullPointerException ex) {
        return failed(ErrorConstant.NULL_POINTER_EXCEPTION, ex);
    }

    //类型转换异常
    @ExceptionHandler(ClassCastException.class)
    //@ResponseBody
    public ResponseResult<String> classCastExceptionHandler(ClassCastException ex) {
        return failed(ErrorConstant.CLASS_CAST_EXCEPTION, ex);
    }


    @ExceptionHandler(value = {MethodArgumentNotValidException.class, BindException.class})
    //@ResponseBody
    public ResponseResult<String> argumentError(Exception e) {
        e.printStackTrace();
        BindingResult bindingResult = null;
        if (e instanceof MethodArgumentNotValidException) {
            bindingResult = ((MethodArgumentNotValidException) e).getBindingResult();
        } else if (e instanceof BindException) {
            bindingResult = ((BindException) e).getBindingResult();
        }
        StringBuilder msg = new StringBuilder();
        assert bindingResult != null;
        bindingResult.getFieldErrors().forEach((fieldError) ->
                msg.append(fieldError.getField() + ":" + fieldError.getDefaultMessage()).append(" ")
        );
        return failed(ErrorConstant.BAD_REQUEST.getCode(), msg.toString().trim(), e);
    }

    //IO异常
    @ExceptionHandler(IOException.class)
    //@ResponseBody
    public ResponseResult<String> iOExceptionHandler(IOException ex) {
        return failed(ErrorConstant.IO_EXCEPTION, ex);
    }

    //未知方法异常
    @ExceptionHandler(NoSuchMethodException.class)
    //@ResponseBody
    public ResponseResult<String> noSuchMethodExceptionHandler(NoSuchMethodException ex) {
        return failed(ErrorConstant.NO_SUCH_METHOD_EXCEPTION, ex);
    }

    //数组越界异常
    @ExceptionHandler(IndexOutOfBoundsException.class)
    //@ResponseBody
    public ResponseResult<String> indexOutOfBoundsExceptionHandler(IndexOutOfBoundsException ex) {
        return failed(ErrorConstant.INDEX_OUT_OF_BOUNDS_EXCEPTION, ex);
    }

    //网络异常
    @ExceptionHandler(ConnectException.class)
    // @ResponseBody
    public ResponseResult<String> connectException(ConnectException ex) {
        return failed(ErrorConstant.CONNECT_EXCEPTION, ex);
    }

    //400错误
    @ExceptionHandler({HttpMessageNotReadableException.class})
    //@ResponseBody
    public ResponseResult<String> requestNotReadable(HttpMessageNotReadableException ex) {
        return failed(ErrorConstant.BAD_REQUEST, ex);
    }

    //400错误
    @ExceptionHandler({TypeMismatchException.class})
    //@ResponseBody
    public ResponseResult<String> requestTypeMismatch(TypeMismatchException ex) {
        return failed(ErrorConstant.BAD_REQUEST, ex);
    }

    //400错误
    @ExceptionHandler({MissingServletRequestParameterException.class})
    //@ResponseBody
    public ResponseResult<String> requestMissingServletRequest(MissingServletRequestParameterException ex) {
        return failed(ErrorConstant.BAD_REQUEST, ex);
    }

    @ExceptionHandler({ServletException.class})
    //@ResponseBody
    public ResponseResult<String> http404(ServletException ex) {
        return failed(ErrorConstant.NOT_FOUND_REQUEST, ex);
    }

    //405错误
    @ExceptionHandler({HttpRequestMethodNotSupportedException.class})
    //@ResponseBody
    public ResponseResult<String> request405(HttpRequestMethodNotSupportedException ex) {
        return failed(ErrorConstant.METHOD_NOT_ALLOWED, ex);
    }

    //406错误
    @ExceptionHandler({HttpMediaTypeNotAcceptableException.class})
    //@ResponseBody
    public ResponseResult<String> request406(HttpMediaTypeNotAcceptableException ex) {
        return failed(ErrorConstant.NOT_ACCEPTABLE, ex);
    }

    //500错误
    @ExceptionHandler({ConversionNotSupportedException.class, HttpMessageNotWritableException.class})
    //@ResponseBody
    public ResponseResult<String> server500(RuntimeException runtimeException) {
        return failed(ErrorConstant.INTERNAL_SERVER_ERROR, runtimeException);
    }

    //app web 异常
    @ExceptionHandler({AppWebException.class})
    //@ResponseBody
    public ResponseResult<String> appWebException(AppWebException appWebException) {
        return failed(appWebException.getErrCode(), appWebException.getMessage(), appWebException);
    }


    //业务逻辑异常
    @ExceptionHandler({BusinessException.class})
    //@ResponseBody
    public ResponseResult<String> appWebException(BusinessException businessException) {
        return failed(businessException.getCode(), businessException.getMessage(), businessException);
    }

    @ExceptionHandler({JsonMappingException.class})
    //@ResponseBody
    public ResponseResult<String> jsonMappingException(JsonMappingException jsonMappingException) {
        return failed(ErrorConstant.ERROR_FORMAT_PARAMETER, jsonMappingException);
    }


    private ResponseResult<String> failed(int code, String message, Exception e) {
        ResponseResult<String> resultDO = new ResponseResult<>(code, message, null);

        logException(e);

        return resultDO;
    }

    private ResponseResult<String> failed(ErrorConstant errorConstant, Exception e) {
        ResponseResult<String> resultDO = new ResponseResult<>(errorConstant.getCode(), e.getMessage());

        logException(e);

        return resultDO;
    }

    /**
     * 异常记录
     *
     * @param e
     */
    private void logException(Exception e) {
        if (e instanceof AppWebException) {
            LOGGER.warn(e.getMessage(), e);
        } else {
            LOGGER.error(e.getMessage(), e);
        }
    }
}
