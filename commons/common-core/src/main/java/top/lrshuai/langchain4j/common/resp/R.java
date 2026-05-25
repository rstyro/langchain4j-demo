package top.lrshuai.langchain4j.common.resp;

import lombok.Data;

import java.io.Serializable;

/**
 * 响应信息主体
 */
@Data
public class R<T> implements Serializable {

    /**
     * 成功
     */
    public static final int SUCCESS = 200;
    public static final String SUCCESS_MSG = "ok";

    /**
     * 失败
     */
    public static final int ERROR = 500;
    public static final String ERROR_MSG = "服务异常";

    private int code;

    private String msg;

    private String trackerId;

    private T data;


    /**
     * 空构造，避免反序列化问题
     */
    public R() {
        this.code = SUCCESS;
        this.msg = SUCCESS_MSG;
    }

    public R(T data, int code, String msg) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    public static <T> R<T> ok() {
        return restResult(null, SUCCESS, SUCCESS_MSG);
    }

    public static <T> R<T> ok(T data) {
        return restResult(data, SUCCESS, SUCCESS_MSG);
    }

    public static <T> R<T> ok(T data, String msg) {
        return restResult(data, SUCCESS, msg);
    }

    public static <T> R<T> error() {
        return restResult(null, ERROR, ERROR_MSG);
    }

    public static <T> R<T> error(String msg) {
        return restResult(null, ERROR, msg);
    }

    public static <T> R<T> error(T data) {
        return restResult(data, ERROR, ERROR_MSG);
    }

    public static <T> R<T> error(T data, String msg) {
        return restResult(data, ERROR, msg);
    }

    public static <T> R<T> error(int code, String msg) {
        return restResult(null, code, msg);
    }


    private static <T> R<T> restResult(T data, int code, String msg) {
        return new R<T>(data,code,msg);
    }

    public static <T> Boolean isError(R<T> ret) {
        return !isSuccess(ret);
    }

    public static <T> Boolean isSuccess(R<T> ret) {
        return R.SUCCESS == ret.getCode();
    }

    public boolean isSuccess(){
        return R.SUCCESS == code;
    }

    /**
     * 链式调用
     */
    public R<T> code(int code) {
        this.code = code;
        return this;
    }

    public R<T> msg(String msg) {
        this.msg = msg;
        return this;
    }

    public R<T> data(T data) {
        this.data = data;
        return this;
    }

}
