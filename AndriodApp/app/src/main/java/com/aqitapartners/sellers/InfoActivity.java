package com.aqitapartners.sellers;

import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Pair;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class InfoActivity extends AppCompatActivity {

    ImageView image ;
    TextView name, phone, area, tags,time, address, description;
    RatingBar ratingBar ;
    ProgressBar pb ;
    LinearLayout parent ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);

        image = (ImageView) findViewById(R.id.profile_image);
        name = (TextView) findViewById(R.id.profile_name) ;
        phone = (TextView) findViewById(R.id.profile_phone) ;
        area = (TextView) findViewById(R.id.profile_area) ;
        time = (TextView) findViewById(R.id.profile_timings);
        tags = (TextView) findViewById(R.id.profile_tags) ;
        address = (TextView) findViewById(R.id.profile_address) ;
        description = (TextView) findViewById(R.id.profile_description) ;
        ratingBar = (RatingBar) findViewById(R.id.profile_rating) ;
        ratingBar.setMax(5);

        parent = (LinearLayout) findViewById(R.id.profile_parent);
        parent.setVisibility(View.INVISIBLE);

        pb = (ProgressBar) findViewById(R.id.profile_progress) ;

        ActionBar actionBar = getSupportActionBar() ;
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);

        AsyncGet getUserInfo = new AsyncGet(this);
        getUserInfo.setUseCase(AsyncGet.CASE_FETCH_USER_INFO);
        List<Pair<String,String>> paramsForUserInfo = new ArrayList< Pair<String,String>>();
        paramsForUserInfo.add(new Pair<String, String>("storeid" , UserInfoHandler.getInstance(this).getStoreId()));
        getUserInfo.setParams(paramsForUserInfo);

        getUserInfo.setOnRequestCompletedListener(new OnRequestCompleteListener() {
            @Override
            public void onRequestSuccessful(String response) {

                JSONObject jUserInfo = null;
                try {
                    jUserInfo = new JSONObject(response);

                    System.out.print("JSON Object" + jUserInfo.toString());

                    /**
                     * {
                     "storeId": 201,
                     "name": "Store1",
                     "tags": "grocery | fruits \u0026amp vegetables | fresh",
                     "area": "Ulsoor",
                     "phone": "9999999999",
                     "timing": "10.30 pm to 4.30 pm, 7 days open",
                     "description": "store 1 is the best",
                     "servicearea": "Ulsoor, Indranagar, Cox Town",
                     "image": "www.image.jpg",
                     "rating": "4.2",
                     "address": "10/7, store 1 road, ulsoor",
                     "homedelivery": false
                     * }
                     */


                    int id = jUserInfo.getInt("storeId");
                    String nameStr = jUserInfo.getString("name");
                    name.setText(nameStr);
//                    String typeStr = jUserInfo.getString("type");
                    String tagsStr = jUserInfo.getString("tags");
                    tags.setText(tagsStr);
                    String areaStr = jUserInfo.getString("area");
                    area.setText(areaStr);
                    String phoneStr = jUserInfo.getString("phone");
                    phone.setText(phoneStr);
                    String timingStr = jUserInfo.getString("timing");
                    time.setText(timingStr);
                    String descriptionStr = jUserInfo.getString("description");
                    description.setText(descriptionStr);
//                    String serviceareaStr = jUserInfo.getString("servicearea");
//                    String imageStr = jUserInfo.getString("image");
//                    image.setImageURI(Uri.parse(imageStr));

                    String ratingStr = jUserInfo.getString("rating");
                    ratingBar.setRating(Float.valueOf(ratingStr));
                    String addressStr = jUserInfo.getString("address");
                    boolean isDeliveryAvailable = jUserInfo.getBoolean("homedelivery");

                    parent.setVisibility(View.VISIBLE);
                    pb.setVisibility(View.INVISIBLE);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onRequestFailed(String error, Exception e) {

            }
        });

        getUserInfo.start();

    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home :
                onBackPressed();
                return true ;
        }

        return super.onOptionsItemSelected(item);
    }
}
