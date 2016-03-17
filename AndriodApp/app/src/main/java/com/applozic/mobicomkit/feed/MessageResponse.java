package com.applozic.mobicomkit.feed;

import com.applozic.mobicomkit.api.JsonMarker;

/**
 * Created by sunil on 22/10/15.
 */
public class MessageResponse extends JsonMarker {
    private String messageKey;
    private String createdAt;

    public MessageResponse() {
    }

    public String getMessageKey() {
        return messageKey;
    }

    public void setMessageKey(String messageKey) {
        this.messageKey = messageKey;
    }

    public String getCreatedAtTime() {
        return createdAt;
    }

    public void setCreatedAtTime(String createdAtTime) {
        this.createdAt = createdAtTime;
    }
}
