package com.easyetz.bean;

import java.io.Serializable;

public class ResponseGasBean implements Serializable {
    public String jsonrpc;
    public String id;
    public String result;
    public GasError error;
}
