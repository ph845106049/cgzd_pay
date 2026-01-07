package com.cgzd.pay.utils;



import java.util.HashMap;
import java.util.Objects;

/**
 * @author xiaoqiang
 * @Description 返回结果信息
 * @date 2019/3/21 16:50
 */
public class Result extends HashMap<Object, Object> {

    public static String CODE_KEY = "code";
    public static String MSG_KEY = "msg";
    public static String DATA_KEY = "data";


    /**
     * 调用结果(-1 异常 0 失败 1 成功)
     */
    public enum CallResult {
        /**
         * 异常
         */
        ERROR(-1, "异常"),
        /**
         * 失败
         */
        FAIL(0, "失败"),
        /**
         * 成功
         */
        SUCCESS(1, "成功");
        /**
         * code码
         */
        private Integer code;
        /**
         * 提示信息
         */
        private String msg;

        private CallResult(Integer code, String msg) {
            this.code = code;
            this.msg = msg;
        }

        public Integer getCode() {
            return code;
        }

        public String getMsg() {
            return msg;
        }
    }

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    /**
     * 成功
     *
     * @return
     */
    public static Result success() {
        Result result = new Result();
        result.put(CODE_KEY, Result.CallResult.SUCCESS.getCode());
        result.put(MSG_KEY, Result.CallResult.SUCCESS.getMsg());
        result.put(DATA_KEY, null);
        return result;
    }

    public static Result success(String msg) {
        Result result = success();
        result.put(MSG_KEY, msg);
        return result;
    }

    public static Result success(ResultCode code, Object obj) {
        Result result = success();
        result.put(CODE_KEY, code.code());
        result.put(MSG_KEY, code.msg());
        result.put(DATA_KEY, obj);
        return result;
    }

    public static Result success(Object obj) {
        Result result = success();
        result.put(DATA_KEY, obj);
        return result;
    }

    public static Result success(String msg, Object obj) {
        Result result = success();
        result.put(MSG_KEY, msg);
        result.put(DATA_KEY, obj);
        return result;
    }

    /**
     * 失败
     *
     * @return
     */
    public static Result fail() {
        Result result = new Result();
        result.put(CODE_KEY, Result.CallResult.FAIL.getCode());
        result.put(MSG_KEY, Result.CallResult.FAIL.getMsg());
        result.put(DATA_KEY, null);
        return result;
    }

    public static Result fail(String msg) {
        Result result = fail();
        result.put(MSG_KEY, msg);
        return result;
    }

    public static Result fail(ResultCode code, Object obj) {
        Result result = fail();
        result.put(CODE_KEY, code.code());
        result.put(MSG_KEY, code.msg());
        result.put(DATA_KEY, obj);
        return result;
    }

    public static Result fail(Object obj) {
        Result result = fail();
        result.put(DATA_KEY, obj);
        return result;
    }

    public static Result fail(String msg, Object obj) {
        Result result = fail();
        result.put(MSG_KEY, msg);
        result.put(DATA_KEY, obj);
        return result;
    }

    /**
     * 异常
     *
     * @return
     */
    public static Result error() {
        Result result = new Result();
        result.put(CODE_KEY, Result.CallResult.ERROR.getCode());
        result.put(MSG_KEY, Result.CallResult.ERROR.getMsg());
        result.put(DATA_KEY, null);
        return result;
    }

    public static Result error(String msg) {
        Result result = error();
        result.put(MSG_KEY, msg);
        return result;
    }

    public static Result error(Object obj) {
        Result result = error();
        result.put(DATA_KEY, obj);
        return result;
    }

    public static Result error(String msg, Object obj) {
        Result result = error();
        result.put(MSG_KEY, msg);
        result.put(DATA_KEY, obj);
        return result;
    }

    /**
     * 自定义
     *
     * @param code
     * @param msg
     * @param obj
     * @return
     */
    public static Result custom(Object code, Object msg, Object obj) {
        Result result = new Result();
        result.put(CODE_KEY, code);
        result.put(MSG_KEY, msg);
        result.put(DATA_KEY, obj);
        return result;
    }

    /**
     * 自定义返回
     * @param code
     * @param obj
     * @return
     */
    public static Result custom(ResultCode code, Object obj) {
        Result result = new Result();
        result.put(CODE_KEY, code.code());
        result.put(MSG_KEY, code.msg());
        result.put(DATA_KEY, obj);
        return result;
    }


    /**
     *
     * @return
     */
    public static Result notLogin(){
        Result result = new Result();
        result.put(CODE_KEY, ResultCode.NOT_LOGIN.code());
        result.put(MSG_KEY, ResultCode.NOT_LOGIN.msg());
        result.put(DATA_KEY, null);
        return result;
    }

    /**
     * 验证成功
     * @param result
     * @return
     */
    public static boolean isSuccess(Result result) {
        return !Objects.isNull(result.get(CODE_KEY)) && (Integer) result.get(CODE_KEY) == 1;
    }

}
