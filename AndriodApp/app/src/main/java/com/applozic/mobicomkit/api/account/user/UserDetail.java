package com.applozic.mobicomkit.api.account.user;

import com.applozic.mobicomkit.api.JsonMarker;

import java.math.BigInteger;

/**
 * Created by sunil on 24/11/15.
 */
public class UserDetail extends JsonMarker {

    private String userId;
    private boolean connected;
    private String displayName;
    private Long lastSeenAtTime;
    private BigInteger unreadCount;


    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public boolean isConnected() {
        return connected;
    }

    public void setConnected(boolean connected) {
        this.connected = connected;
    }

    public Long getLastSeenAtTime() {
        return lastSeenAtTime;
    }

    public void setLastSeenAtTime(Long lastSeenAtTime) {
        this.lastSeenAtTime = lastSeenAtTime;
    }

    public BigInteger getUnreadCount() {
        return unreadCount;
    }

    public void setUnreadCount(BigInteger unreadCount) {
        this.unreadCount = unreadCount;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    @Override
    public String toString() {
        return "UserDetail{" +
                "userId='" + userId + '\'' +
                ", connected=" + connected +
                ", displayName=" + displayName +
                ", lastSeenAtTime=" + lastSeenAtTime +
                ", unreadCount=" + unreadCount +
                '}';
    }
}
