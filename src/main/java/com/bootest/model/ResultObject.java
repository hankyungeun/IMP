package com.bootest.model;

public class ResultObject {

    protected boolean result;
    protected String message;
    protected Object data;

    public ResultObject() {
        this(false, "");
    }

    public ResultObject(boolean result, String message) {
        this.result = result;
        this.message = message;
    }

    public boolean isResult() {
        return result;
    }

    public void setResult(boolean result) {
        this.result = result;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "ResultObject [result=" + result + ", message= " + message + ", data=" + data + "]";
    }

}
