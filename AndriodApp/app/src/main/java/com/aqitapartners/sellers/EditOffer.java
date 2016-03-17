package com.aqitapartners.sellers;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Pair;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class EditOffer extends AppCompatActivity {

    public static final String KEY_IS_EDIT_MODE = "isedit" ;
    public static final String KEY_OFFER_ID = "offerid" ;
    public static final String KEY_ITEM_NAME = "itemname" ;
    public static final String KEY_ITEM_HASUOM = "hasuom" ;
    public static final String KEY_ITEM_PRICE = "price" ;
    public static final String KEY_ITEM_QTY = "qty" ;
    public static final String KEY_ITEM_OUM = "oum" ;
    public static final String KEY_INDEX = "index" ;
    public static final String RETURN_NAME = "returnname" ;
    public static final String RETURN_QTY = "returnqty" ;
    public static final String RETURN_UOM = "returnuom" ;
    public static final String RETURN_PRICE = "returnprice" ;
    public static final String RETURN_ID = "returnid" ;
    public static final String RETURN_INDEX = "returnindex" ;
    public static final String RETURN_HASUOM = "returnhasuom" ;
    private static final String RETURN_ISEDITMODE = "returniseditmode" ;

    EditText nameEdit ;
    EditText qtyEdit ;
    EditText priceEdit ;
    Spinner uomSelector;

    String mName, mQty , mPrice, mUOM, mId ;

    boolean mIsEditMode, mHasOUM ;

    Context c ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_offer);

        ActionBar mActionBar = getSupportActionBar();
        mActionBar.setDisplayHomeAsUpEnabled(true);
        mActionBar.setHomeButtonEnabled(true);

        Intent intent = getIntent();
        mId = intent.getStringExtra(KEY_OFFER_ID);
        mHasOUM = intent.getBooleanExtra(KEY_ITEM_HASUOM, false) ;
        mIsEditMode = intent.getBooleanExtra(KEY_IS_EDIT_MODE, false) ;
        mName = intent.getStringExtra(KEY_ITEM_NAME) ;
        mPrice = intent.getStringExtra(KEY_ITEM_PRICE) ;
        mQty = intent.getStringExtra(KEY_ITEM_QTY) ;
        mUOM = intent.getStringExtra(KEY_ITEM_OUM) ;

        nameEdit = (EditText) findViewById(R.id.item_edit_name);
        qtyEdit = (EditText) findViewById(R.id.item_edit_quantity) ;
        priceEdit = (EditText) findViewById(R.id.item_edit_price) ;

        if (mHasOUM)
        {
            ((View)qtyEdit.getParent()).setVisibility(View.VISIBLE);
        }
        else
        {
            ((View)qtyEdit.getParent()).setVisibility(View.GONE);
        }

        uomSelector = (Spinner) findViewById(R.id.item_edit_uom_selector);
        uomSelector.setAdapter(ArrayAdapter.createFromResource(this, R.array.oums, android.R.layout.simple_list_item_1)) ;

        if (mIsEditMode)
        {
            nameEdit.setText( mName );
            priceEdit.setText( mPrice );
            uomSelector.setSelection(1);
        }

        Button submit = (Button) findViewById(R.id.edit_submit) ;

        c = this ;

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String name = nameEdit.getText().toString();
                String price = priceEdit.getText().toString();
                if (!name.isEmpty() & !price.isEmpty())
                {
                    try
                    {
                        Float actualPrice = Float.parseFloat(price);
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                        Toast.makeText(c , "Enter Numeric Price" , 0 ).show();
                        return ;
                    }
                }
                else
                {
                    Toast.makeText(c , "Please enter values" , 0 ).show();
                }


                AsyncGet get = new AsyncGet(c) ;
                get.setUseCase(AsyncGet.CASE_UPDATE);

                List<Pair<String,String>> params = new ArrayList<Pair<String, String>>();

                UserInfoHandler infoFetcher = UserInfoHandler.getInstance(c);


                mName = nameEdit.getText().toString() ;
                mPrice = priceEdit.getText().toString() ;

                params.add(new Pair<String, String>("storeid", infoFetcher.getStoreId() )) ;
                params.add(new Pair<String, String>("name", mName )) ;
                params.add(new Pair<String, String>("price", mPrice )) ;

                if (mIsEditMode)
                {
                    params.add(new Pair<String, String>("offerid" , mId)) ;
                }
                else
                {
                    params.add(new Pair<String, String>("qty" , qtyEdit.getText().toString() )) ;
                    params.add(new Pair<String, String>("uom" , (String) uomSelector.getSelectedItem() )) ;
                }

                get.setOnRequestCompletedListener(new OnRequestCompleteListener() {
                    @Override
                    public void onRequestSuccessful(String response) {
                        response.trim();

                        if (mIsEditMode) {
                            if (response.contains("true")) {

                                Toast.makeText(c, "Updated", 0).show();

                                Intent result = new Intent();
                                result.putExtra(RETURN_NAME , mName ) ;
                                result.putExtra(RETURN_ID , mId ) ;
                                result.putExtra(RETURN_PRICE , mPrice ) ;
                                result.putExtra(RETURN_QTY , mQty ) ;
                                result.putExtra(RETURN_UOM , mUOM ) ;
                                result.putExtra(RETURN_HASUOM, mHasOUM) ;
                                result.putExtra(RETURN_ISEDITMODE , mIsEditMode );
                                setResult(0, result);
                                finish();

                            } else {
                                Toast.makeText(c, "Something went wrong please try later", 0).show();
                            }
                        } else {
                            System.out.println("Creat mode");
                            if (response.contains("false")) {
                                Toast.makeText(c, "Something went wrong, Please try later", 0).show();
                            } else {

                                Toast.makeText(c, "Updated", 0).show();

                                mId = response ;
                                mId = mId.replaceAll("\"" , "");
                                System.out.println("Id created = " + mId );

                                Intent result = new Intent();
                                result.putExtra(RETURN_NAME , mName ) ;
                                result.putExtra(RETURN_ID , mId ) ;
                                result.putExtra(RETURN_PRICE , mPrice ) ;
                                result.putExtra(RETURN_QTY , mQty ) ;
                                result.putExtra(RETURN_UOM , mUOM ) ;
                                result.putExtra(RETURN_HASUOM, mHasOUM) ;
                                result.putExtra(RETURN_ISEDITMODE, mIsEditMode);
                                setResult(0, result);
                                finish();
                            }
                        }

                    }

                    @Override
                    public void onRequestFailed(String error, Exception e) {

                        ((Activity) c).runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(c, "Something went wrong, Please try later", 0).show();
                            }
                        });
                    }
                });

                get.setParams(params);
                get.start();



            }
        });
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
