package com.company;

import java.io.Serializable;

public class Response implements Serializable {

    private long id;
    private long requestId;
    private int responseType;
    private String responseMessage;

    public Response(){}

    public long getRequestId() {
        return requestId;
    }

    public void setRequestId(long requestId) {
        this.requestId = requestId;
    }

    public String getResponseMessage() {
        return responseMessage;
    }

    public void setResponseMessage(String responseMessage) {
        this.responseMessage = responseMessage;
    }

    public int getResponseType() {
        return responseType;
    }

    public void setResponseType(int responseType) {
        this.responseType = responseType;
    }
}
