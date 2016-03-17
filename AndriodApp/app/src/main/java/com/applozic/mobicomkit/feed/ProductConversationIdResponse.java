package com.applozic.mobicomkit.feed;

/**
 * Created by sunil on 23/12/15.
 */
public class ProductConversationIdResponse {


    private String status;
    private String generatedAt;
    private Object response;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getGeneratedAt() {
        return generatedAt;
    }

    public void setGeneratedAt(String generatedAt) {
        this.generatedAt = generatedAt;
    }

    public Object getResponse() {
        return response;
    }

    public void setResponse(Object response) {
        this.response = response;
    }

    @Override
    public String toString() {
        return "ProductConversationIdResponse{" +
                "status='" + status + '\'' +
                ", generatedAt='" + generatedAt + '\'' +
                ", response='" + response + '\'' +
                '}';
    }
}
