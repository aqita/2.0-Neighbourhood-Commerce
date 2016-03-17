package com.aqitapartners.sellers;

/**
 * Created by Mustafa on 06-01-2016.
 */
public interface OnRequestCompleteListener {

    public void onRequestSuccessful (String response) ;
    public void onRequestFailed (String error , Exception e) ;


}
