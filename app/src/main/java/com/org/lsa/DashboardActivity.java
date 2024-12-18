package com.org.lsa;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.eze.api.EzeAPI;

import android.app.ActivityOptions;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.TextPaint;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.navigation.NavigationView;
import com.org.lsa.adapter.ListViewAdapter;
import com.org.lsa.custom.NetworkHandler;
import com.org.lsa.custom.Utility;
import com.org.lsa.custom.WsUtility;
import com.org.lsa.model.DataModal;
import com.org.lsa.web_services.RetrofitAPI;

import org.json.JSONObject;
import org.json.JSONArray;
import org.json.JSONException;

import java.io.ByteArrayOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.TimeZone;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DashboardActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    private Toolbar toolbar;
    public DrawerLayout mDrawer;
    private TextView app_version;
    private ImageView btn_qrScanner;
    private ActionBarDrawerToggle mDrawerToggle;
    private Button continue_page;
    private Button day_page;
//    private Button print_reciept;
    public TextView grandCount;
    public TextView totalCount;
    public TextView residentApartCount;
    public TextView residentIndCount;

    //Commercial
    public TextView tv_commercial_total_properties;
    public TextView tv_commercial_total_demand;
    public TextView tv_commercial_total_pending;
    public TextView tv_commercial_total_collected;

    //Residential
    public TextView tv_residential_total_properties;
    public TextView tv_residential_total_demand;
    public TextView tv_residential_total_pending;
    public TextView tv_residential_total_collected;

    private final int REQUEST_CODE_INITIALIZE = 10001;
    private final int REQUEST_CODE_PREPARE = 10002;

    private Spinner spinnerZone;
    private Spinner spinnerWard;
    private HashMap<String, String> zoneMap;
    private HashMap<String, String> wardMap;
    private String api_key = "http://45.114.246.201:6005"; // Replace with your actual API key
    private String wardId = "";
    private String zoneId = "";

    private String wardName = "";
    private String zoneName = "";
    private boolean hasSummery = false;

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        Toolbar toolbar2 = findViewById(R.id.toolbar);
        this.toolbar = toolbar2;
        setSupportActionBar(toolbar2);

        getSupportActionBar().setTitle((CharSequence) "Lucknow Swachhata Abhiyan");

        spinnerZone = findViewById(R.id.spinner_zone);
        spinnerWard = findViewById(R.id.spinner_ward);

        zoneMap = new HashMap<>();
        wardMap = new HashMap<>();

        fetchZoneDetails();

        progressDialog = new ProgressDialog(DashboardActivity.this);
        progressDialog.setMessage("Loading...");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setCancelable(false);
        progressDialog.show();

        spinnerZone.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedZone = parent.getItemAtPosition(position).toString();

                if(selectedZone.equals("Select zone")){
                    zoneName = "Selected";
                    zoneId = "-99";
                    emptyWards();
                    clearData();
                }

                if (!selectedZone.equals("Select zone")) {
                    zoneName = selectedZone;
                    zoneId = zoneMap.get(selectedZone);
                    if (zoneId != null) {
                        fetchWardsForZone(zoneId);
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        emptyWards();

        spinnerWard.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedWard = parent.getItemAtPosition(position).toString();

//                Log.d("Location", "Selected ward " + selectedWard);
//                Log.d("Location", "Selected ward map " + wardMap);

                if(selectedWard.equals("Select ward")){
                    wardName = "Selected";
                    wardId = "-99";
                    clearData();
                }

                if (!selectedWard.equals("Select ward")) {
                    wardName = selectedWard;
                    wardId = wardMap.get(selectedWard);
//                    Log.d("Location", "Selected ward id " + wardId);
                    if (wardId != null) {
//                        Log.d("Location", wardId);
                        getSummeryForWard(zoneId,wardId);
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        LogcatHelper.captureLogs(this, "all_logs.txt");

        this.mDrawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerToggle = new ActionBarDrawerToggle(
                this, this.mDrawer, this.toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close) {
            public void onDrawerClosed(View v) {
                super.onDrawerClosed(v);
            }

            public void onDrawerOpened(View v) {
                super.onDrawerOpened(v);
            }
        };
        this.mDrawer.addDrawerListener(mDrawerToggle);
        mDrawerToggle.syncState();

        this.btn_qrScanner = findViewById(R.id.btn_qrScanner);
        TextView textView = findViewById(R.id.appVersion);
        this.app_version = textView;

        this.btn_qrScanner.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                spinnerZone.setSelection(0);
                clearData();
                emptyWards();
            }
        });
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(DashboardActivity.this);
        navigationView.getMenu().getItem(0).setChecked(true);
        /*this.listView =  findViewById(R.id.left_drawer_list);
        ListViewAdapter listViewAdapter = new ListViewAdapter(this, new String[]{"Dashboard", "Logout"}, new int[]{R.drawable.dashboard, R.drawable.ic_logout});
        this.viewAdapter = listViewAdapter;
        this.listView.setAdapter(listViewAdapter);
        this.listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                mDrawer.closeDrawer(GravityCompat.START);
                FragmentManager fm = DashboardActivity.this.getSupportFragmentManager();
                for (int i = 0; i < fm.getBackStackEntryCount(); i++) {
                    fm.popBackStack();
                }
                empLogin(position);
            }
        });*/

        TextView versionTextView = findViewById(R.id.version_text);
        try {
            // Get version code or version name from package info
            PackageInfo packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            String versionName = packageInfo.versionName;

            // Set version text
            versionTextView.setText("Version: " + versionName);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }




//        this.linearCollectedAmount = findViewById(R.id.linear_collected_amount);
//        this.linearCollectedAmount.setOnClickListener(new View.OnClickListener() {
//            public void onClick(View v) {
//                callAmountCollection("1");
//
//            }
//        });
        this.continue_page = findViewById(R.id.continue_page);
        this.day_page=findViewById(R.id.day_page);
        this.continue_page.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                callAmountCollection("0");
               /* SharedPreferences.Editor editor = Utility.getSharedPreferences(DashboardActivity.this).edit();
                editor.putString("Amount_Collection_Selection", "0");
                editor.commit();
                Intent intent = new Intent(getApplicationContext(), AmountCollection.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);*/
            }
        });

        this.day_page.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callDayendCollection("0");
            }
        });

//        this.print_reciept = findViewById(R.id.print_receipt);
//        this.print_reciept.setOnClickListener(new View.OnClickListener() {
//            public void onClick(View v) {
//                //Function call of print receipt
//                callPrintFun("Test123","LSM1234","1","Sarojini naidu","12/34","Shiva","1234567899","Total address","QR code","100.00","Nandini");
//            }
//        });

        this.grandCount = (TextView) findViewById(R.id.grandTotal_count);
        this.totalCount = (TextView) findViewById(R.id.todayTotal_count);
        this.residentIndCount = (TextView) findViewById(R.id.rsIndividual_count);
        this.residentApartCount = (TextView) findViewById(R.id.rsApartment_count);

        //Commercial
        this.tv_commercial_total_properties = (TextView) findViewById(R.id.tv_commercial_total_properties);
        this.tv_commercial_total_demand= (TextView) findViewById(R.id.tv_commercial_total_demand);
        this.tv_commercial_total_pending= (TextView) findViewById(R.id.tv_commercial_total_pending);
        this.tv_commercial_total_collected= (TextView) findViewById(R.id.tv_commercial_total_collected);

        //Residential
        this.tv_residential_total_properties = (TextView) findViewById(R.id.tv_residential_total_properties);
        this.tv_residential_total_demand= (TextView) findViewById(R.id.tv_residential_total_demand);
        this.tv_residential_total_pending= (TextView) findViewById(R.id.tv_residential_total_pending);
        this.tv_residential_total_collected= (TextView) findViewById(R.id.tv_residential_total_collected);


        try {
//            String resoonce_loan = new putSurveyUserRegistration().execute().get();
//            String resoonce_loan = new ExecuteTask().execute().get();

//            Log.e("DashBoard","response: "+resoonce_loan);
           // postData();

            // JSONObject jsonObject = new JSONObject(resoonce_loan) ;

            // Log.e("DashBoard","response StatusCode: "+jsonObject.get("StatusCode"));
            //  Log.e("DashBoard","response StatusMessage: "+jsonObject.get("StatusMessage"));

        } catch (Exception e) {
            e.printStackTrace();
        }

        initializeSDK();
        doPrepareDeviceEzeTap();

    }

    private int getSpinnerPosition(Spinner spinner, String value) {
        ArrayAdapter<String> adapter = (ArrayAdapter<String>) spinner.getAdapter();
        return adapter.getPosition(value);
    }



    private void emptyWards(){
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, Collections.singletonList("Select ward"));
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerWard.setAdapter(adapter);
    }

    private void initializeSDK() {
        Log.d("Log", "Called initialization");
        JSONObject jsonRequest = new JSONObject();
        try {
            jsonRequest.put("demoAppKey", "80504210-c4a6-4678-bd7a-b3f5aaca2ffb");
            jsonRequest.put("prodAppKey", "51cf6003-3380-47ed-86e1-5f81ffbe586f");
            jsonRequest.put("merchantName", "NAGAR_NIGAM_LKO");
            jsonRequest.put("userName", "0904202400");
//            jsonRequest.put("userName", "9718775851");
            jsonRequest.put("currencyCode", "INR");
            jsonRequest.put("appMode", "PROD");
            jsonRequest.put("captureSignature", "false");
            jsonRequest.put("prepareDevice", "false");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.d("Log", "json created " + jsonRequest);
        Log.d("Log", "Called init");
        EzeAPI.initialize(this, REQUEST_CODE_INITIALIZE, jsonRequest);
        Log.d("Log", "Ended init");
    }

    private void doPrepareDeviceEzeTap(){
        EzeAPI.prepareDevice(this, REQUEST_CODE_PREPARE);
    }

    private void fetchZoneDetails() {
        //RegisterActivity registerActivity = new RegisterActivity();
        //String secretKey = registerActivity.retrieveStaticKey();
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    URL url = new URL(api_key + "/api/PaymentsDetails/GetAllCircles");
                    HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                    //String authString = "patna:patna#2020";
                    //String encodeToString = Base64.encodeToString(authString.getBytes(StandardCharsets.UTF_8), Base64.NO_WRAP);

//                    Log.d("API", url.toString());
                    //httpURLConnection.setRequestProperty("Authorization", "Basic " + encodeToString);
                    httpURLConnection.setRequestProperty("Accept", "*/*");
                    httpURLConnection.setRequestMethod("GET");
                    httpURLConnection.setDoOutput(false);


                    int responseCode = httpURLConnection.getResponseCode();
//                    Log.d("API", "response code: " + responseCode);

                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        StringBuilder response = new StringBuilder();
                        InputStreamReader inputStreamReader = new InputStreamReader(httpURLConnection.getInputStream());
                        int read;
                        while ((read = inputStreamReader.read()) != -1) {
                            response.append((char) read);
                        }
                        inputStreamReader.close();

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                progressDialog.dismiss();
                                parseAndPopulateDropdowns(response.toString());
                            }
                        });
                    } else {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                clearData();
                                progressDialog.dismiss();
                                Toast.makeText(DashboardActivity.this, "Failed to fetch zones", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            clearData();
                            progressDialog.dismiss();
                            Toast.makeText(DashboardActivity.this, "Error occurred: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });
    }

    private void parseAndPopulateDropdowns(String jsonData) {
        try {
            List<String> zoneList = new ArrayList<>();
            zoneList.add("Select zone");

            JSONArray jsonArray = new JSONArray(jsonData.replace("\\\\", ""));
            if (jsonArray.length() <= 0) {
                clearData();
                Toast.makeText(this, "No Zone Data!", Toast.LENGTH_SHORT).show();
                return;
            }

            zoneMap.clear();

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                String zoneId = jsonObject.getString("uccZoneId");
                String zoneName = jsonObject.getString("zoneName");
                List<String> notNeeded = new ArrayList<>(Arrays.asList("2", "5", "8"));
//                List<String> notNeeded = new ArrayList<>(Arrays.asList("5", "8")); //Testing
                if(!notNeeded.contains(zoneId)){
                    zoneMap.put(zoneName, zoneId);
                    zoneList.add(zoneName);
                }
            }

            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, zoneList);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerZone.setAdapter(adapter);
        } catch (Exception e) {
            e.printStackTrace();
            clearData();
            Toast.makeText(this, "Failed to parse data", Toast.LENGTH_SHORT).show();
        }

        Intent intent = getIntent();
        String savedZoneName = intent.getStringExtra("zoneName");
        String savedWardName = intent.getStringExtra("wardName");

        if (savedZoneName != null && savedWardName != null) {
            // Set the zone spinner
            int zonePosition = getSpinnerPosition(spinnerZone, savedZoneName);
            spinnerZone.setSelection(zonePosition);


            // Fetch wards and set the ward spinner after wards are loaded
            zoneId = zoneMap.get(savedZoneName);
            fetchWardsForZone(zoneId);
        }
    }

    private void fetchWardsForZone(final String circleId) {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    URL url = new URL(api_key + "/api/PaymentsDetails/GetWardsForCircle/"+circleId);
                    HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                    //String authString = "patna:patna#2020";
                    //String encodeToString = Base64.encodeToString(authString.getBytes(StandardCharsets.UTF_8), Base64.DEFAULT);

//                    httpURLConnection.setRequestProperty("Accept-Charset", "UTF-8");
                    httpURLConnection.setRequestProperty("Accept", "*/*");
                    httpURLConnection.setRequestMethod("GET");
                    httpURLConnection.setDoOutput(false);


                    int responseCode = httpURLConnection.getResponseCode();
                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        StringBuilder response = new StringBuilder();
                        InputStreamReader inputStreamReader = new InputStreamReader(httpURLConnection.getInputStream());
                        int read;
                        while ((read = inputStreamReader.read()) != -1) {
                            response.append((char) read);
                        }
                        inputStreamReader.close();

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                parseWardsResponse(response.toString());
                            }
                        });
                    } else {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                clearData();
                                Toast.makeText(DashboardActivity.this, "Failed to fetch wards", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            clearData();
                            Toast.makeText(DashboardActivity.this, "Error occurred: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });
    }

    private void parseWardsResponse(String responseString) {
        try {
            List<String> wardList = new ArrayList<>();
            wardList.add("Select ward");

//            Log.d("Location", "Wards string " + responseString);
            JSONArray jsonArray = new JSONArray(responseString.replace("\\\\", ""));
            if (jsonArray.length() <= 0) {
                Toast.makeText(this, "No Ward Data!", Toast.LENGTH_SHORT).show();
                return;
            }

            wardMap.clear();

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
//                int wardId = jsonObject.getInt("wardId");
                int wardId = jsonObject.getInt("wardNo");
                String wardName = jsonObject.getInt("wardNo") +" - "+ jsonObject.getString("wardName");
                wardMap.put(wardName, String.valueOf(wardId));
                wardList.add(wardName);
            }

            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, wardList);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerWard.setAdapter(adapter);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to parse wards data", Toast.LENGTH_SHORT).show();
            clearData();
        }

        Intent intent = getIntent();
        String savedZoneName = intent.getStringExtra("zoneName");
        String savedWardName = intent.getStringExtra("wardName");

        if (savedZoneName != null && savedWardName != null) {
            int wardPosition = getSpinnerPosition(spinnerWard, savedWardName);
            spinnerWard.setSelection(wardPosition);

            // Fetch the summary for the saved ward
            wardId = wardMap.get(savedWardName);
            if (zoneId != null && wardId != null) {
                getSummeryForWard(zoneId, wardId);
            }
        }
    }

    private void getSummeryForWard(final String circleId,final String wardNo) {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    URL url = new URL(api_key + "/api/PaymentsDetails/GetWardSummary?CircleId=" + circleId + "&wardno=" + wardNo);
                    HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                    httpURLConnection.setRequestProperty("Accept", "*/*");
                    httpURLConnection.setRequestMethod("GET");
                    httpURLConnection.setDoOutput(false);


                    int responseCode = httpURLConnection.getResponseCode();
                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        StringBuilder response = new StringBuilder();
                        InputStreamReader inputStreamReader = new InputStreamReader(httpURLConnection.getInputStream());
                        int read;
                        while ((read = inputStreamReader.read()) != -1) {
                            response.append((char) read);
                        }
                        inputStreamReader.close();

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                hasSummery = true;
                                parseSummery(response.toString());
                            }
                        });
                    } else {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                hasSummery = false;
                                Toast.makeText(DashboardActivity.this, "Failed to summery for provided Zone:"+zoneName+", Ward:"+wardName, Toast.LENGTH_SHORT).show();
                                clearData();
                            }
                        });
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            clearData();
                            Toast.makeText(DashboardActivity.this, "Error occurred: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });
    }

    private void clearData(){
        this.grandCount.setText("");
        this.totalCount.setText("");
        this.residentApartCount.setText("");
        this.residentIndCount.setText("");

        //Commercial
        this.tv_commercial_total_properties.setText("Total Properties: "+0);
        this.tv_commercial_total_demand.setText("Total Demand: "+0.0);
        this.tv_commercial_total_pending.setText("Total Pending: "+0.0);
        this.tv_commercial_total_collected.setText("Total Collected: "+0.0);

        //Residential
        this.tv_residential_total_properties.setText("Total Properties: "+0);
        this.tv_residential_total_demand.setText("Total Demand: "+0.0);
        this.tv_residential_total_pending.setText("Total Pending: "+0.0);
        this.tv_residential_total_collected.setText("Total Collected: "+0.0);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Replace `circleId` and `wardNo` with the actual variables or values you want to use
        getSummeryForWard(zoneId, wardId);  // Fetch latest summary data whenever screen is visible
    }



    private void parseSummery(String jsonResponse) {
        try {
            // Assuming the response is a JSON array of objects with properties
            JSONArray jsonArray = new JSONArray(jsonResponse);

            // Iterate through the array and parse each object's properties
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject obj = jsonArray.getJSONObject(i);

                //Properties
                int totalCommProperties = obj.getInt("comm_properties");
                int totalResiProperties = obj.getInt("resi_properties");
                int totalProperties = totalResiProperties+totalCommProperties;

                //Demand
                double totalCommDemand = obj.getDouble("comm_demand");
                double totalResiDemand = obj.getDouble("resi_demand");
                double totalDemand = totalResiDemand+totalCommDemand;

                //Outstanding
                double totalCommOutstanding = obj.getDouble("comm_outstanding");
                double totalResiOutstanding = obj.getDouble("resi_outstanding");
                double totalOutstanding = totalResiOutstanding+totalCommOutstanding;

                System.out.println("Checking "+totalDemand+ " "+totalOutstanding);

                //Total
                this.grandCount.setText(Integer.toString(totalProperties));
                this.totalCount.setText(Double.toString(totalDemand));
                this.residentApartCount.setText(Double.toString(totalOutstanding));
                this.residentIndCount.setText(Double.toString((totalDemand-totalOutstanding)));

                //Commercial
                this.tv_commercial_total_properties.setText("Total Properties: "+totalCommProperties);
                this.tv_commercial_total_demand.setText("Total Demand: "+totalCommDemand);
                this.tv_commercial_total_pending.setText("Total Pending: "+totalCommOutstanding);
                this.tv_commercial_total_collected.setText("Total Collected: "+(totalCommDemand-totalCommOutstanding));

                //Residential
                this.tv_residential_total_properties.setText("Total Properties: "+totalResiProperties);
                this.tv_residential_total_demand.setText("Total Demand: "+totalResiDemand);
                this.tv_residential_total_pending.setText("Total Pending: "+totalResiOutstanding);
                this.tv_residential_total_collected.setText("Total Collected: "+(totalResiDemand-totalResiOutstanding));
            }
        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(DashboardActivity.this, "Failed to parse data", Toast.LENGTH_SHORT).show();
        }
    }


    private void callAmountCollection(String flag) {

        if(zoneId.equals("-99") || wardId.equals("-99") || zoneId.isEmpty() || wardId.isEmpty() || zoneName.equals("Selected") || wardName.equals("Selected") || zoneName.isEmpty() || wardName.isEmpty()){
            Toast.makeText(DashboardActivity.this, "Please select zone and ward", Toast.LENGTH_SHORT).show();
            return;
        }

        if(!hasSummery){
            Toast.makeText(DashboardActivity.this, "No details available for selected zone and ward", Toast.LENGTH_SHORT).show();
            return;
        }

        SharedPreferences.Editor editor = Utility.getSharedPreferences(DashboardActivity.this).edit();
        editor.putString("Amount_Collection_Selection", flag);
        editor.commit();
        Intent intent = new Intent(getApplicationContext(), AmountCollection.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("zoneId", zoneId);
        intent.putExtra("wardId", wardId);
        intent.putExtra("zoneName", zoneName);
        intent.putExtra("wardName", wardName);
        startActivity(intent);
    }

    private void callDayendCollection(String flag) {

        if(zoneId.equals("-99") || wardId.equals("-99") || zoneId.isEmpty() || wardId.isEmpty() || zoneName.equals("Selected") || wardName.equals("Selected") || zoneName.isEmpty() || wardName.isEmpty()){
            Toast.makeText(DashboardActivity.this, "Please select zone and ward", Toast.LENGTH_SHORT).show();
            return;
        }

        if(!hasSummery){
            Toast.makeText(DashboardActivity.this, "No details available for selected zone and ward", Toast.LENGTH_SHORT).show();
            return;
        }

        SharedPreferences.Editor editor = Utility.getSharedPreferences(DashboardActivity.this).edit();
        editor.putString("Day_Collection_Selection", flag);
        editor.commit();
        Intent intent = new Intent(getApplicationContext(), DayReport.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("zoneId", zoneId);
        intent.putExtra("wardId", wardId);
        intent.putExtra("zoneName", zoneName);
        intent.putExtra("wardName", wardName);
        startActivity(intent);
    }

    /*@Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }*/


    /* access modifiers changed from: private */
    public void empLogin(int position) {
        if (position == 0) {
            Intent intent = new Intent(this, DashboardActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent, ActivityOptions.makeCustomAnimation(this, R.anim.fabe, R.anim.fabe).toBundle());
        } else if (position == 1) {
            logoutDetails();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_main_drawer, menu);
        return false;
        /* How to remove three dots from the toolbar of navigation drawer
        In your Activity in onCreateOptionsMenu, just make it return false */
    }

    public void logoutDetails() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setIcon(R.drawable.app_logos);
        builder.setTitle("Confirm");
        builder.setMessage("Are you sure want to logout from application?");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                SharedPreferences.Editor editor = Utility.getSharedPreferences(DashboardActivity.this).edit();
                editor.remove("status");
                editor.apply();

                Intent navigattoLogin = new Intent(DashboardActivity.this, Login.class);
                navigattoLogin.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);//67108864
                startActivity(navigattoLogin);
                finish();
                // DashboardActivity.this.clearAllPrefernces();
                // DashboardActivity.this.clearLoginPrefernces();
            }
        });
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                DashboardActivity.this.getSupportActionBar().setTitle((CharSequence) "Lucknow Swachhata Abhiyan ");
                dialog.dismiss();
            }
        });
        builder.create().show();
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try {
            String responseString = data.getStringExtra("response");
            JSONObject response = new JSONObject(responseString);

            switch (requestCode) {
                case REQUEST_CODE_INITIALIZE:
                    handleInitializeResponse(response);
                    break;

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void showToastMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void handleInitializeResponse(JSONObject response) {
//        Log.d("Log", "Initialization done");
//        JSONObject result = response.optJSONObject("result");
//        String message = (result != null) ? result.optString("message", "Initialization failed") : "Initialization failed";
//        showAlert("Initialization", message);
        Toast.makeText(getApplicationContext(), "Initialization of Razorpay device is done", Toast.LENGTH_SHORT).show();
    }

    private void showAlert(String title, String message) {
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("OK", null)
                .show();
    }

    public void onBackPressed() {
        if (this.mDrawer.isDrawerOpen(GravityCompat.START)) {
            this.mDrawer.closeDrawer(GravityCompat.START);
        } else {
            logoutDetails();
            finish();
//            new AlertDialog.Builder(this).setTitle("Logout ..!!! ").setMessage("Are you sure, you want to logout ?").setPositiveButton("YES", new DialogInterface.OnClickListener() {
//                public void onClick(DialogInterface dialog, int which) {
//                    Intent setIntent = new Intent("android.intent.action.MAIN");
//                    setIntent.addCategory("android.intent.category.HOME");
//                    setIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);//268435456
//                    DashboardActivity.this.startActivity(setIntent);
//                    finish();
//                }
//            }).setNegativeButton("NO", (DialogInterface.OnClickListener) null).setIcon(R.drawable.app_logos).show();

        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
      /*  if (id == R.id.action_sync) {
            return true;
        } else if (id == R.id.action_logout) {
            Utility.callSignOutAlert(MainActivity.this);
            return true;
        }*/

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_home) {
            empLogin(0);
            // Handle the camera action
        } else if (id == R.id.nav_logout) {
            empLogin(1);
        } /* else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }*/

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public class putSurveyUserRegistration extends AsyncTask<Void, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            // progressDialog.show();

        }

        @RequiresApi(api = Build.VERSION_CODES.N)
        @Override
        protected String doInBackground(Void... params) {
            String response = null;
            try {

                /*JSONArray arr_famil_mem = new JSONArray();
                HashMap<String, JSONObject> map = new HashMap<String, JSONObject>();
                for (int i = 1; i <= 1; i++) {*/
                // Bitmap bit=bitmaps.get(i);
                JSONObject jsonne = new JSONObject();

                /*jsonne.put("userId", "33");
                jsonne.put("surveyDetailId", "2284");
                jsonne.put("surveySubDetailId", "1");
                jsonne.put("phoneNumber", "1234567890");// 04805*/

                /*jsonne.put("sdId", Integer.parseInt(surveyDataModelSelectedList.get(0).getSDId()));
                jsonne.put("zoneId", Integer.parseInt(surveyDataModelSelectedList.get(0).getZoneId()));
                jsonne.put("zoneNo", surveyDataModelSelectedList.get(0).getZoneNo());
                jsonne.put("wardNo", surveyDataModelSelectedList.get(0).getWardNo());
                jsonne.put("establishmentTypeId", Integer.parseInt(surveyDataModelSelectedList.get(0).getEstablishmentTypeId()));
                jsonne.put("ownerShipTypeId", Integer.parseInt(surveyDataModelSelectedList.get(0).getOwnerShipTypeId()));
                jsonne.put("houseNo", surveyDataModelSelectedList.get(0).getHouseNo());
                jsonne.put("tagId", surveyDataModelSelectedList.get(0).getTagId());
                jsonne.put("amount_collected", Integer.valueOf(balance_amt_entry_txt.getText().toString().trim()));
//                jsonne.put("collected_date", "2024-07-08T06:45:10.945Z");
                jsonne.put("collected_date", Utility.getDateTime_DD_MM_YYYY());
                jsonne.put("collected_period", selectedSubDivisionString);
                jsonne.put("receipt_number", payment_reference_no_entry_edt.getText().toString().trim());
                jsonne.put("receipt_img", "");
                jsonne.put("sync_server", true);
                jsonne.put("sync_date", "2024-07-08T06:45:10.945Z");*/
//                jsonne.put("sync_date", Utility.getDateTime_DD_MM_YYYY());

                jsonne.put("sdId", 1345);
                jsonne.put("zoneId", 1);
                jsonne.put("zoneNo", "Test");
                jsonne.put("wardNo", "ward");
                jsonne.put("establishmentTypeId", 23);
                jsonne.put("ownerShipTypeId", 54);
                jsonne.put("houseNo", "2-2-11");
                jsonne.put("tagId", "T123");
                jsonne.put("amount_collected", 10);
                jsonne.put("collected_date", "2024-07-08T06:45:10.945Z");
                jsonne.put("collected_period", "123");
                jsonne.put("receipt_number", "1234e");
                jsonne.put("receipt_img", "");
                jsonne.put("sync_server", true);
                jsonne.put("sync_date", "2024-07-08T06:45:10.945Z");

                    /* map.put("json" + i, jsonne);
                    arr_famil_mem.put(map.get("json" + i));
                }*/

                if (Utility.showLogs == 0) {
                    Log.i("jsonne string", jsonne.toString().trim());
                }

                response = WsUtility.executePostHttps(
                        main_url + urlReg,
                        jsonne.toString().trim(), "POST");
            } catch (Exception e) {
                //progressDialog.dismiss();
                return null;
            }
            // progressDialog.dismiss();
            return response;
        }

        @Override
        protected void onPostExecute(final String resp) {
            super.onPostExecute(resp);
            // progressDialog.dismiss();
        }

    }

    //    private String main_url = "http://ramkyapi.beulahsoftware.com/api/PaymentsDetails/", urlReg = "CustomerPaymentDetails";
    private String main_url = "https://ramkyapi.beulahsoftware.com/api/PaymentsDetails/", urlReg = "CustomerPaymentDetails";

    /*class ExecuteTask extends AsyncTask<String, Integer, String>
    {

        @Override
        protected String doInBackground(String... params) {

            String res=PostData(params);

            return res;
        }

        @Override
        protected void onPostExecute(String result) {
          //  progressBar.setVisibility(View.GONE);
            //progess_msz.setVisibility(View.GONE);
            Toast.makeText(getApplicationContext(), result, Toast.LENGTH_SHORT).show();
        }

    }

    public String PostData(String[] valuse) {
        String s="";
        try
        {
            HttpClient httpClient=new DefaultHttpClient();
            HttpPost httpPost=new HttpPost(main_url+urlReg);

            *//*List<NameValuePair> list=new ArrayList<NameValuePair>();
            list.add(new BasicNameValuePair("name", valuse[0]));
            list.add(new BasicNameValuePair("pass",valuse[1]));
            httpPost.setEntity(new UrlEncodedFormEntity(list));*//*
            JSONObject jsonne = new JSONObject();
            jsonne.put("sdId", 1345);
            jsonne.put("zoneId", 1);
            jsonne.put("zoneNo", "Test");
            jsonne.put("wardNo", "ward");
            jsonne.put("establishmentTypeId", 23);
            jsonne.put("ownerShipTypeId", 54);
            jsonne.put("houseNo", "2-2-11");
            jsonne.put("tagId", "T123");
            jsonne.put("amount_collected", 10);
            jsonne.put("collected_date", "2024-07-08T06:45:10.945Z");
            jsonne.put("collected_period", "123");
            jsonne.put("receipt_number", "1234e");
            jsonne.put("receipt_img", "");
            jsonne.put("sync_server", true);
            jsonne.put("sync_date", "2024-07-08T06:45:10.945Z");
            httpPost.setHeader((Header) jsonne);
            //setEntity(new UrlEncodedFormEntity((Iterable<? extends NameValuePair>) jsonne));
            HttpResponse httpResponse=  httpClient.execute(httpPost);

            HttpEntity httpEntity=httpResponse.getEntity();
            s= readResponse(httpResponse);

        }
        catch(Exception exception)  {}
        return s;


    }
    public String readResponse(HttpResponse res) {
        InputStream is=null;
        String return_text="";
        try {
            is=res.getEntity().getContent();
            BufferedReader bufferedReader=new BufferedReader(new InputStreamReader(is));
            String line="";
            StringBuffer sb=new StringBuffer();
            while ((line=bufferedReader.readLine())!=null)
            {
                sb.append(line);
            }
            return_text=sb.toString();
        } catch (Exception e)
        {

        }
        return return_text;

    }*/

    private DataModal modal;

    private void postData() {

        // on below line we are creating a retrofit
        // builder and passing our base url
        /*Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://ramkyapi.beulahsoftware.com//api/PaymentsDetails/")
//                .baseUrl("https://ramesharjampudi-001-site1.ltempurl.com/Api/User/")
                // as we are sending data in json format so
                // we have to add Gson converter factory
                .addConverterFactory(GsonConverterFactory.create())
                // at last we are building our retrofit builder.
                .build();
        // below line is to create an instance for our retrofit api class.
        RetrofitAPI retrofitAPI = retrofit.create(RetrofitAPI.class);*/
        //ssl unsafe instance creation
        RetrofitAPI retrofitAPI = NetworkHandler.getRetrofit().create(RetrofitAPI.class);

        modal = new DataModal(
                222,
                3,
                "zone1",
                "word1",
                12,
                25,
                "1-23",
                "tag-5",
                50,
                Utility.getDateTime_DD_MM_YYYY(),
                "JUNE",
                "12345e",
                "",
                true, Utility.getDateTime_DD_MM_YYYY()

        );

        Call<DataModal> call = retrofitAPI.createPost(modal);

        call.enqueue(new Callback<DataModal>() {
            @Override
            public void onResponse(Call<DataModal> call, Response<DataModal> response) {
                // this method is called when we get response from our api.
                Toast.makeText(DashboardActivity.this, "Data added to API", Toast.LENGTH_SHORT).show();

                // we are getting response from our body
                // and passing it to our modal class.
                DataModal responseFromAPI = response.body();

                // on below line we are getting our data from modal class
                // and adding it to our string.
                String responseString = "Response Code : " + response.code();

                // below line we are setting our
                // string to our text view.
                // responseTV.setText(responseString);

                Log.e(TAG, "response: " + response);
//                Log.e(TAG, "response StatusCode: " + responseFromAPI.getStatusCode());
//                Log.e(TAG, "response StatusMessage: " + responseFromAPI.getStatusMessage());
                Log.e(TAG, "response.code: " + response.code());
                Log.e(TAG, "response.body(): " + response.body());
                Log.e(TAG, "response.raw(): " + response.raw());
                Log.e(TAG, "response.isSuccessful(): " + response.isSuccessful());
                Log.e(TAG, "response.message(): " + response.message());
                Log.e(TAG, "response.errorBody(): " + response.errorBody());
                Log.e(TAG, "response.headers(): " + response.headers());

            }

            @Override
            public void onFailure(Call<DataModal> call, Throwable t) {
                // setting text to our text view when
                // we get error response from API.
                // responseTV.setText("Error found is : " + t.getMessage());

                Log.e(TAG, "onFailure: " + t.getMessage());
            }
        });
    }

    private final static String TAG = DashboardActivity.class.getSimpleName();
}