package com.easyetz.bean;

import java.io.Serializable;

public class BaseResponseBean implements Serializable {

    private static final long serialVersionUID = -1477609349345966116L;

    public int status;
    public String message;

    public ResponseBean toResponseBean() {
        ResponseBean responseBean = new ResponseBean();
        responseBean.status = status;
        responseBean.message = message;
        return responseBean;
    }

    public int getCode() {
        return status;
    }

    public void setCode(int code) {
        this.status = code;
    }

    public String getMessage() {
        return message == null ? "" : message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}