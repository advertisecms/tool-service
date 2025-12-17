package com.ai.center.model;

import java.io.Serializable;

/**
 * 全局统一返回结果封装类
 * @param <T> 数据泛型类型
 */
public class Result<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 响应状态码（200成功，其他为失败）
     */
    private int code;

    /**
     * 响应消息
     */
    private String msg;

    /**
     * 响应数据
     */
    private T data;

    /**
     * 响应时间戳（毫秒）
     */
    private long timestamp;

    // 私有化构造器，通过静态方法创建实例
    private Result() {
        this.timestamp = System.currentTimeMillis();
    }

    // 静态内部类：常用状态码枚举（可根据业务扩展）
    public enum ResultCode {
        SUCCESS(200, "操作成功"),
        PARAM_ERROR(400, "参数错误"),
        BUSINESS_ERROR(401, "业务异常"),
        UNAUTHORIZED(403, "未授权/登录失效"),
        NOT_FOUND(404, "资源不存在"),
        SYSTEM_ERROR(500, "系统异常");

        private final int code;
        private final String msg;

        ResultCode(int code, String msg) {
            this.code = code;
            this.msg = msg;
        }

        public int getCode() {
            return code;
        }

        public String getMsg() {
            return msg;
        }
    }

    // ========== 快捷创建方法 ==========

    /**
     * 通用成功返回（无数据）
     */
    public static <T> Result<T> ok() {
        return buildResult(ResultCode.SUCCESS.getCode(), ResultCode.SUCCESS.getMsg(), null);
    }

    /**
     * 成功返回（带数据）
     */
    public static <T> Result<T> ok(T data) {
        return buildResult(ResultCode.SUCCESS.getCode(), ResultCode.SUCCESS.getMsg(), data);
    }

    /**
     * 成功返回（自定义消息+数据）
     */
    public static <T> Result<T> ok(String msg, T data) {
        return buildResult(ResultCode.SUCCESS.getCode(), msg, data);
    }

    /**
     * 通用失败返回（默认系统异常）
     */
    public static <T> Result<T> fail() {
        return buildResult(ResultCode.SYSTEM_ERROR.getCode(), ResultCode.SYSTEM_ERROR.getMsg(), null);
    }

    /**
     * 失败返回（自定义消息）
     */
    public static <T> Result<T> fail(String msg) {
        return buildResult(ResultCode.SYSTEM_ERROR.getCode(), msg, null);
    }

    /**
     * 失败返回（自定义状态码+消息）
     */
    public static <T> Result<T> fail(int code, String msg) {
        return buildResult(code, msg, null);
    }

    /**
     * 失败返回（自定义状态码+消息+数据）
     */
    public static <T> Result<T> fail(int code, String msg, T data) {
        return buildResult(code, msg, data);
    }

    /**
     * 根据枚举返回结果（推荐，统一状态码管理）
     */
    public static <T> Result<T> result(ResultCode resultCode) {
        return buildResult(resultCode.getCode(), resultCode.getMsg(), null);
    }

    public static <T> Result<T> result(ResultCode resultCode, T data) {
        return buildResult(resultCode.getCode(), resultCode.getMsg(), data);
    }

    // ========== 链式调用方法 ==========

    public Result<T> code(int code) {
        this.code = code;
        return this;
    }

    public Result<T> msg(String msg) {
        this.msg = msg;
        return this;
    }

    public Result<T> data(T data) {
        this.data = data;
        return this;
    }

    // ========== 私有构建方法 ==========

    private static <T> Result<T> buildResult(int code, String msg, T data) {
        Result<T> result = new Result<>();
        result.code = code;
        result.msg = msg;
        result.data = data;
        return result;
    }

    // ========== Getter & Setter ==========

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    // ========== 重写toString，方便日志打印 ==========

    @Override
    public String toString() {
        return "Result{" +
                "code=" + code +
                ", msg='" + msg + '\'' +
                ", data=" + data +
                ", timestamp=" + timestamp +
                '}';
    }


}