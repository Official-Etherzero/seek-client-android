package com.easyetz.bean;

import java.io.Serializable;

public class ResponseResultBean<T> implements Serializable {

    public int status;
    public String message;
    public T result = null;

}