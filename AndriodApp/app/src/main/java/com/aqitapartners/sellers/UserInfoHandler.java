package com.aqitapartners.sellers;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by Mustafa on 06-01-2016.
 */

public class UserInfoHandler {

    private static UserInfoHandler instance ;
    Context c;

    SharedPreferences userInfo ;
    SharedPreferences.Editor edit ;
    private Object isAuthenticated;


    private UserInfoHandler(Context c)
    {
        this.c = c ;
        userInfo = c.getSharedPreferences("userinfo" , Context.MODE_PRIVATE) ;
        edit = userInfo.edit();
    }

    public static UserInfoHandler getInstance(Context c)
    {
        if  (instance == null)
        {
            instance = new UserInfoHandler(c);
        }
        return instance ;
    }

    public void setIsAuthenticated() {
        edit.putBoolean("authenticated", true) ;
        edit.commit();
    }

    public void setIsNotAuthenticated() {
        edit.putBoolean("authenticated", false) ;
        edit.remove("storeId");
        edit.remove("name");
        edit.remove("type");
        edit.commit();
    }



    public void setStoreId(int id) {
        edit.putInt("storeId", id);
        edit.commit();
    }

    public void setName(String name) {
        edit.putString("name", name);
        edit.commit();
    }

    public String getName() {
        return userInfo.getString("name" , "NA");
    }

    public void setType(String type) {
        edit.putString("type", type);
        edit.commit();
    }

    public String getType()
    {
        return userInfo.getString("type" , "NA" ) ;
    }

    public String getStoreId() {
        return "" + userInfo.getInt("storeId", 0);
    }

    public boolean getIsAuthenticated() {
        return userInfo.getBoolean("authenticated", false) ;
    }

    public void setImageUrl(String imageUrl) {
        edit.putString("image" , imageUrl);
    }

    public String getImageUrl ()
    {
        return userInfo.getString("image" , "") ;
    }

/*
    public void setTags(String tags) {
        edit.putString("tags" , tags) ;
        edit.commit();
    }

    public String getTags() {
        return userInfo.getString("tags", "") ;
    }

    public void setArea(String area) {
        edit.putString("area" , area) ;
        edit.commit();
    }

    public String getArea() {
        return userInfo.getString("area" , "") ;
    }

    public void setPhone(String phone) {
        edit.putString("phone" , phone) ;
        edit.commit();
    }

    public String getPhone() {
        return userInfo.getString("phone" , "") ;
    }

    public void setTiming(String timing) {
        edit.putString("timing" , timing) ;
        edit.commit();
    }

    public String getTiming() {
        return userInfo.getString("timing" , "") ;
    }

    public void setDescription(String description) {
        edit.putString("description" , description) ;
        edit.commit();
    }

    public String getDescription() {
        return userInfo.getString("description" , "") ;
    }

    public void setServiceArea(String servicearea) {
        edit.putString("servicearea" , servicearea ) ;
        edit.commit();
    }

    public String getServiceArea() {
        return userInfo.getString("servicearea" , "") ;
    }


    public void setImageUrl(String imageUrl) {
        edit.putString("imageUrl" , imageUrl ) ;
        edit.commit();
    }

    public String getImageUrl() {
        return userInfo.getString("imageUrl" , "") ;
    }


    public void setRating(String rating) {
        edit.putString("rating" , rating) ;
        edit.commit();
    }

    public String getRating() {
        return userInfo.getString("rating" , "") ;
    }


    public void setAddress(String address) {
        edit.putString("address" , address ) ;
        edit.commit();
    }

    public String getAddress() {
        return userInfo.getString("address" , "") ;
    }

    public void setIsDeliveryAvailable (boolean isdelivery)
    {

    }
*/



}
