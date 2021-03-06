package com.example.ivosilva.nomi.nfc;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.NfcEvent;
import android.nfc.NfcManager;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.example.ivosilva.nomi.R;
import com.example.ivosilva.nomi.login.LoginFragment;
import com.example.ivosilva.nomi.menu.MenuActivity;
import com.example.ivosilva.nomi.volley.CustomJSONObjectRequest;
import com.example.ivosilva.nomi.volley.CustomVolleyRequestQueue;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;

public class NFCShareActivity extends AppCompatActivity {

    Fragment active_fragment = null;
    private NfcAdapter mNfcAdapter;
    private boolean resumed = false;
    private String new_profile;

    private static final String PROFILETOSHARE = "profilekey";
    private static final String PROFILEID = "profileID";
    private static final String REQUEST_TAG = "NFCShareActivity";

    String profile_id;

    SharedPreferences shared_preferences;
    private RequestQueue mQueue;

    private Activity activity = this;
    private Context context = this;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // transition animation from login to NFC Activity
        overridePendingTransition(R.anim.fadein, R.anim.fadeout);

        setContentView(R.layout.activity_nfc);

        NFCEnabled();

    }

    @Override
    public void onDestroy(){
        Crouton.cancelAllCroutons();
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();

        //shared_preferences = getSharedPreferences(PROFILETOSHARE, Context.MODE_PRIVATE);
        //profile_id = shared_preferences.getString(PROFILEID, "-1");

        resumed = true;
        NFCEnabled();
        resumed = false;

    }

    protected void NFCEnabled() {

        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
        mNfcAdapter.setNdefPushMessageCallback(new NfcAdapter.CreateNdefMessageCallback() {

            /*
             * (non-Javadoc)
             * @see android.nfc.NfcAdapter.CreateNdefMessageCallback#createNdefMessage(android.nfc.NfcEvent)
             */
            @Override
            public NdefMessage createNdefMessage(NfcEvent event) {
                NdefRecord message = NdefRecord.createMime("text/plain", profile_id.getBytes());
                return new NdefMessage(new NdefRecord[]{message});
            }

        }, this, this);

        //  let's check if NFC is enabled!
        NfcManager manager = (NfcManager) getApplicationContext().getSystemService(Context.NFC_SERVICE);
        NfcAdapter adapter = manager.getDefaultAdapter();

        if (adapter == null) {
            // Stop here, we definitely need NFC
            Toast.makeText(this, R.string.nfc_not_supported, Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        FragmentManager fm = getSupportFragmentManager();

        if (adapter.isEnabled()) {
            /*
            *   NFC SHOWS LOVE
            */



            if (resumed) {
                PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
                mNfcAdapter.enableForegroundDispatch(this, pendingIntent, null, null);
            }

            if (active_fragment == null) {
                active_fragment = new NFCUpFragment();
                fm.beginTransaction().add(R.id.nfc_fragment_container, active_fragment).commit();
                Log.d("NFCEnabled_true", "add");
            } else {
                active_fragment = new NFCUpFragment();
                fm.beginTransaction().replace(R.id.nfc_fragment_container, active_fragment).commit();
                Log.d("NFCEnabled_true", "replace");
            }
        } else {

            /*
            *   NFC DOESN'T CARE ABOUT US
            */
            if(active_fragment == null){
                active_fragment = new NFCDownFragment();
                fm.beginTransaction().add(R.id.nfc_fragment_container, active_fragment).commit();
                Log.d("NFCEnabled_false", "add");
            }
            else{
                active_fragment = new NFCDownFragment();
                fm.beginTransaction().replace(R.id.nfc_fragment_container, active_fragment).commit();
                Log.d("NFCEnabled_false", "replace");
            }
        }
    }



    @Override
    protected void onNewIntent(Intent intent) {
		/*
		 * This method gets called, when a new Intent gets associated with the current activity instance.
		 * Instead of creating a new activity, onNewIntent will be called. For more information have a look
		 * at the documentation.
		 *
		 * In our case this method gets called, when the user attaches a Tag to the device.
		 */


        Parcelable[] rawMsgs = intent
                .getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
        NdefMessage msg = (NdefMessage) rawMsgs[0];

        byte[] payload = msg.getRecords()[0].getPayload();

        try{

            // THIS GETS THE BEAMED MESSAGE!!! WOHOOOO!
            Log.d("onNewIntent", new String(payload, 0, payload.length, "UTF-8"));

            shared_preferences = getSharedPreferences(PROFILETOSHARE, Context.MODE_PRIVATE);
            profile_id = shared_preferences.getString(PROFILEID, "-1");

            Log.d("onNewIntent", profile_id);

            shared_preferences = getSharedPreferences(LoginFragment.SERVER, Context.MODE_PRIVATE);
            String serverIp = shared_preferences.getString(LoginFragment.SERVERIP, "localhost");


            // Connect the two profiles!
            mQueue = CustomVolleyRequestQueue.getInstance(context).getRequestQueue();
            String url = "http://"+serverIp+"/api/profile/relation/";

            try {

                new_profile = new String(payload, 0, payload.length, "UTF-8");
                JSONObject jsonBody = new JSONObject("{" +
                        "\"profileId2\": " + new_profile + " ," +
                        "\"profileId1\": " + profile_id +
                        "}");

                final CustomJSONObjectRequest jsonRequest = new CustomJSONObjectRequest(Request.Method.PUT, url, jsonBody,
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject jsonObject) {
                                Log.d("onResponse", jsonObject.toString());

                                Intent connected_intent = new Intent(activity, NFCConnectedActivity.class);
                                Bundle b = new Bundle();
                                b.putString("new_connection", jsonObject.toString());
                                b.putString("new_profile", new_profile);
                                connected_intent.putExtras(b);
                                startActivity(connected_intent);
                                finish();
                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError volleyError) {
                                Crouton.makeText(activity, R.string.error_sharing, Style.ALERT).show();
                            }
                        });
                jsonRequest.setTag(REQUEST_TAG);
                mQueue.add(jsonRequest);
            } catch (JSONException e) {
                Log.d("JSONException", e.toString());
            }

        } catch (UnsupportedEncodingException e) {
            Log.e("onNewIntent", e.toString());
        }

    }
}
