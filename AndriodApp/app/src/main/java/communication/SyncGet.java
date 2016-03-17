package communication;

import android.util.Pair;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

/**
 * Created by Mustafa on 06-01-2016.
 */
public class SyncGet {

    public static String BASE_URL = "http://52.76.118.161:7070/api/v1/" ;
    public static String CASE_VERIFY_URL_SLUG = "verifyStore" ;
    public static final String CASE_FETCH_USER_INFO = "getStoreDetailsForPartners";

    List<Pair <String , String> > params ;
    String useCase ;
    String response ;

    public void setParams(List<Pair<String, String>> params) {
        this.params = params;
    }


    public void setUseCase(String useCase) {
        this.useCase = useCase;
    }

    public String getResponse() {
        return response;
    }

    public boolean execute ()
    {

        URL url = null;
        try {

            StringBuilder urlString =  new StringBuilder(BASE_URL + useCase) ;

            if (!params.isEmpty())
            {
                urlString.append("?") ;

                urlString.append(params.get(0).first);
                urlString.append( "=" );
                urlString.append(params.get(0).second);

            }

            for ( int i = 1 ; i < params.size() ; i++ )
            {
                urlString.append("&") ;
                urlString.append(params.get(i).first);
                urlString.append( "=" );
                urlString.append(params.get(i).second);
            }

            System.out.print("Request url = " + urlString);

            url = new URL(urlString.toString());
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.connect();
            InputStream stream = new BufferedInputStream(conn.getInputStream()) ;


            StringBuilder sb=new StringBuilder();
            BufferedReader br = new BufferedReader(new InputStreamReader(stream));
            String read;

            while((read=br.readLine()) != null) {
                sb.append(read);
            }

            response = sb.toString();
            System.out.println(response);
            return true ;

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


        return false ;
    }


}
