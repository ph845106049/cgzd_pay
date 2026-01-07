package com.cgzd.pay.utils;

/**
 * 描述：API 统一返回状态码
 *
 * @author He Yong
 * @version 1.0
 * @since 2018/1/31
 */
public enum ResultCode {

    /* 成功状态码 */
    SUCCESS(1, "成功"),
    /* 失败状态码 */
    FAILURE(2, "失败"),
    /* 参数错误：10001-19999 */
    PARAM_IS_INVALID(10001, "参数无效"),
    PARAM_IS_BLANK(10002, "参数为空"),
    PARAM_TYPE_BIND_ERROR(10003, "参数类型错误"),
    PARAM_NOT_COMPLETE(10004, "参数缺失"),
    REMOTE_LOGIN(10005, "异地登录"),
    NOT_LOGIN(10006, "用户未登录"),
    PARAM_KEY_REPEAT(10007, "参数键值重复"),
    NAME_REPEAT(10008, "名称重复"),
    PARENT_NODE_INVALID(10009, "父节点无效"),
    PASSWORD_SAME(10010, "密码不一致"),
    ADMIN_NOT_CHANGE(10011, "管理员不可修改"),
    PHONE_NOT_REG(10012, "手机号未注册"),
    PHONE_REG_ED(10013, "手机号已注册"),
    SMS_CODE_ERROR(10014, "手机验证码错误"),
    SMS_CODE_EXP(10015, "手机验证码过期"),
    USER_NOT_FOUND(10016, "用户不存在"),
    OLD_PWD_ERROR(10017, "旧密码错误"),
    PWD_SAME(10018, "密码不可相同"),
    PWD_LEN_ERROR(10019, "密码长度不足"),
    GET_OPENID_FAIL(10020, "获取 openid 失败!"),
    ILLEGAL_CONTENT(10021, "非法内容!"),
    ACCESS_ERROR(10022, "获取微信TOKEN失败!"),
    INTERFACE_ERROR(10022, "接口调用失败!"),
    NO_DATA(10023, "数据不存在"),
    SIGN_REPEAT(10024, "签到重复"),
    IS_REG(10025, "已注册"),
    NOT_REG(10026, "未注册"),
    PARAM_REPEAT(10027, "参数重复"),
    AUTH_FAIL(99998, "认证失败"),
    SYS_ERROR(99999, "系统异常");

    private int code;

    private String msg;

    ResultCode(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public int code() {
        return code;
    }

    public String msg() {
        return msg;
    }
}