package com.taibiex.stakingservice.common.constant;

public enum ResultEnum {
    /*
     * 操作成功
     */
    SUCCESS(200, "success"),

    /*
     * 201	Created	 已创建。成功请求并创建了新的资源
     */
    SUCCESS_CREATED(201, "Successfully inserted the task into the task execution queue."),
    /*
     * 无法判断
     */
    ERROR_PARAMS(400, "params error"),

    ALBUM_NOT_EXIST(404, "album is not exist"),

    NOT_OWN_ALBUM(403, "you don't own the album"),

    /**
     * 用户地址为null
     */
    ACCOUNT_ERROR(405, "account is null"),

    CONFLICT(409, "Conflict"),
    /*
     * 提示信息
     */
    ERROR_MSG(502, "system error"),


    USER_EXIST(601, "user exist"),

    USER_NOT_EXIT(602, "USER_NOT_EXIT"),
    USER_PARENT_NOT_EXIST(603, "user parent not exist"),

    NO_Commodity(604, "NO Commodity"),

    Stage_error(605, "Stage_error"),

    KEY_ERROR(606, "key error or login status invalid"), //签名错误或登录状态失效

    SIGN_ERROR(607, "sign error"),

    COMM_BIND(608, "commodity is bind"),

    LOGIN_FAILED(609, "Login Failed"),

    INSERT_FAILED(610, "插入失败"),

    DELETE_FAILED(611, "删除失败"),
    ROLE_NOT_EXIST(612, "角色不存在"),

    USER_ROLE_EXIST(613, "用户对应角色已存在"),

    DELETE_USER_NOT_AUTHORIZATION(614, "您没有权限删除此用户"),

    REFRESH_TOKEN_FAILED(615, "刷新token失败"),

    REFRESH_TOKEN_EXPIRED(616, "refreshToken已过期"),

    UPDATE_FAILED(617, "更新失败"),

    PASSWORD_ERROR(618, "用户名或密码错误"),

    ALBUM_EXIST(619, "专辑已存在，请勿重复创建"),

    CREATOR_NOT_UNIQUE(620, "专辑作者不唯一"),

    /*
     * 提示信息
     */
    REWARD_FAILED(621, "领取奖励失败"),


    BUSINESS_EXCEPTION_PARAMETER_UPLOAD_FILE_IS_EMPTY(51012, "上传文件不能为空"),

    BUSINESS_EXCEPTION_UPLOAD_FILE_IS_NOT_IMAGE(52001, "上传文件并非图片类型"),

    BUSINESS_EXCEPTION_UPLOAD_FILE_FAILED(52002, "上传文件失败，请重试");

    public int code;

    public String message;

    private ResultEnum(int code, String message) {
        this.code = code;
        this.message = message;
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

}
