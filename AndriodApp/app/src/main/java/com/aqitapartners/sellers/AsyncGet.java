package com.aqitapartners.sellers;

import android.app.Activity;
import android.content.Context;
import android.util.Pair;
import android.webkit.URLUtil;

import com.applozic.mobicomkit.api.HttpRequestUtils;

import org.apache.http.protocol.HTTP;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.List;

/**
 * Created by Mustafa on 06-01-2016.
 */
public class AsyncGet extends Thread {

    public static final String BASE_URL = "http://52.76.118.161:7070/api/v1/" ;
    public static final String CASE_LIST = "getOfferings" ;
    public static final String CASE_UPDATE = "updateOrAddOffering" ;
    public static final String CASE_DELETE = "removeOffering" ;
    public static final String CASE_FETCH_USER_INFO = "getStoreDetailsForPartners" ;

    Context c;

    List<Pair<String , String> > params ;
    String useCase ;
    String response ;

    OnRequestCompleteListener orcl ;

    public void setParams(List<Pair<String, String>> params) {
        this.params = params;
    }

    public AsyncGet (Context c)
    {
        this.c = c ;
    }


    public void setUseCase(String useCase) {
        this.useCase = useCase;
    }

    public String getResponse() {
        return response;
    }

    public void setOnRequestCompletedListener(OnRequestCompleteListener orcl )
    {
        this.orcl = orcl ;
    }

    @Override
    public void run() {
        super.run();

        URL url = null;
        try {

            StringBuilder urlString =  new StringBuilder(BASE_URL + useCase) ;

            if (!params.isEmpty())
            {
                urlString.append("?") ;

                urlString.append(params.get(0).first);
                urlString.append( "=" );
                urlString.append(URLEncoder.encode(params.get(0).second , "UTF-8"));
            }

            for ( int i = 1 ; i < params.size() ; i++ )
            {
                urlString.append("&") ;
                urlString.append(params.get(i).first);
                urlString.append( "=" );
                urlString.append(URLEncoder.encode(params.get(i).second , "UTF-8") );
            }

            System.out.print("Request url = " + urlString + "\n" );

            url = new URL(urlString.toString());

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.connect();
            InputStream stream = new BufferedInputStream(conn.getInputStream()) ;

            StringBuilder sb = new StringBuilder();
            BufferedReader br = new BufferedReader(new InputStreamReader(stream));
            String read;

            while((read=br.readLine()) != null) {
                sb.append(read);
            }

            response = sb.toString();
            System.out.println(response);

            ((Activity) c).runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    orcl.onRequestSuccessful(response);
                }
            });

        } catch (MalformedURLException e) {
            e.printStackTrace();
            orcl.onRequestFailed("Bad URL", e);
        } catch (IOException e) {
            e.printStackTrace();
            orcl.onRequestFailed("Internet Exception", e);
        }







    }
}
