package com.saami.app.testingfastandroidnetworking;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import okhttp3.Authenticator;
import okhttp3.Credentials;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.Route;

public class MainActivity extends AppCompatActivity {


    ArrayList<String> datanama = new ArrayList<String>();

    private TextView textResponse;
    private ProgressBar progress;
    void DesignInitialize(){
        progress = findViewById(R.id.progressBar);
        progress.setVisibility(View.GONE);
        textResponse = findViewById(R.id.text_response);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        DesignInitialize();
        OkHttpClient okHttpClient = new OkHttpClient().newBuilder()
                .authenticator(new Authenticator() {
                    @Override
                    public Request authenticate(Route route, Response response) throws IOException {
                        String credential = Credentials.basic("admin", "Admin1234");
                        return response.request().newBuilder().header("Authorization", credential).build();
                    }
                })
                .build();
        AndroidNetworking.initialize(getApplicationContext(),okHttpClient);
        startGetData();


    }

    void startGetData(){
        progress.setVisibility(View.VISIBLE);
        AndroidNetworking.get("http://dev.3guru.com:1558/BC150HRA/ODataV4/Company('HRA Group Development')/CausesOfAbsence")
                .addQueryParameter("$select","Code, Description")
                .setTag(this)
                .setPriority(Priority.LOW)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        progress.setVisibility(View.GONE);

                        try {
                            JSONArray data = response.getJSONArray("value");
                            for(int i = 0;i < data.length(); i++){
                                JSONObject datas = data.getJSONObject(i);
                                String name = datas.optString("Description");
                                datanama.add(name);
                            }
                            textResponse.setText(datanama.toString().replace("[","").replace("]",""));
                        } catch (JSONException e) {
                            e.printStackTrace();
                            System.out.println("ParsingError : "+e.toString());
                        }
                    }

                    @Override
                    public void onError(ANError anError) {
                        progress.setVisibility(View.GONE);
                        if (anError.getErrorCode() != 0) {
                            // received error from server
                            textResponse.setText(anError.getErrorBody()+"\n"+anError.getErrorDetail());
                        } else {
                            // error.getErrorDetail() : connectionError, parseError, requestCancelledError
                            textResponse.setText(anError.getErrorCode()+"\n"+anError.getErrorBody()+"\n"+anError.getErrorDetail());
                        }
                    }
                });
    }
}
