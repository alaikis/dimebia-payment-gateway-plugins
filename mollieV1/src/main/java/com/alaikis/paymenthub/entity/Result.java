package com.alaikis.paymenthub.entity;


import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class Result {
    private boolean ok=true;
    private Object result=null;
    private String errors=null;

    public Object getResult() {
        return result;
    }

    public void setResult(Object result) {
        this.result = result;
    }

    public String getErrors() {
        return errors;
    }

    public void setErrors(String errors) {
        this.errors = errors;
    }

    public boolean isOk() {
        return ok;
    }

    public void setOk(boolean ok) {
        this.ok = ok;
    }
}
