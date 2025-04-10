package com.alaikis.paymenthub.entity;

public class ResultUtil {
    public static Result success(Object data) {
        Result result = new Result();
        result.setResult(data);
        result.setOk(true);
        return result;
    }

    public static Result fail(String message) {
        Result result = new Result();
        result.setErrors(message);
        result.setOk(true);
        return result;
    }
}
