package com.easyetz.bean;

import java.io.Serializable;

public class ResponseBean<T> implements Serializable {

    public int status;
    public String message;
    public T data = null;

}