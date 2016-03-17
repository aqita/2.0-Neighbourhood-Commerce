package com.aqitapartners.sellers;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ListView;
import android.widget.ProgressBar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Mustafa on 06-01-2016.
 */

public class ServicesFragment extends Fragment {

    public static final String SERVICE_FRAGMENT_TAG = "SF";
    private static final String ANIMATION_TAG = "ANIMATION" ;
    ListView list;
    ProgressBar bar;
    ServicesAdapter adapter;
    Map<Integer, Integer> itemIdToPositionHashMap ;

    public ServicesFragment() {

    }

    public void refreshListView() {

        list.invalidate();
        list.invalidateViews();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_services, null);
        list = (ListView) rootView.findViewById(R.id.services_list);

        adapter = new ServicesAdapter(getActivity());
        list.setAdapter(adapter);

        bar = (ProgressBar) rootView.findViewById(R.id.services_progress_bar);
        bar.setVisibility(View.VISIBLE);

        AsyncGet get = new AsyncGet(getActivity());

        List<Pair<String, String>> params = new ArrayList<Pair<String, String>>();

        UserInfoHandler handler = UserInfoHandler.getInstance(getActivity());
        params.add(new Pair<String, String>("storeid", handler.getStoreId()));

        get.setParams(params);
        get.setUseCase(AsyncGet.CASE_LIST);
        get.setOnRequestCompletedListener(new OnRequestCompleteListener() {

            @Override
            public void onRequestSuccessful(String response) {
                System.out.print("some response recieved");

                try {

                    List<ServiceListItem> data = new ArrayList<ServiceListItem>();
                    JSONArray dataList = new JSONArray(response);

                    for (int i = 0; i < dataList.length(); i++) {

                        JSONObject currentObj = dataList.getJSONObject(i);
                        ServiceListItem item = new ServiceListItem();
                        item.setName(currentObj.getString("name"));
                        item.setId("" + currentObj.getInt("offerId"));
                        item.setPrice("" + currentObj.getDouble("price"));

                        data.add(item);

                    }

                    adapter.setData(data);
                    bar.setVisibility(View.INVISIBLE);
                    list.invalidateViews();
                    list.invalidate();

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onRequestFailed(String error, Exception e) {

            }
        });

        get.start();

        return rootView;
    }

    public void setupDeleteAnimation()
    {
        if (  itemIdToPositionHashMap == null )
        {
            itemIdToPositionHashMap = new HashMap<Integer,Integer>();
        }

        int firstItem = list.getFirstVisiblePosition();
        Log.d(ANIMATION_TAG, "[Setup Animation] List Child count : " + list.getChildCount()) ;
        Log.d(ANIMATION_TAG, "[Setup Animation] List count       : " + list.getCount()) ;
        Log.d(ANIMATION_TAG, "[Setup Animation] Adappter   count : " + adapter.getCount()) ;
        Log.d(ANIMATION_TAG, "The first item is " + firstItem);

        int lastVisibleItem =  Math.min(firstItem + list.getChildCount() , list.getCount() );

        for (int i = 0 ; i < lastVisibleItem ; i ++)
        {
            itemIdToPositionHashMap.put( (int) adapter.getItemId( firstItem + i)  , list.getChildAt(i).getTop() ) ;
            Log.d(ANIMATION_TAG, "Position " + i + " : " +  itemIdToPositionHashMap.get((int) adapter.getItemId(i)) + " pixels" ) ;
        }

        list.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
            @Override
            public boolean onPreDraw() {
                list.getViewTreeObserver().removeOnPreDrawListener(this);
                int firstItem = list.getFirstVisiblePosition();
                Log.d(ANIMATION_TAG, "[Predraw] List Child count : " + list.getChildCount());
                Log.d(ANIMATION_TAG, "[Predraw] List count       : " + list.getCount());
                Log.d(ANIMATION_TAG, "[Predraw] Adappter count   : " + adapter.getCount());
                Log.d(ANIMATION_TAG, "The first item is " + firstItem);

                int lastVisibleItem =  Math.min(firstItem + list.getChildCount() , list.getCount() );
                System.out.println("last visible item" + lastVisibleItem);

                for (int i = 0 ; i < lastVisibleItem  ; i++) {
                    int position = i + firstItem ;
                    View currentView = list.getChildAt(i);
                    int itemId = (int) adapter.getItemId(position);
                    int oldPosition = (int) itemIdToPositionHashMap.get(itemId);
                    Log.d(ANIMATION_TAG , "old Position : " +  oldPosition ) ;
                    int delta = currentView.getTop() - oldPosition;
                    Log.d(ANIMATION_TAG, "The calculated delta is" + delta);

                    currentView.setTranslationY( -delta);
                    currentView.animate().setDuration(300).translationY(0).withEndAction(new Runnable() {
                        @Override
                        public void run() {

                        }
                    });
                    currentView.animate().start();
                }

                return true;
            }
        });
    }


    public void setupAdditionAnimation (ServiceListItem item)
    {

    }

    public void addOrUpdateItem (ServiceListItem item)
    {
//        setupDeleteAnimation();
        adapter.addOrUpdateItem(item);
        list.invalidate();
        list.invalidateViews();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        System.out.println("On Activity Result Fragment");

        if (requestCode == 786)
        {
            if ( resultCode == Activity.RESULT_OK)
            {

                String newName = data.getStringExtra(EditOffer.RETURN_NAME);
                String newQty = data.getStringExtra(EditOffer.RETURN_QTY);
                String newUOM = data.getStringExtra(EditOffer.RETURN_UOM);
                String newPrice = data.getStringExtra(EditOffer.RETURN_PRICE);
                String newId = data.getStringExtra(EditOffer.RETURN_ID);
                boolean newIsUOMAvailable = data.getBooleanExtra(EditOffer.RETURN_HASUOM , false) ;

                ServiceListItem item = new ServiceListItem() ;
                item.setName(newName);
                item.setId(newId);
                item.setPrice(newPrice);
                item.setUom(newUOM);
                item.setIsUOMAvailable(newIsUOMAvailable);

                addOrUpdateItem(item);

            }
        }
    }

    public void deleteItem(int index) {

        setupDeleteAnimation();
        adapter.remove(index);
        refreshListView();
    }
}
