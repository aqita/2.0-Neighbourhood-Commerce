package com.aqitapartners.sellers;

import com.aqitapartners.sellers.R;
import android.content.Context;
import android.content.Intent;
import android.database.DataSetObserver;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ListAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

/**
 * Created by Mustafa on 06-01-2016.
 */
public class ServicesAdapter implements ListAdapter {

    List<ServiceListItem> data ;
    HashMap<Object , Integer> mIds ;
    Context c ;
    private static final String TAG = "ServicesAdapter" ;

    public ServicesAdapter (Context c )
    {
        this.c = c ;
        data = new ArrayList<ServiceListItem>();
        mIds = new HashMap<Object , Integer>();
    }

    @Override
    public void registerDataSetObserver(DataSetObserver observer) {

    }

    @Override
    public void unregisterDataSetObserver(DataSetObserver observer) {

    }

    @Override
    public int getCount() {
        return data.size() ;
    }

    @Override
    public Object getItem(int position) {
        return data.get(position);
    }

    @Override
    public long getItemId(int position) {
        Object key = getItem(position);
        if( key !=null & mIds.containsKey(key))
        {
            return mIds.get( key ) ;
        }
        return -1 ;
//        return position ;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (convertView == null)
        {
            LayoutInflater inflater = (LayoutInflater) c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.service_list_item , null) ;
        }

        TextView name = (TextView) convertView.findViewById(R.id.service_list_item_title) ;
        TextView price = (TextView) convertView.findViewById(R.id.service_list_item_price) ;
        TextView qty = (TextView) convertView.findViewById(R.id.service_list_item_qty) ;

        ImageButton edit = (ImageButton) convertView.findViewById(R.id.service_list_item_edit);
        ImageButton delete = (ImageButton) convertView.findViewById(R.id.service_list_item_delete);

        ServiceListItem currentItem = data.get(position) ;

        name.setText(currentItem.getName() );
        price.setText(currentItem.getPrice());

        if (currentItem.isUOMAvailable)
        {
            qty.setText(currentItem.getQuantity() + " " + currentItem.getUom() );
            qty.setVisibility(View.VISIBLE);
        }
        else
        {
            qty.setVisibility(View.INVISIBLE);
        }

        edit.setOnClickListener(new IndexedOnClickListener(position) {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(c.getApplicationContext(), EditOffer.class);

                intent.putExtra(EditOffer.KEY_ITEM_NAME, data.get(index).getName()) ;
                intent.putExtra(EditOffer.KEY_ITEM_HASUOM , data.get(index).isUOMAvailable() ) ;
                intent.putExtra(EditOffer.KEY_IS_EDIT_MODE , true ) ;
                intent.putExtra(EditOffer.KEY_OFFER_ID , data.get(index).getId() ) ;
                intent.putExtra(EditOffer.KEY_ITEM_PRICE , data.get(index).getPrice() ) ;
                intent.putExtra(EditOffer.KEY_ITEM_QTY , data.get(index).getQuantity() ) ;
                intent.putExtra(EditOffer.KEY_ITEM_OUM , data.get(index).getUom() ) ;
                intent.putExtra(EditOffer.KEY_INDEX, index) ;

                ((HomeActivity)c).startActivityForResult(intent , 786);
//                ServicesFragment frag = (ServicesFragment) ((HomeActivity)c).getSupportFragmentManager().findFragmentByTag(ServicesFragment.SERVICE_FRAGMENT_TAG);
//                frag.startActivityForResult(intent , 786);

            }
        });

        delete.setOnClickListener(new IndexedOnClickListener(position) {
            @Override
            public void onClick(View v) {

                AsyncGet get = new AsyncGet(c);

                get.setUseCase(AsyncGet.CASE_DELETE);
                List<Pair<String,String>> params = new ArrayList<Pair<String, String>>();
                params.add(new Pair<String, String>( "offerid" , "" + data.get(index).getId())) ;
                get.setParams(params);

                get.setOnRequestCompletedListener(new OnRequestCompleteListener() {
                    @Override
                    public void onRequestSuccessful(String response) {
                        if (response.equals("true"))
                        {
                            Toast.makeText(c , "Removed" , 0 ).show();

                            Runnable runnable = new Runnable() {
                                @Override
                                public void run() {
                                    ServicesFragment frag = (ServicesFragment) ((HomeActivity) c ).getSupportFragmentManager().findFragmentByTag( ServicesFragment.SERVICE_FRAGMENT_TAG );
                                    System.out.println("tried to retrieve fragment by ID");
                                    if(frag != null)
                                    {
                                        System.out.println("Fragment not null");
                                        frag.deleteItem(index);
                                    }
                                }
                            } ;

                            ((HomeActivity) c).runOnUiThread(runnable);

                        }
                        else
                        {
                            Toast.makeText(c , "Failed to remove" , 0 ).show();
                        }
                    }

                    @Override
                    public void onRequestFailed(String error, Exception e) {

                    }
                });

                get.start();

            }
        });


        return convertView ;
    }

    @Override
    public int getItemViewType(int position) {
        return 0;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    public void setData(List<ServiceListItem> data) {
        this.data = data;
        for (int i = 0 ; i < data.size() ; i ++)
        {
            mIds.put(data.get(i) , i) ;
        }
    }

    @Override
    public boolean areAllItemsEnabled() {
        return true;
    }

    @Override
    public boolean isEnabled(int position) {
        return true;
    }

    public void addOrUpdateItem(ServiceListItem item) {
        boolean alreadyExist = false ;
        System.out.println("Add or update. Loop Size : " + data.size());
        for (int i = 0 ; i < data.size() ; i ++)
        {
            Log.d( TAG ,"Comparing " + data.get(i).getId() + " with " + item.getId() );
            if (data.get(i).getId().equals(item.getId()) )
            {
                Log.d( TAG ,"Comparison Successful " );
                alreadyExist = true ;
                ServiceListItem origItem = data.get(i);
                origItem.setPrice(item.getPrice());
                origItem.setName(item.getName());
                origItem.setUom(item.getUom());

                break ;
            }
        }
        if(!alreadyExist)
        {
            data.add(item);
        }
    }

    public void remove(int index) {
        data.remove(index);
    }

    public abstract class IndexedOnClickListener implements View.OnClickListener {

        int index ;

        IndexedOnClickListener(int position)
        {
            index = position ;
        }
    }


}
