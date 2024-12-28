package com.org.lsa;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.provider.Settings;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ThreadLocalRandom;

import com.eze.api.EzeAPI;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationAvailability;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.org.lsa.adapter.PaymentHistoryAdapter;
import com.org.lsa.custom.GpsTracker;
import com.org.lsa.custom.PermissionUtils;
import com.org.lsa.custom.SearchableSpinner;
import com.org.lsa.custom.Utility;
import com.org.lsa.receivers.GPSReceiver;

import org.json.JSONObject;
import org.json.JSONArray;
import org.json.JSONException;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.TimeZone;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import okhttp3.Call;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Callback;
import okhttp3.Response;

public class AmountCollectionEntry extends AppCompatActivity {

    private GpsTracker gpsTracker;
    boolean isGPSEnabled = false;
    String current_lat;
    String current_lng;

    private Calendar selectedCalendar = Calendar.getInstance();
    private String formattedDateTime_chq = "";
    private String formattedDateTime_dd = "";
    private String formattedDateTime_neft = "";
    private String formattedDateTime_rtgs = "";

    private final int REQUEST_CODE_CARD_TXN = 10005;
    private final int REQUEST_CODE_SALE_TXN = 10007;
    private final int REQUEST_CODE_UPI_TXN = 10017;
    private final int REQUEST_CODE_QR_CODE_TXN = 10019;
    private final int REQUEST_CODE_REMOTE_PAY = 10016;
    private final int REQUEST_CODE_PRINT_RECEIPT = 10028;
    private final int REQUEST_CODE_PRINT_BITMAP = 10029;

    private String api_key = "http://45.114.246.201:6005";
    private String sendImgStr = "", sendTransactionNoStr = "", sendBankNameStr = "", sendBankDateStr="", sendTransactionNoStr_QR = "";

    String zoneId, wardId, zoneName, wardName, status;

    boolean authorized = false;

//    private Button goBackButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_amount_collection_entry);
        Toolbar toolbar2 = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar2);

        getSupportActionBar().setTitle((CharSequence) "Amount Collection Entry");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(getResources().getDrawable(R.drawable.back_arrow));

        // Check if SMS permission is granted
//        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
//            // Request permission
//            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SEND_SMS}, MY_PERMISSIONS_REQUEST_SEND_SMS);
//        }

       /* goBackButton = findViewById(R.id.goBack_button);

        goBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AmountCollectionEntry.this, DashboardActivity.class);
                intent.putExtra("zoneName", zoneName);
                intent.putExtra("wardName", wardName);
                startActivity(intent);
            }
        });*/


        initializeViews();
        enableGPSReceiver();
        requestStoragePermission();
    }



    private TextInputEditText house_no_entry_edt, zone_ward_entry_edt,
            owner_name_entry_edt, owner_contact_entry_edt, establishment_entry_edt;
    private String house_noStr, zone_wardStr, owner_nameStr, owner_contactStr, establishmentStr, addressStr;

    private int SD_idStr;

    private Integer fixedValueAmt = 0;

    private TextInputEditText
            address_entry_edt, lat_long_entry_edt;
    private ImageButton get_location_img_btn, edit_owner_contact_no_img_btn;

    private TextView edit_entry_txt, amountToBeCollectedTitle_txt, tvShowPaymentHistory;

    private TextView show_bank_qr_code_entry_txt, paid_amt_entry_txt, balance_amt_entry_txt;

    private LinearLayout hide_lin_lay_qr_code_bank, hide_ln_lay_qr_photo;
    private ImageView qr_code_bank_entry_img_view, upload_payment_transaction_id_entry_img_view;
    private ImageButton upload_camera_image_button;
    private TextInputEditText collected_from_entry_edt, payment_reference_no_entry_edt, remarksAny_edt;

    private Button calculateButton;
    private TextInputEditText monthsPayingEditText;
    private TextInputEditText amountPayingEditText;

    TextView displayStatus;

//    private LinearLayout monthsContainer;
    private GridLayout monthsContainer;

    private Button submit_entry_btn;

    private List<CheckBox> checkBoxesList = new ArrayList<>();

    private List<Map<String, Object>> paymentHistoryList;



    private void initializeViews() {

        Intent intent = getIntent();
        SD_idStr = intent.getIntExtra("SD_id",0);
        wardId = String.valueOf(intent.getIntExtra("wardNo",0));
//        Log.d(TAG, "SD_idStr : " + SD_idStr);

        loadPaymentHistory(SD_idStr);

        house_noStr = intent.getStringExtra("house_no");
        zoneName = intent.getStringExtra("zoneName");
        wardName = intent.getStringExtra("wardName");
        zone_wardStr = zoneName +" and "+wardName;
        owner_nameStr = intent.getStringExtra("owner_name");
        owner_contactStr = intent.getStringExtra("owner_contact");
        establishmentStr = intent.getStringExtra("establishmentType");
//        Log.d("Establishment","Establishment type "+establishmentStr);
        addressStr = intent.getStringExtra("address");
        zoneId = intent.getStringExtra("zoneId");

        house_no_entry_edt = findViewById(R.id.house_no_entry_edt);

        zone_ward_entry_edt = findViewById(R.id.zone_ward_entry_edt);

        owner_name_entry_edt = findViewById(R.id.owner_name_entry_edt);

        owner_contact_entry_edt = findViewById(R.id.owner_contact_entry_edt);

//        establishment_entry_edt = findViewById(R.id.establishment_entry_edt);

        address_entry_edt = findViewById(R.id.address_entry_edt);

        displayStatus = findViewById(R.id.razorpay_status);

//        lat_long_entry_edt = findViewById(R.id.lat_long_entry_edt);

        get_location_img_btn = findViewById(R.id.get_location_img_btn);

        edit_entry_txt = findViewById(R.id.edit_entry_txt);
        edit_entry_txt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                address_entry_edt.setEnabled(true);
                get_location_img_btn.setEnabled(true);
            }
        });

        tvShowPaymentHistory = findViewById(R.id.tvShowPaymentHistory);
        tvShowPaymentHistory.setPaintFlags(Paint.UNDERLINE_TEXT_FLAG);
        tvShowPaymentHistory.setOnClickListener(v -> showPaymentHistoryDialog());

        paid_amt_entry_txt = findViewById(R.id.paid_amt_entry_txt);
        balance_amt_entry_txt = findViewById(R.id.balance_amt_entry_txt);

        house_no_entry_edt.setText(house_noStr);
        zone_ward_entry_edt.setText(zone_wardStr);
        owner_name_entry_edt.setText(owner_nameStr);
        owner_contact_entry_edt.setText(owner_contactStr);
//        establishment_entry_edt.setText(establishmentStr);
        address_entry_edt.setText(addressStr);

        collected_from_entry_edt = findViewById(R.id.collected_from_entry_edt);
        amountToBeCollectedTitle_txt = findViewById(R.id.amt_to_be_collected_title_txt);
        int months = intent.getIntExtra("defaultMonths",0);
        int amount = intent.getIntExtra("total_outstanding_amount",0);
        if(months == 0){
            amount = 0;
        }
        balance_amt_entry_txt.setText(""+amount);
        paid_amt_entry_txt.setText(""+months);

        monthsContainer = findViewById(R.id.monthsContainer);

        displayLastNMonths(months);

        if(months!=0){
            fixedValueAmt = amount/months;
        }
        amountToBeCollectedTitle_txt.setText(getResources().getString(R.string.amt_collected_per_month_title) + " (₹" + fixedValueAmt + ")");

        monthsPayingEditText = findViewById(R.id.months_paying);
        amountPayingEditText = findViewById(R.id.amount_paying);
        calculateButton = findViewById(R.id.calculate_button);

        calculateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                calculateAmount();
            }
        });

        show_bank_qr_code_entry_txt = findViewById(R.id.show_bank_qr_code_entry_txt);
        show_bank_qr_code_entry_txt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(amountPayingEditText.getText().toString().isEmpty()){
                    Toast.makeText(AmountCollectionEntry.this, "Please enter no. of months and click on calculate to pay", Toast.LENGTH_SHORT).show();
                    return;
                }
                if(monthsPayingEditText.getText().toString().equals("0")){
                    Toast.makeText(AmountCollectionEntry.this, "Number of months is 0", Toast.LENGTH_SHORT).show();
                    return;
                }
                payment_reference_no_entry_edt.requestFocus();
                hide_lin_lay_qr_code_bank.setVisibility(View.VISIBLE);
            }
        });
        hide_lin_lay_qr_code_bank = findViewById(R.id.hide_lin_lay_qr_code_bank);
        hide_lin_lay_qr_code_bank.setVisibility(View.GONE);

        hide_ln_lay_qr_photo = findViewById(R.id.hide_ln_lay_capture_photo);

        qr_code_bank_entry_img_view = findViewById(R.id.qr_code_bank_entry_img_view);
        payment_reference_no_entry_edt = findViewById(R.id.payment_reference_no_entry_edt);
        remarksAny_edt = findViewById(R.id.remarks_any_edt);
        upload_payment_transaction_id_entry_img_view = findViewById(R.id.upload_payment_transaction_id_entry_img_view);

        upload_camera_image_button = findViewById(R.id.upload_camera_image_button);
        upload_camera_image_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                filename = "Testing_" + System.currentTimeMillis();
                callCameraNew(TAKE_PICTURE_ONE, filename);
            }
        });

        submit_entry_btn = findViewById(R.id.submit_entry_btn);
        submit_entry_btn.setVisibility(View.GONE);
        submit_entry_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                validateSubmittingData();
            }
        });

        paymentType_spin = findViewById(R.id.payment_type_spin);
        hide_qrCodePayment_linear = findViewById(R.id.hide_qr_code_payment_linear);
        callChequePaymentViews();

    }

    private void loadPaymentHistory(int mainSDId) {
        new Thread(() -> {
            try {
                URL url = new URL(api_key + "/api/PaymentsDetails/GetPaymentHistory?mainSDId=" + mainSDId);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Accept", "*/*");
                conn.setDoOutput(false);

                int responseCode = conn.getResponseCode();
//                Log.d("Debug", "Response code " + responseCode + " response " + conn.getResponseMessage() + " URL " + conn.getURL());

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    InputStream is = conn.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        sb.append(line);
                    }

                    paymentHistoryList = new Gson().fromJson(
                            sb.toString(), new TypeToken<List<Map<String, Object>>>() {}.getType()
                    );

                    runOnUiThread(() -> Log.d("Debug", "Payment history loaded successfully"));

                } else if (responseCode == HttpURLConnection.HTTP_NOT_FOUND) {
                    paymentHistoryList = new ArrayList<>(); // No history found
                    runOnUiThread(() -> Log.d("Debug", "No payment history found for this user"));
                } else {
                    runOnUiThread(() -> Toast.makeText(this, "Failed to load data", Toast.LENGTH_SHORT).show());
                }
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    private void showPaymentHistoryDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.payment_history_dialog, null);
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();

        TextView noHistoryTextView = dialogView.findViewById(R.id.noHistoryTextView);
        RecyclerView rvPaymentHistory = dialogView.findViewById(R.id.rvPaymentHistory);
        rvPaymentHistory.setLayoutManager(new LinearLayoutManager(this));

        if (paymentHistoryList == null) {
            Toast.makeText(this, "Payment history not loaded yet. Please try again.", Toast.LENGTH_SHORT).show();
        } else if (paymentHistoryList.isEmpty()) {
            noHistoryTextView.setVisibility(View.VISIBLE);
            rvPaymentHistory.setVisibility(View.GONE);
        } else {
            PaymentHistoryAdapter adapter = new PaymentHistoryAdapter(paymentHistoryList);
            rvPaymentHistory.setAdapter(adapter);
            noHistoryTextView.setVisibility(View.GONE);
            rvPaymentHistory.setVisibility(View.VISIBLE);
        }

        dialog.show();

        ImageView btnCloseDialog = dialog.findViewById(R.id.btnCloseDialog);
        btnCloseDialog.setOnClickListener(v -> dialog.dismiss());
    }



    private int fin_months, fin_amount;

    private void calculateAmount() {
        // Get the input from the months paying field
        String monthsInput = monthsPayingEditText.getText().toString();

        // Check if the input is valid
        if (!TextUtils.isEmpty(monthsInput)) {
            try {
                fin_months = Integer.parseInt(monthsInput);

                if(fin_months <= 0){
                    modifyCheckboxes(0);
                    amountPayingEditText.setText("");
                    if(fin_months == 0){
                        showToastMessage("Months should not be 0");
                    }
                    else{
                        showToastMessage("Months should not be negative");
                    }
                    return;
                }

                if(fin_months > Integer.parseInt(paid_amt_entry_txt.getText().toString())){
                    Toast.makeText(AmountCollectionEntry.this, "The number of months should be less than the due months", Toast.LENGTH_SHORT).show();
                    amountPayingEditText.setText("");
                    return;
                }

                modifyCheckboxes(fin_months);

                // Calculate the amount by multiplying months by the fixed price
                fin_amount = fin_months * fixedValueAmt;

                // Display the calculated amount in the amount paying field
                amountPayingEditText.setText(String.valueOf(fin_amount));
            } catch (NumberFormatException e) {
                // Handle invalid input
                monthsPayingEditText.setError("Enter a valid number of months");
            }
        } else {
            monthsPayingEditText.setError("This field cannot be empty");
        }
    }

    private void displayLastNMonths(int monthsToPay) {
        // Clear previous checkboxes if any
        monthsContainer.removeAllViews();
        checkBoxesList.clear();

        // Get current month
        Calendar calendar = Calendar.getInstance();

        // Format for month and year (e.g., Jan 2023)
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM yyyy", Locale.getDefault());

        // Loop to generate the last N months
        for (int i = 0; i < monthsToPay; i++) {

            // Go to the previous month
            calendar.add(Calendar.MONTH, -1);

            // Create a new CheckBox for each month
            CheckBox checkBox = new CheckBox(this);
            checkBox.setText(dateFormat.format(calendar.getTime()));
            checkBox.setEnabled(false);  // Make checkbox read-only

            checkBoxesList.add(checkBox);
        }

        Collections.reverse(checkBoxesList);

        // Add checkboxes to the container in reversed order
//        for (CheckBox checkBox : checkBoxesList) {
//            monthsContainer.addView(checkBox);
//        }
        for (CheckBox checkBox : checkBoxesList) {
            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = 0; // Use layout_weight behavior
            params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f); // Distribute space evenly
            checkBox.setLayoutParams(params);

            monthsContainer.addView(checkBox);
        }
    }

    private void modifyCheckboxes(int months) {
        int months_n = Integer.parseInt(paid_amt_entry_txt.getText().toString());
        for(int j=0;j<months_n;j++){
            checkBoxesList.get(j).setChecked(false);
            checkBoxesList.get(j).setTextColor(Color.GRAY);
        }
        if(months <= 0 || months>months_n){
            return;
        }
        for(int i=0;i<months;i++){
            checkBoxesList.get(i).setChecked(true);
            checkBoxesList.get(i).setTextColor(Color.BLACK);
        }
    }

    private SearchableSpinner paymentType_spin;
    private LinearLayout hide_qrCodePayment_linear;

    private LinearLayout hide_chequePayment_linear, hide_ln_lay_cheque_photo;
    private ImageButton uploadCameraImagecheque_button;
    private ImageView uploadChequePayment_entry_img_view;
    private TextInputEditText paymentChequeBankName_entry_edt, cheque_number_edt;

    MaterialButton cheque_date_btn;



    private void callChequePaymentViews() {
        hide_chequePayment_linear = findViewById(R.id.hide_cheque_payment_linear);
        hide_chequePayment_linear.setVisibility(View.GONE);

        paymentChequeBankName_entry_edt = findViewById(R.id.payment_cheque_bank_name_entry_edt);
        cheque_number_edt = findViewById(R.id.cheque_number_edt);
        cheque_date_btn = findViewById(R.id.cheque_date_btn);

        cheque_date_btn.setOnClickListener(v -> showDatePickerDialog(cheque_date_btn,0));

        hide_ln_lay_cheque_photo = findViewById(R.id.hide_ln_lay_cheque_photo);
        uploadChequePayment_entry_img_view = findViewById(R.id.upload_cheque_payment_entry_img_view);

        uploadCameraImagecheque_button = findViewById(R.id.upload_camera_image_cheque_button);
        uploadCameraImagecheque_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                filename = "Testing_Chq_" + System.currentTimeMillis();
                callCameraNew(TAKE_PICTURE_CHEQUE, filename);
            }
        });

        callDDPaymentViews();

    }

    private LinearLayout hide_ddPayment_linear, hide_ln_lay_dd_photo;
    private ImageButton uploadCameraImagedd_button;
    private ImageView uploadddPayment_entry_img_view;
    private TextInputEditText paymentddBankName_entry_edt, dd_number_edt;
    private MaterialButton dd_date_btn;


    @SuppressLint("WrongViewCast")
    private void callDDPaymentViews() {
        hide_ddPayment_linear = findViewById(R.id.hide_dd_payment_linear);
        hide_ddPayment_linear.setVisibility(View.GONE);

        paymentddBankName_entry_edt = findViewById(R.id.payment_dd_bank_name_entry_edt);
        dd_number_edt = findViewById(R.id.dd_number_edt);
//        dd_date_edt = findViewById(R.id.dd_date_edt);
        dd_date_btn = findViewById(R.id.dd_date_btn);

        dd_date_btn.setOnClickListener(v -> showDatePickerDialog(dd_date_btn,1));

        hide_ln_lay_dd_photo = findViewById(R.id.hide_ln_lay_dd_photo);
        uploadddPayment_entry_img_view = findViewById(R.id.upload_dd_payment_entry_img_view);

        uploadCameraImagedd_button = findViewById(R.id.upload_camera_image_dd_button);
        uploadCameraImagedd_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                filename = "Testing_DD_" + System.currentTimeMillis();
                callCameraNew(TAKE_PICTURE_DD, filename);
            }
        });

        callNeftPaymentViews();

    }

    private LinearLayout hide_neftPayment_linear, hide_ln_lay_neft_photo;
    private ImageButton uploadCameraImageneft_button;
    private ImageView uploadneftPayment_entry_img_view;
    private TextInputEditText paymentneftBankName_entry_edt, neft_number_edt;

    MaterialButton neft_date_btn;


    private void callNeftPaymentViews() {
        hide_neftPayment_linear = findViewById(R.id.hide_neft_payment_linear);
        hide_neftPayment_linear.setVisibility(View.GONE);

        paymentneftBankName_entry_edt = findViewById(R.id.payment_neft_bank_name_entry_edt);
        neft_number_edt = findViewById(R.id.neft_number_edt);
        neft_date_btn = findViewById(R.id.neft_date_btn);

        neft_date_btn.setOnClickListener(v -> showDatePickerDialog(neft_date_btn,2));

        hide_ln_lay_neft_photo = findViewById(R.id.hide_ln_lay_neft_photo);
        uploadneftPayment_entry_img_view = findViewById(R.id.upload_neft_payment_entry_img_view);

        uploadCameraImageneft_button = findViewById(R.id.upload_camera_image_neft_button);
        uploadCameraImageneft_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                filename = "Testing_NEFT_" + System.currentTimeMillis();
                callCameraNew(TAKE_PICTURE_NEFT, filename);
            }
        });

        callRtgsPaymentViews();

    }

    private LinearLayout hide_rtgsPayment_linear, hide_ln_lay_rtgs_photo;
    private ImageButton uploadCameraImagertgs_button;
    private ImageView uploadrtgsPayment_entry_img_view;
    private TextInputEditText paymentrtgsBankName_entry_edt, rtgs_number_edt, rtgs_date_edt;

    MaterialButton rtgs_date_btn;


    private void callRtgsPaymentViews() {
        hide_rtgsPayment_linear = findViewById(R.id.hide_rtgs_payment_linear);
        hide_rtgsPayment_linear.setVisibility(View.GONE);

        paymentrtgsBankName_entry_edt = findViewById(R.id.payment_rtgs_bank_name_entry_edt);
        rtgs_number_edt = findViewById(R.id.rtgs_number_edt);
        rtgs_date_btn = findViewById(R.id.rtgs_date_btn);

        rtgs_date_btn.setOnClickListener(v -> showDatePickerDialog(rtgs_date_btn,3));

        hide_ln_lay_rtgs_photo = findViewById(R.id.hide_ln_lay_rtgs_photo);
        uploadrtgsPayment_entry_img_view = findViewById(R.id.upload_rtgs_payment_entry_img_view);

        uploadCameraImagertgs_button = findViewById(R.id.upload_camera_image_rtgs_button);
        uploadCameraImagertgs_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                filename = "Testing_RTGS_" + System.currentTimeMillis();
                callCameraNew(TAKE_PICTURE_RTGS, filename);
            }
        });

        callPaymentTypeSpinner(paymentType_spin);

        // callAddRadioGroup();
    }

    private void showDatePickerDialog(MaterialButton button, int method) {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    selectedCalendar.set(year, month, dayOfMonth);
                    updateDateButton(button, selectedCalendar, method);
                },
                selectedCalendar.get(Calendar.YEAR),
                selectedCalendar.get(Calendar.MONTH),
                selectedCalendar.get(Calendar.DAY_OF_MONTH)
        );

        // Disable future dates
        datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
        datePickerDialog.show();
    }

    private void updateDateButton(MaterialButton button, Calendar calendar, int method) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        if(method == 0){
            formattedDateTime_chq = sdf.format(calendar.getTime());  // Store formatted date
            button.setText("Issued date: " + formattedDateTime_chq);
        }
        else if(method == 1){
            formattedDateTime_dd = sdf.format(calendar.getTime());  // Store formatted date
            button.setText("Issued date: " + formattedDateTime_dd);
        }else if(method == 2){
            formattedDateTime_neft = sdf.format(calendar.getTime());  // Store formatted date
            button.setText("Issued date: " + formattedDateTime_neft);
        }else if(method == 3){
            formattedDateTime_rtgs = sdf.format(calendar.getTime());  // Store formatted date
            button.setText("Issued date: " + formattedDateTime_rtgs);
        }

    }

    private RadioGroup paymentGroupRadio;
    private String paymentTypeSelection = "QR";

    /* private void callAddRadioGroup() {
        paymentGroupRadio = findViewById(R.id.payment_group_radio);
        RadioButton qrCodeRadia_btn = findViewById(R.id.qr_code_radia_btn);
        qrCodeRadia_btn.setChecked(true);

        paymentGroupRadio.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                int checkedRadioButtonID = paymentGroupRadio.getCheckedRadioButtonId();
                switch (checkedRadioButtonID) {
                    case R.id.qr_code_radia_btn:
                        paymentTypeSelection = "QR";
                        enablePaymentSelectedView(0);
                        break;

                    case R.id.cheque_dd_radia_btn:
                        paymentTypeSelection = "CHEQUE";
                        enablePaymentSelectedView(1);

                        break;

                    case R.id.cash_radia_btn:
                        paymentTypeSelection = "CASH";
                        enablePaymentSelectedView(2);

                        break;
                    case R.id.dd_radia_btn:
                        paymentTypeSelection = "DD";
                        enablePaymentSelectedView(3);
                        break;

//                    case R.id.neft_radia_btn:
//                        paymentTypeSelection = "NEFT";
//                        enablePaymentSelectedView(4);
//
//                        break;
//
//                    case R.id.rtgs_radia_btn:
//                        paymentTypeSelection = "RTGS";
//                        enablePaymentSelectedView(5);
//                        break;

                    case R.id.card_radia_btn:
                        paymentTypeSelection = "CARD";
                        Toast.makeText(AmountCollectionEntry.this,
                                "Razorpay card payment", Toast.LENGTH_SHORT).show();
//                        payWithCard();
//                        submit_entry_btn.setVisibility(View.VISIBLE);
                        break;

                    case R.id.upi_radia_btn:
                        paymentTypeSelection = "UPI";
                        Toast.makeText(AmountCollectionEntry.this,
                                "Razorpay UPI payment", Toast.LENGTH_SHORT).show();
//                        payWithUPI();
//                        submit_entry_btn.setVisibility(View.VISIBLE);
                        break;

                    case R.id.rqr_radia_btn:
                        paymentTypeSelection = "Dynamic QR";
                        Toast.makeText(AmountCollectionEntry.this,
                                "Razorpay QR payment", Toast.LENGTH_SHORT).show();
//                        payWithQRCode();
//                        submit_entry_btn.setVisibility(View.VISIBLE);
                        break;

                    default:
                        break;
                }
            }
        });
    } */



    private void payWithUPI() {
        JSONObject jsonRequest = new JSONObject();
        try {
            jsonRequest.put("amount", Integer.parseInt(amountPayingEditText.getText().toString()));

            JSONObject jsonOptionalParams = new JSONObject();
            try {
                JSONObject jsonCustomer = new JSONObject();
                try {
                    jsonCustomer.put("name", owner_nameStr);
                    jsonCustomer.put("mobileNo", owner_contactStr);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                JSONObject jsonReferences = new JSONObject();
                JSONArray additionalReferences = new JSONArray();
                try {
//                    long number = (long) Math.floor(Math.random() * 9_000_000_000L) + 1_000_000_000L;
                    long timePart = System.currentTimeMillis(); // Current time in milliseconds
                    long randomPart = ThreadLocalRandom.current().nextInt(1_000, 10_000); // Random 4 digits
                    long number = timePart * 10_000 + randomPart;
                    jsonReferences.put("reference1", number);


                    additionalReferences.put("Paying using UPI");
                    additionalReferences.put("LSA payment");
                    jsonReferences.put("additionalReferences", additionalReferences);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                jsonReferences.put("additionalReferences", additionalReferences);
                jsonOptionalParams.put("references", jsonReferences);
                jsonOptionalParams.put("customer", jsonCustomer);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            jsonRequest.put("options", jsonOptionalParams);
            JSONObject upiObject = new JSONObject();
            try {
                upiObject.put("payerVPA", "success@razorpay");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            jsonRequest.put("upi", upiObject);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        EzeAPI.upiTransaction(this, REQUEST_CODE_UPI_TXN, jsonRequest);
    }

    private void payWithQRCode() {
        JSONObject jsonRequest = new JSONObject();
        try {
            jsonRequest.put("amount", Integer.parseInt(amountPayingEditText.getText().toString()));

            JSONObject jsonOptionalParams = new JSONObject();
            try {
                JSONObject jsonCustomer = new JSONObject();
                try {
                    jsonCustomer.put("name", owner_nameStr);
                    jsonCustomer.put("mobileNo", owner_contactStr);
//                    jsonCustomer.put("email", email_idStr);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                JSONObject jsonReferences = new JSONObject();
                JSONArray additionalReferences = new JSONArray();
                try {
//                    long number = (long) Math.floor(Math.random() * 9_000_000_000L) + 1_000_000_000L;
                    long timePart = System.currentTimeMillis(); // Current time in milliseconds
                    long randomPart = ThreadLocalRandom.current().nextInt(1_000, 10_000); // Random 4 digits
                    long number = timePart * 10_000 + randomPart;
                    jsonReferences.put("reference1", number);

                    additionalReferences.put("Paying using QR code");
                    additionalReferences.put("LSA payment");
                    jsonReferences.put("additionalReferences", additionalReferences);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                jsonReferences.put("additionalReferences", additionalReferences);
                jsonOptionalParams.put("references", jsonReferences);
                jsonOptionalParams.put("customer", jsonCustomer);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            jsonRequest.put("options", jsonOptionalParams);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        EzeAPI.qrCodeTransaction(this, REQUEST_CODE_QR_CODE_TXN, jsonRequest);
    }

    private void payWithSMS() {
        JSONObject jsonRequest = new JSONObject();
        try {
            jsonRequest.put("amount", Integer.parseInt(amountPayingEditText.getText().toString()));
//            jsonRequest.put("amount", 2);

            JSONObject jsonOptionalParams = new JSONObject();
            try {
                JSONObject jsonCustomer = new JSONObject();
                try {
                    jsonCustomer.put("name", owner_nameStr);
                    jsonCustomer.put("mobileNo", owner_contactStr);
//                    jsonCustomer.put("email", email_idStr);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                JSONObject jsonReferences = new JSONObject();
                JSONArray additionalReferences = new JSONArray();
                try {
//                    long number = (long) Math.floor(Math.random() * 9_000_000_000L) + 1_000_000_000L;
                    long timePart = System.currentTimeMillis(); // Current time in milliseconds
                    long randomPart = ThreadLocalRandom.current().nextInt(1_000, 10_000); // Random 4 digits
                    long number = timePart * 10_000 + randomPart;
                    jsonReferences.put("reference1", number);

                    additionalReferences.put("Paying using QR code");
                    additionalReferences.put("LSA payment");
                    jsonReferences.put("additionalReferences", additionalReferences);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                jsonReferences.put("additionalReferences", additionalReferences);
                jsonOptionalParams.put("references", jsonReferences);
                jsonOptionalParams.put("customer", jsonCustomer);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            jsonRequest.put("options", jsonOptionalParams);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        doPay(jsonRequest);
    }

    private void doPay(JSONObject jsonRequest) {
        // Assuming EzeAPI is integrated for remote payment
        EzeAPI.remotePayment(this, REQUEST_CODE_REMOTE_PAY, jsonRequest);
    }

    private void payWithCard() {
        JSONObject jsonRequest = new JSONObject();
        try {
            jsonRequest.put("amount", Integer.parseInt(amountPayingEditText.getText().toString()));
            jsonRequest.put("amountCashback", 0);
            jsonRequest.put("amountTip", 0);

            JSONObject jsonReferences = new JSONObject();
            try {
//                long number = (long) Math.floor(Math.random() * 9_000_000_000L) + 1_000_000_000L;
                long timePart = System.currentTimeMillis(); // Current time in milliseconds
                long randomPart = ThreadLocalRandom.current().nextInt(1_000, 10_000); // Random 4 digits
                long number = timePart * 10_000 + randomPart;
                jsonReferences.put("reference1", number);
                JSONArray additionalReferences = new JSONArray();
                additionalReferences.put("Paying using card");
                additionalReferences.put("LSA payment");
                jsonReferences.put("additionalReferences", additionalReferences);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            JSONObject jsonCustomer = new JSONObject();
            try {
                jsonCustomer.put("name", owner_nameStr);
                jsonCustomer.put("mobileNo", owner_contactStr);
//                jsonCustomer.put("email", email_idStr);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            JSONObject jsonOptions = new JSONObject();
            try {
                jsonOptions.put("references", jsonReferences);
                jsonOptions.put("customer", jsonCustomer);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            jsonRequest.put("options", jsonOptions);
            jsonRequest.put("mode", "SALE");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        doSaleTxn(jsonRequest);
    }

    private void doSaleTxn(JSONObject jsonRequest) {
        EzeAPI.cardTransaction(this, REQUEST_CODE_SALE_TXN, jsonRequest);
    }

    private void printReceipt(String txnId) {
        EzeAPI.printReceipt(this, REQUEST_CODE_PRINT_RECEIPT, txnId);
    }

    private void enablePaymentSelectedView(int selectedPaymentVal) {

        clearIfExistingData();

        switch (selectedPaymentVal) {
            case 0:
                //Qr
                hide_chequePayment_linear.setVisibility(View.GONE);
                hide_ddPayment_linear.setVisibility(View.GONE);
                hide_neftPayment_linear.setVisibility(View.GONE);
                hide_rtgsPayment_linear.setVisibility(View.GONE);
//                hide_ln_lay_cheque_photo.setVisibility(View.VISIBLE);
                uploadChequePayment_entry_img_view.setImageBitmap(null);
                uploadddPayment_entry_img_view.setImageBitmap(null);
                uploadneftPayment_entry_img_view.setImageBitmap(null);
                uploadrtgsPayment_entry_img_view.setImageBitmap(null);

                submit_entry_btn.setVisibility(View.GONE);

                hide_ln_lay_qr_photo.setVisibility(View.VISIBLE);

                hide_qrCodePayment_linear.setVisibility(View.VISIBLE);

                break;

            case 1:
                //Cheque
                hide_qrCodePayment_linear.setVisibility(View.GONE);
                hide_ddPayment_linear.setVisibility(View.GONE);
                hide_neftPayment_linear.setVisibility(View.GONE);
                hide_rtgsPayment_linear.setVisibility(View.GONE);

                upload_payment_transaction_id_entry_img_view.setImageBitmap(null);
                uploadddPayment_entry_img_view.setImageBitmap(null);
                uploadneftPayment_entry_img_view.setImageBitmap(null);
                uploadrtgsPayment_entry_img_view.setImageBitmap(null);
                submit_entry_btn.setVisibility(View.GONE);

                hide_ln_lay_cheque_photo.setVisibility(View.VISIBLE);
                hide_chequePayment_linear.setVisibility(View.VISIBLE);
                break;

            case 2:
                //Cash
                hide_qrCodePayment_linear.setVisibility(View.GONE);
                hide_chequePayment_linear.setVisibility(View.GONE);
                hide_ddPayment_linear.setVisibility(View.GONE);
                hide_neftPayment_linear.setVisibility(View.GONE);
                hide_rtgsPayment_linear.setVisibility(View.GONE);

                upload_payment_transaction_id_entry_img_view.setImageBitmap(null);
                uploadChequePayment_entry_img_view.setImageBitmap(null);
                uploadddPayment_entry_img_view.setImageBitmap(null);
                uploadneftPayment_entry_img_view.setImageBitmap(null);
                uploadrtgsPayment_entry_img_view.setImageBitmap(null);

//                hide_ln_lay_cheque_photo.setVisibility(View.VISIBLE);
                // hide_ln_lay_cheque_photo.setVisibility(View.GONE);

                submit_entry_btn.setVisibility(View.VISIBLE);
                break;
            case 3:
                //DD
                hide_qrCodePayment_linear.setVisibility(View.GONE);
                hide_chequePayment_linear.setVisibility(View.GONE);
                hide_ddPayment_linear.setVisibility(View.VISIBLE);
                hide_neftPayment_linear.setVisibility(View.GONE);
                hide_rtgsPayment_linear.setVisibility(View.GONE);

                upload_payment_transaction_id_entry_img_view.setImageBitmap(null);
                uploadChequePayment_entry_img_view.setImageBitmap(null);
                //uploadddPayment_entry_img_view.setImageBitmap(null);
                uploadneftPayment_entry_img_view.setImageBitmap(null);
                uploadrtgsPayment_entry_img_view.setImageBitmap(null);


                hide_ln_lay_dd_photo.setVisibility(View.VISIBLE);
//                hide_ln_lay_cheque_photo.setVisibility(View.GONE);

                submit_entry_btn.setVisibility(View.GONE);
                break;
            case 4:
                //NEFT
                hide_qrCodePayment_linear.setVisibility(View.GONE);
                hide_chequePayment_linear.setVisibility(View.GONE);
                hide_ddPayment_linear.setVisibility(View.GONE);
                hide_neftPayment_linear.setVisibility(View.VISIBLE);
                hide_rtgsPayment_linear.setVisibility(View.GONE);

                upload_payment_transaction_id_entry_img_view.setImageBitmap(null);
                uploadChequePayment_entry_img_view.setImageBitmap(null);
                uploadddPayment_entry_img_view.setImageBitmap(null);
//                uploadneftPayment_entry_img_view.setImageBitmap(null);
                uploadrtgsPayment_entry_img_view.setImageBitmap(null);


                hide_ln_lay_neft_photo.setVisibility(View.VISIBLE);
//                hide_ln_lay_cheque_photo.setVisibility(View.GONE);

                submit_entry_btn.setVisibility(View.GONE);
                break;
            case 5:
                //RTGS
                hide_qrCodePayment_linear.setVisibility(View.GONE);
                hide_chequePayment_linear.setVisibility(View.GONE);
                hide_ddPayment_linear.setVisibility(View.GONE);
                hide_neftPayment_linear.setVisibility(View.GONE);
                hide_rtgsPayment_linear.setVisibility(View.VISIBLE);

                upload_payment_transaction_id_entry_img_view.setImageBitmap(null);
                uploadChequePayment_entry_img_view.setImageBitmap(null);
                uploadddPayment_entry_img_view.setImageBitmap(null);
                uploadneftPayment_entry_img_view.setImageBitmap(null);
//                uploadrtgsPayment_entry_img_view.setImageBitmap(null);


                hide_ln_lay_rtgs_photo.setVisibility(View.VISIBLE);
//                hide_ln_lay_cheque_photo.setVisibility(View.GONE);

                submit_entry_btn.setVisibility(View.GONE);
                break;
            default:
                hide_qrCodePayment_linear.setVisibility(View.GONE);
                hide_chequePayment_linear.setVisibility(View.GONE);
                hide_ddPayment_linear.setVisibility(View.GONE);
                hide_neftPayment_linear.setVisibility(View.GONE);
                hide_rtgsPayment_linear.setVisibility(View.GONE);

                upload_payment_transaction_id_entry_img_view.setImageBitmap(null);
                uploadChequePayment_entry_img_view.setImageBitmap(null);
                uploadddPayment_entry_img_view.setImageBitmap(null);
                uploadneftPayment_entry_img_view.setImageBitmap(null);
                uploadrtgsPayment_entry_img_view.setImageBitmap(null);

//                displayStatus.setVisibility(View.hanVISIBLE);
                submit_entry_btn.setVisibility(View.VISIBLE);
                break;
        }

    }

    private void clearIfExistingData(){
        upload_payment_transaction_id_entry_img_view.setImageBitmap(null);
//        upload_payment_transaction_id_entry_img_view.setBackgroundResource(R.drawable.ic_baseline_photo_camera_24);
        uploadChequePayment_entry_img_view.setImageBitmap(null);
//        uploadChequePayment_entry_img_view.setBackgroundResource(R.drawable.ic_baseline_photo_camera_24);
        uploadddPayment_entry_img_view.setImageBitmap(null);
//        uploadddPayment_entry_img_view.setBackgroundResource(R.drawable.ic_baseline_photo_camera_24);
        uploadneftPayment_entry_img_view.setImageBitmap(null);
//        uploadneftPayment_entry_img_view.setBackgroundResource(R.drawable.ic_baseline_photo_camera_24);
        uploadrtgsPayment_entry_img_view.setImageBitmap(null);
//        uploadrtgsPayment_entry_img_view.setBackgroundResource(R.drawable.ic_baseline_photo_camera_24);

        //QR
        this.payment_reference_no_entry_edt.setText("");
        this.remarksAny_edt.setText("");

        //Cheque
        paymentChequeBankName_entry_edt.setText("");
        cheque_number_edt.setText("");
        formattedDateTime_chq = "";
        cheque_date_btn.setText("Issued date: ");

        //DD
        paymentddBankName_entry_edt.setText("");
        dd_number_edt.setText("");
        formattedDateTime_dd = "";
        dd_date_btn.setText("Issued date: ");

        //NEFT
        paymentneftBankName_entry_edt.setText("");
        neft_number_edt.setText("");
        formattedDateTime_neft = "";
        neft_date_btn.setText("Issued date: ");

        //RTGS
        paymentrtgsBankName_entry_edt.setText("");
        rtgs_number_edt.setText("");
        formattedDateTime_rtgs = "";
        rtgs_date_btn.setText("Issued date: ");


    }

    private Boolean ownerNumberEditFlag = false;

    private void validateSubmittingData() {
//        Log.d("API","Clicked submit button");
        if (ownerNumberEditFlag) {
            if (!this.owner_contact_entry_edt.getText().toString().trim().matches("^[6789]\\d{9}$")) {
                showToastMessage("Please Enter Valid Owner Number ..!");
                this.owner_contact_entry_edt.requestFocus();
                return;
            }
        }

        String months_pay = this.monthsPayingEditText.getText().toString().trim();
        if (months_pay.length() == 0) {
            showToastMessage("Please Enter Number of months amount paying..!");
            this.monthsPayingEditText.requestFocus();
            return;
        }

        String amount_months = this.amountPayingEditText.getText().toString().trim();
        if (amount_months.length() == 0) {
            showToastMessage("Please entre number of months and click on calculate button..!");
            this.amountPayingEditText.requestFocus();
            return;
        }

        if (paymentTypeSelection.equalsIgnoreCase("QR")) {
            if (this.upload_payment_transaction_id_entry_img_view.getDrawable() == null) {
                showToastMessage("Please " + getResources().getString(R.string.transaction_id_upload) + "!..");
                return;
            }
            String paymentRefStr = this.payment_reference_no_entry_edt.getText().toString().trim();
            if (paymentRefStr.length() == 0) {
                showToastMessage("Please Enter " + getResources().getString(R.string.bank_qr_code_ref_number1) + "..!");
                this.payment_reference_no_entry_edt.requestFocus();
                return;
            }
        } else if (paymentTypeSelection.equalsIgnoreCase("Cheque")) {

            if (this.uploadChequePayment_entry_img_view.getDrawable() == null) {
                showToastMessage("Please " + getResources().getString(R.string.cheque_photo_upload) + "!..");
                return;
            }
            String paymentRefStr = this.paymentChequeBankName_entry_edt.getText().toString().trim();
            if (paymentRefStr.length() == 0) {
                showToastMessage("Please Enter " + getResources().getString(R.string.bank_name_cheque) + "..!");
                this.paymentChequeBankName_entry_edt.requestFocus();
                return;
            }

            // Limit the bank name length
            if (paymentRefStr.length() > 50) {
                showToastMessage("Bank name cannot exceed 50 characters.");
                this.paymentChequeBankName_entry_edt.requestFocus();
                return;
            }

            String chequeDDNumberStr = this.cheque_number_edt.getText().toString().trim();
            if (chequeDDNumberStr.length() == 0) {
                showToastMessage("Please Enter " + getResources().getString(R.string.cheque_no) + "..!");
                this.cheque_number_edt.requestFocus();
                return;
            }

            if(formattedDateTime_chq.isEmpty()){
                showToastMessage("Please choose issued date..!");
                return;
            }

        } else if (paymentTypeSelection.equalsIgnoreCase("DD")) {

            if (this.uploadddPayment_entry_img_view.getDrawable() == null) {
                showToastMessage("Please " + getResources().getString(R.string.dd_photo_upload) + "!..");
                return;
            }
            String paymentRefStr = this.paymentddBankName_entry_edt.getText().toString().trim();
            if (paymentRefStr.length() == 0) {
                showToastMessage("Please Enter " + getResources().getString(R.string.bank_name_dd) + "..!");
                this.paymentddBankName_entry_edt.requestFocus();
                return;
            }

            // Limit the bank name length
            if (paymentRefStr.length() > 50) {
                showToastMessage("Bank name cannot exceed 50 characters.");
                this.paymentddBankName_entry_edt.requestFocus();
                return;
            }

            String chequeDDNumberStr = this.dd_number_edt.getText().toString().trim();
            if (chequeDDNumberStr.length() == 0) {
                showToastMessage("Please Enter " + getResources().getString(R.string.dd_no) + "..!");
                this.dd_number_edt.requestFocus();
                return;
            }

            if(formattedDateTime_dd.isEmpty()){
                showToastMessage("Please choose issued date..!");
                return;
            }

        } else if (paymentTypeSelection.equalsIgnoreCase("NEFT")) {

            if (this.uploadneftPayment_entry_img_view.getDrawable() == null) {
                showToastMessage("Please " + getResources().getString(R.string.neft_photo_upload) + "!..");
                return;
            }
            String paymentRefStr = this.paymentneftBankName_entry_edt.getText().toString().trim();
            if (paymentRefStr.length() == 0) {
                showToastMessage("Please Enter " + getResources().getString(R.string.bank_name_neft) + "..!");
                this.paymentneftBankName_entry_edt.requestFocus();
                return;
            }

            // Limit the bank name length
            if (paymentRefStr.length() > 50) {
                showToastMessage("Bank name cannot exceed 50 characters.");
                this.paymentneftBankName_entry_edt.requestFocus();
                return;
            }

            String chequeDDNumberStr = this.neft_number_edt.getText().toString().trim();
            if (chequeDDNumberStr.length() == 0) {
                showToastMessage("Please Enter " + getResources().getString(R.string.neft_no) + "..!");
                this.neft_number_edt.requestFocus();
                return;
            }

            if(formattedDateTime_neft.isEmpty()){
                showToastMessage("Please choose issued date..!");
                return;
            }

        } else if (paymentTypeSelection.equalsIgnoreCase("RTGS")) {

            if (this.uploadrtgsPayment_entry_img_view.getDrawable() == null) {
                showToastMessage("Please " + getResources().getString(R.string.rtgs_photo_upload) + "!..");
                return;
            }
            String paymentRefStr = this.paymentrtgsBankName_entry_edt.getText().toString().trim();
            if (paymentRefStr.isEmpty()) {
                showToastMessage("Please Enter " + getResources().getString(R.string.bank_name_rtgs) + "..!");
                this.paymentrtgsBankName_entry_edt.requestFocus();
                return;
            }

            // Limit the bank name length
            if (paymentRefStr.length() > 50) {
                showToastMessage("Bank name cannot exceed 50 characters.");
                this.paymentrtgsBankName_entry_edt.requestFocus();
                return;
            }

            String chequeDDNumberStr = this.rtgs_number_edt.getText().toString().trim();
            if (chequeDDNumberStr.isEmpty()) {
                showToastMessage("Please Enter " + getResources().getString(R.string.rtgs_no) + "..!");
                this.rtgs_number_edt.requestFocus();
                return;
            }

            if(formattedDateTime_rtgs.isEmpty()){
                showToastMessage("Please choose issued date..!");
                return;
            }
        }
        else if(paymentTypeSelection.equalsIgnoreCase("SMS Link") || paymentTypeSelection.equalsIgnoreCase("Dynamic QR") || paymentTypeSelection.equalsIgnoreCase("Card")){
            if(!authorized || status.equals("fail")){
                showToastMessage("Razorpay payment failed..!");
                return;
            }
        }
        int months = Integer.parseInt(monthsPayingEditText.getText().toString());

        if(months != fin_months){
            showToastMessage("No. of months have been modified!!!");
            this.monthsPayingEditText.requestFocus();
            return;
        }

        long timePart = System.currentTimeMillis(); // Current time in milliseconds
        long randomPart = ThreadLocalRandom.current().nextInt(1_000, 10_000); // Random 4 digits
        long number = timePart * 10_000 + randomPart;

        if (photoCalVal == 0) {
            sendImgStr = photoBase64Str;
            sendTransactionNoStr_QR = payment_reference_no_entry_edt.getText().toString().trim();
            sendBankNameStr = remarksAny_edt.getText().toString().trim();
        } else if (photoCalVal == 1) {
            sendImgStr = photoBase64ChqStr;
            sendTransactionNoStr = cheque_number_edt.getText().toString().trim();
            sendBankNameStr = paymentChequeBankName_entry_edt.getText().toString().trim();
            sendBankDateStr = formattedDateTime_chq;
            sendTransactionNoStr_QR = String.valueOf(number);
        } else if (photoCalVal == 3) {
            sendImgStr = photoBase64DDStr;
            sendTransactionNoStr = dd_number_edt.getText().toString().trim();
            sendBankNameStr = paymentddBankName_entry_edt.getText().toString().trim();
            sendBankDateStr = formattedDateTime_dd;
            sendTransactionNoStr_QR = String.valueOf(number);
        } else if (photoCalVal == 4) {
            sendImgStr = photoBase64NEFTStr;
            sendTransactionNoStr = neft_number_edt.getText().toString().trim();
            sendBankNameStr = paymentneftBankName_entry_edt.getText().toString().trim();
            sendBankDateStr = formattedDateTime_neft;
            sendTransactionNoStr_QR = String.valueOf(number);
        } else if (photoCalVal == 5) {
            sendImgStr = photoBase64RGTSStr;
            sendTransactionNoStr = rtgs_number_edt.getText().toString().trim();
            sendBankNameStr = paymentrtgsBankName_entry_edt.getText().toString().trim();
            sendBankDateStr = formattedDateTime_rtgs;
            sendTransactionNoStr_QR = String.valueOf(number);
        }

        if(sendTransactionNoStr_QR.isEmpty()){
            sendTransactionNoStr_QR = String.valueOf(number);
        }

        System.out.println("Image url " + sendImgStr + " " + sendTransactionNoStr + " " + sendBankNameStr);


        /*new AlertDialog.Builder(AmountCollectionEntry.this)
                .setTitle("Print Receipt")
                .setMessage("Do you want to print the receipt for the user?")
                .setPositiveButton("Print", (dialog, which) -> {
                    // Call the print function only if the user clicks "Print"
                    SharedPreferences prefs = Utility.getSharedPreferences(AmountCollectionEntry.this);
                    String uName = prefs.getString("UserName", "");
                    if (uName.contains("@")) {
                        uName = uName.substring(0, uName.indexOf("@"));
                    }
                    callPrintFun(sendTransactionNoStr, String.valueOf(SD_idStr), zoneName, wardName, house_noStr, owner_nameStr, owner_contactStr, addressStr, paymentTypeSelection, String.valueOf(fin_amount), uName, Integer.parseInt(String.valueOf(fin_months)));
//                    new Handler().postDelayed(this::callSubmitAmountCollectionData, 3000);
                })
                .setNegativeButton("Cancel", (dialog, which) -> {
//                    new Handler().postDelayed(this::callSubmitAmountCollectionData, 3000);
                    callSubmitAmountCollectionData();
                })
                .create()
                .show();*/
        callSubmitAmountCollectionData();

    }

    private File base64ToFile(String base64Str) {
        try {
            byte[] decodedString = Base64.decode(base64Str, Base64.DEFAULT);
            File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            File file = new File(getCacheDir(), "receipt_image.jpg");
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(decodedString);
            fos.flush();
            fos.close();
//            Log.d("API","Converted to image from base64");
            return file;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void deleteFile(File file) {
        if (file != null && file.exists()) {
            boolean isDeleted = file.delete();
            if (isDeleted) {
                Log.d("File Deletion", "File deleted successfully");
            } else {
                Log.d("File Deletion", "File deletion failed");
            }
        }
    }

    private ProgressDialog progressDialog;

    private void callSubmitAmountCollectionData() {
//        Log.d("API","Called submit call amount data function");

        progressDialog = new ProgressDialog(AmountCollectionEntry.this);
        progressDialog.setMessage("Submitting Data to server...");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setCancelable(false);
        progressDialog.show();
//        Log.d("API","Called submit call amount data function");


        String paymentDate = "";

        Calendar now = Calendar.getInstance();

        // Formatter to convert the time to the desired format in UTC
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));

        // Format the UTC time
        paymentDate = sdf.format(now.getTime());

        JSONObject postData = new JSONObject();

        SharedPreferences prefs = Utility.getSharedPreferences(AmountCollectionEntry.this);
        String userName = prefs.getString("UserName", "");

        File imageFile;
        if (sendImgStr != null) {
            imageFile = base64ToFile(sendImgStr);
        } else {
            imageFile = null;
        }

        String updateType = "1";
        if (establishmentStr.toLowerCase().startsWith("commercial")) {
            updateType = "4";
        } else if (establishmentStr.toLowerCase().startsWith("resident")) {
            updateType = "1";
        }

        OkHttpClient client = new OkHttpClient();
        MultipartBody.Builder multipartBuilder = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("ZoneId", zoneId)
                .addFormDataPart("WardNo", wardId)
                .addFormDataPart("Main_SDId", String.valueOf(SD_idStr))
                .addFormDataPart("HouseNo", house_noStr)
                .addFormDataPart("EstablishmentType", establishmentStr)
                .addFormDataPart("OwnerName", owner_nameStr)
                .addFormDataPart("OwnerContactNo", owner_contactStr)
                .addFormDataPart("Payment_Date", paymentDate)
                .addFormDataPart("Amount_Paid", String.valueOf(fin_amount))
                .addFormDataPart("Months_Covered", String.valueOf(fin_months))
                .addFormDataPart("Mode_of_Payment", String.valueOf(paymentType_spinPostion))
                .addFormDataPart("Payment_details", "By " + paymentTypeSelection)
                .addFormDataPart("Chq_dd_ne_rt_oth_no", sendTransactionNoStr)
                .addFormDataPart("Issuing_br_bk", sendBankNameStr)
                .addFormDataPart("Issued_date", sendBankDateStr)
                .addFormDataPart("Payment_referenceNo", sendTransactionNoStr_QR)
                .addFormDataPart("date_paid", paymentDate)
                .addFormDataPart("collected_by", userName)
                .addFormDataPart("UpdateType", updateType);

        if (imageFile != null) {
//            Log.d("API","Image present");
            multipartBuilder.addFormDataPart("paymentReceiptImage", imageFile.getName(),
                    RequestBody.create(MediaType.parse("image/*"), imageFile));
        }

//        Log.d("API","Form data "+multipartBuilder);

        MultipartBody requestBody = multipartBuilder.build();
        Request request = new Request.Builder()
                .url(api_key + "/api/PaymentsDetails/CreatePayment")
                .post(requestBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    progressDialog.dismiss();
                    Toast.makeText(AmountCollectionEntry.this, "Connection Failure: Failed to submit data", Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                /*runOnUiThread(() -> {
//                    Log.d("API", "response " + response.code());
//                    Log.d("API", "response " + response.message());
//                    Log.d("API", "response " + response);
                    progressDialog.dismiss();
                    if (response.isSuccessful()) {
                        Toast.makeText(AmountCollectionEntry.this, "Successfully Submitted...", Toast.LENGTH_SHORT).show();
                        // Navigate to another screen
                        deleteFile(imageFile);
                        //Intent intent = new Intent(getApplicationContext(), AmountCollection.class);
                        //intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        /*Intent intent = new Intent(getApplicationContext(), AmountCollection.class); // Replace with your desired class
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        intent.putExtra("zoneId", zoneId);
                        intent.putExtra("zoneName", zoneName);
                        intent.putExtra("wardId", wardId);
                        intent.putExtra("wardName", wardName);
                        startActivity(intent);
                        Intent intent = new Intent(AmountCollectionEntry.this, DashboardActivity.class);
                        intent.putExtra("zoneName", zoneName);
                        intent.putExtra("wardName", wardName);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(AmountCollectionEntry.this, "Failed to submit data with code " + response.code() + ", " + response.message(), Toast.LENGTH_SHORT).show();
                    }
                });*/
                Log.d("API", "response " + response.code());
                    Log.d("API", "response " + response.message());
                    Log.d("API", "response " + response);
                if (response.isSuccessful()) {
                    // Fetch the Receipt ID
                    fetchReceiptIDAndSendOTP();
                } else {
                    runOnUiThread(() -> {
                        progressDialog.dismiss();
                        Toast.makeText(AmountCollectionEntry.this, "Failed to submit data with code " + response.code() + ", " + response.message(), Toast.LENGTH_SHORT).show();
                    });
                }
            }
        });

    }

    private void fetchReceiptIDAndSendOTP() {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(api_key + "/PrintReceipt/" + sendTransactionNoStr_QR)
                .get()
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    progressDialog.dismiss();
                    Toast.makeText(AmountCollectionEntry.this, "Failed to fetch receipt data", Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
//                        Log.d("API",response.body().string());

                        String responseBody = response.body().string();

                        String receiptID = "";
                        if (responseBody.startsWith("Receipt ID:")) {
                            receiptID = responseBody.substring(responseBody.indexOf(":") + 1).trim();
                        } else {
                            Log.e("API", "Unexpected response format: " + responseBody);
                        }

                        sendOTPReceipt(receiptID);
                    } catch (Exception e) {
                        progressDialog.dismiss();
                        Toast.makeText(AmountCollectionEntry.this, "Data saved to database, but cannot retrieve data!", Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    }
                } else {
                    runOnUiThread(() -> {
                        progressDialog.dismiss();
                        Toast.makeText(AmountCollectionEntry.this, "Failed to fetch receipt data with code " + response.code(), Toast.LENGTH_SHORT).show();
                    });
                }
            }
        });
    }

    private void sendOTPReceipt(String receiptID) {
        OkHttpClient client = new OkHttpClient();
        JSONObject postData = new JSONObject();
        try {
            postData.put("PaymentId", receiptID);
            postData.put("Amount", fin_amount);
//            postData.put("PhoneNumber", owner_contactStr);
            postData.put("PhoneNumber", "9718775851");
            postData.put("ModeOfPayment", paymentTypeSelection);

            RequestBody requestBody = RequestBody.create(MediaType.parse("application/json"), postData.toString());
            Request request = new Request.Builder()
                    .url(api_key + "/api/PaymentsDetails/SendOTPReceipt")
                    .post(requestBody)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    e.printStackTrace();
                    runOnUiThread(() -> {
                        progressDialog.dismiss();
                        Toast.makeText(AmountCollectionEntry.this, "Failed to send OTP receipt", Toast.LENGTH_SHORT).show();
                    });
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    runOnUiThread(() -> {
                        progressDialog.dismiss();
                        if (response.isSuccessful()) {
                            Toast.makeText(AmountCollectionEntry.this, "Successfully Submitted and Message Sent to user!", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(AmountCollectionEntry.this, DashboardActivity.class);
                            intent.putExtra("zoneName", zoneName);
                            intent.putExtra("wardName", wardName);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            finish();
                        } else {
                            Toast.makeText(AmountCollectionEntry.this, "Failed to send OTP receipt with code " + response.code(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void showToastMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private int paymentType_spinPostion = 0;
    private String paymentType_spinSelectedSeasonCode = "",
            paymentType_spinSelectedFinYear = "";
    private List<String> paymentTypeList, paymentTypeCodeList, paymentTypeNameList;

    private void callPaymentTypeSpinner(SearchableSpinner paymentType_spin) {

        paymentTypeCodeList = new ArrayList<>();
        paymentTypeCodeList.add("0");
        paymentTypeNameList = new ArrayList<>();
        paymentTypeNameList.add("Select");

        paymentTypeNameList.add("QR CODE");
        paymentTypeCodeList.add("1");
        paymentTypeNameList.add("CHEQUE");
        paymentTypeCodeList.add("2");
        paymentTypeNameList.add("DD");
        paymentTypeCodeList.add("3");
        paymentTypeNameList.add("CASH");
        paymentTypeCodeList.add("4");
        paymentTypeNameList.add("NEFT");
        paymentTypeCodeList.add("5");
        paymentTypeNameList.add("RTGS");
        paymentTypeCodeList.add("6");
        paymentTypeNameList.add("SMS Link");
        paymentTypeCodeList.add("7");
        paymentTypeNameList.add("Dynamic QR");
        paymentTypeCodeList.add("8");
        paymentTypeNameList.add("Card");
        paymentTypeCodeList.add("9");


        Utility.assignArrayAdpListToSpin(AmountCollectionEntry.this, paymentTypeNameList, paymentType_spin);
        paymentType_spin.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                paymentType_spinPostion = position;
                if(amountPayingEditText.getText().toString().isEmpty()){
                    Toast.makeText(AmountCollectionEntry.this, "Please enter no. of months and click on calculate to pay", Toast.LENGTH_SHORT).show();
                    payment_reference_no_entry_edt.requestFocus();
                    return;
                }
                if(monthsPayingEditText.getText().toString().equals("0")){
                    Toast.makeText(AmountCollectionEntry.this, "Number of months is 0", Toast.LENGTH_SHORT).show();
                    payment_reference_no_entry_edt.requestFocus();
                    return;
                }
                if (paymentType_spinPostion > 0) {
                    paymentType_spinSelectedSeasonCode =
                            paymentTypeCodeList.get(paymentType_spinPostion);

                    paymentType_spinSelectedFinYear =
                            paymentTypeNameList.get(paymentType_spinPostion);

                    switch (paymentType_spinPostion) {
                        case 1:
                            paymentTypeSelection = "QR";
                            enablePaymentSelectedView(0);
                            break;

                        case 2:
                            paymentTypeSelection = "CHEQUE";
                            enablePaymentSelectedView(1);

                            break;

                        case 4:
                            paymentTypeSelection = "CASH";
                            enablePaymentSelectedView(2);

                            break;
                        case 3:
                            paymentTypeSelection = "DD";
                            enablePaymentSelectedView(3);
                            break;

                        case 5:
                            paymentTypeSelection = "NEFT";
                            enablePaymentSelectedView(4);

                            break;

                        case 6:
                            paymentTypeSelection = "RTGS";
                            enablePaymentSelectedView(5);

                            break;
                        case 7:
                            paymentTypeSelection = "SMS Link";
                            Toast.makeText(AmountCollectionEntry.this,
                                    "Razorpay SMS payment", Toast.LENGTH_SHORT).show();
//                            payWithCard();
                            payWithSMS();
                            enablePaymentSelectedView(9);
                            break;

                        case 8:
                            paymentTypeSelection = "Dynamic QR";
                            Toast.makeText(AmountCollectionEntry.this,
                                    "Razorpay Dynamic QR payment", Toast.LENGTH_SHORT).show();
                            payWithQRCode();
                            enablePaymentSelectedView(9);
//                            submit_entry_btn.setVisibility(View.VISIBLE);
                            break;

                        case 9:
                            paymentTypeSelection = "Card";
                            Toast.makeText(AmountCollectionEntry.this,
                                    "Razorpay Card payment", Toast.LENGTH_SHORT).show();
                            payWithCard();
                            enablePaymentSelectedView(9);
//                            submit_entry_btn.setVisibility(View.VISIBLE);
                            break;
                        default:
                            break;
                    }

                } else {
                    paymentType_spinSelectedSeasonCode = "";
                    paymentType_spinSelectedFinYear = "";
                    paymentType_spin.setSelection(1, true);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        paymentType_spin.setSelection(1, true);
    }

    private String filename;
    private static final int TAKE_PICTURE_ONE = 1;
    private static final int TAKE_PICTURE_CHEQUE = 2;
    private static final int TAKE_PICTURE_DD = 3;
    private static final int TAKE_PICTURE_NEFT = 4;
    private static final int TAKE_PICTURE_RTGS = 5;
    private Uri imageUri;

    private void callCameraNew(int takePictureOne, String fileName) {
       /* ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.Images.Media.TITLE, "New Picture");
        contentValues.put(MediaStore.Images.Media.DESCRIPTION, "From the camera");
        imageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);*/
       /* File filePhoto = new File(Environment.getExternalStorageDirectory(), "Pic.jpg");
        imageUri = Uri.fromFile(filePhoto);
        Utility.imageFilePath = imageUri.getPath();*/
        //Camera intent
        /*Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        startActivityForResult(cameraIntent, takePictureOne);*/

       // Create the camera_intent ACTION_IMAGE_CAPTURE it will open the camera for capture the image
        Intent camera_intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Start the activity with camera_intent, and request pic id
        startActivityForResult(camera_intent, takePictureOne);
    }

    private final static String TAG = AmountCollectionEntry.class.getSimpleName();
    private int photoCalVal = -1;
    private String photoBase64Str = "";
    private String photoBase64ChqStr = "", photoBase64DDStr = "", photoBase64NEFTStr = "", photoBase64RGTSStr = "";

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try {
            if (Utility.showLogs == 0) {
                Log.d(TAG, "resultCode " + resultCode);
            }
            if (resultCode == Activity.RESULT_OK) {

                if (requestCode == TAKE_PICTURE_ONE) {
                    if (Utility.showLogs == 0) {
//                        Log.d(TAG, "resultCode QR " + resultCode + " data " + data);
//                        Log.d(TAG, " Utility.imageFilePath QR " + Utility.imageFilePath);
                    }
                    try {
                        photoCalVal = 0;
//                        String imageURI = getContentResolver().openInputStream(imageUri).toString();
//                        Log.d(TAG, " imageURI QR " + imageURI);
//
//                        FileUtils fileUtils = new FileUtils(AmountCollectionEntry.this);
//                        String imageRealPathUtils = FileUtils.getPath(imageUri);
//                        Utility.imageFilePath = imageRealPathUtils;
//                        Log.d("TAG", " imageRealPathUtils QR " + imageRealPathUtils);
//
//                        Utility.latitudeUStr = latitudeStr;
//                        Utility.longitudeUStr = longitudeStr;
////                    new ImageCompression(AmountCollectionEntry.this).execute(imageRealPathUtils);
//
//
//                        Bitmap picture = BitmapFactory.decodeStream(getContentResolver().openInputStream(imageUri));
                        Bitmap picture = (Bitmap) data.getExtras().get("data");
                        callNewCaptureData(picture);

                    } catch (Exception e) {
                        e.printStackTrace();
                    } catch (OutOfMemoryError eee) {
                        eee.printStackTrace();
                        Toast.makeText(AmountCollectionEntry.this,
                                "Image Size Must Not Exceed 5MB QR", Toast.LENGTH_SHORT).show();
                    }
                }
                if (requestCode == TAKE_PICTURE_CHEQUE) {

                    if (Utility.showLogs == 0) {
//                        Log.d(TAG, "resultCode " + resultCode + " data " + data);
//                        Log.d(TAG, " Utility.imageFilePath " + Utility.imageFilePath);
                    }
                    try {
                        photoCalVal = 1;
                        /*String imageURI = getContentResolver().openInputStream(imageUri).toString();
                        Log.d(TAG, " imageURI " + imageURI);

                        FileUtils fileUtils = new FileUtils(AmountCollectionEntry.this);
                        String imageRealPathUtils = FileUtils.getPath(imageUri);
                        Utility.imageFilePath = imageRealPathUtils;
                        Log.d(TAG, " imageRealPathUtils " + imageRealPathUtils);

                        Utility.latitudeUStr = latitudeStr;
                        Utility.longitudeUStr = longitudeStr;
//                    new ImageCompression(AmountCollectionEntry.this).execute(imageRealPathUtils);

//                        Bitmap picture = BitmapFactory.decodeStream(getContentResolver().openInputStream(imageUri));*/
                        Bitmap picture = (Bitmap) data.getExtras().get("data");
                        callNewCaptureData(picture);

                    } catch (Exception e) {
                        e.printStackTrace();
                    } catch (OutOfMemoryError eee) {
                        eee.printStackTrace();
                        Toast.makeText(AmountCollectionEntry.this,
                                "Image Size Must Not Exceed 5MB", Toast.LENGTH_SHORT).show();
                    }
                }
                if (requestCode == TAKE_PICTURE_DD) {

                    if (Utility.showLogs == 0) {
//                        Log.d(TAG, "resultCode DD " + resultCode + " data " + data);
//                        Log.d(TAG, " Utility.imageFilePath DD " + Utility.imageFilePath);
                    }
                    try {
                        photoCalVal = 3;
//                        String imageURI = getContentResolver().openInputStream(imageUri).toString();
//                        Log.d(TAG, " imageURI DD " + imageURI);
//
//                        FileUtils fileUtils = new FileUtils(AmountCollectionEntry.this);
//                        String imageRealPathUtils = FileUtils.getPath(imageUri);
//                        Utility.imageFilePath = imageRealPathUtils;
//                        Log.d(TAG, " imageRealPathUtils DD " + imageRealPathUtils);
//
//                        Utility.latitudeUStr = latitudeStr;
//                        Utility.longitudeUStr = longitudeStr;
////                    new ImageCompression(AmountCollectionEntry.this).execute(imageRealPathUtils);
//
//                        Bitmap picture = BitmapFactory.decodeStream(getContentResolver().openInputStream(imageUri));
                        Bitmap picture = (Bitmap) data.getExtras().get("data");
                        callNewCaptureData(picture);

                    } catch (Exception e) {
                        e.printStackTrace();
                    } catch (OutOfMemoryError eee) {
                        eee.printStackTrace();
                        Toast.makeText(AmountCollectionEntry.this,
                                "Image Size Must Not Exceed 5MB DD", Toast.LENGTH_SHORT).show();
                    }
                }
                if (requestCode == TAKE_PICTURE_NEFT) {

                    if (Utility.showLogs == 0) {
//                        Log.d(TAG, "resultCode NEFT " + resultCode + " data " + data);
//                        Log.d(TAG, " Utility.imageFilePath NEFT " + Utility.imageFilePath);
                    }
                    try {
                        photoCalVal = 4;
//                        String imageURI = getContentResolver().openInputStream(imageUri).toString();
//                        Log.d(TAG, " imageURI NEFT " + imageURI);
//
//                        FileUtils fileUtils = new FileUtils(AmountCollectionEntry.this);
//                        String imageRealPathUtils = FileUtils.getPath(imageUri);
//                        Utility.imageFilePath = imageRealPathUtils;
//                        Log.d(TAG, " imageRealPathUtils NEFT " + imageRealPathUtils);
//
//                        Utility.latitudeUStr = latitudeStr;
//                        Utility.longitudeUStr = longitudeStr;
////                    new ImageCompression(AmountCollectionEntry.this).execute(imageRealPathUtils);
//
//                        Bitmap picture = BitmapFactory.decodeStream(getContentResolver().openInputStream(imageUri));
                        Bitmap picture = (Bitmap) data.getExtras().get("data");
                        callNewCaptureData(picture);

                    } catch (Exception e) {
                        e.printStackTrace();
                    } catch (OutOfMemoryError eee) {
                        eee.printStackTrace();
                        Toast.makeText(AmountCollectionEntry.this,
                                "Image Size Must Not Exceed 5MB NEFT", Toast.LENGTH_SHORT).show();
                    }
                }
                if (requestCode == TAKE_PICTURE_RTGS) {

                    if (Utility.showLogs == 0) {
//                        Log.d(TAG, "resultCode RTGS " + resultCode + " data " + data);
//                        Log.d(TAG, "RTGS Utility.imageFilePath " + Utility.imageFilePath);
                    }
                    try {
                        photoCalVal = 5;
//                        String imageURI = getContentResolver().openInputStream(imageUri).toString();
//                        Log.d(TAG, " imageURI " + imageURI);
//
//                        FileUtils fileUtils = new FileUtils(AmountCollectionEntry.this);
//                        String imageRealPathUtils = FileUtils.getPath(imageUri);
//                        Utility.imageFilePath = imageRealPathUtils;
//                        Log.d(TAG, " imageRealPathUtils RTGS " + imageRealPathUtils);
//
//                        Utility.latitudeUStr = latitudeStr;
//                        Utility.longitudeUStr = longitudeStr;
////                    new ImageCompression(AmountCollectionEntry.this).execute(imageRealPathUtils);
//
//                        Bitmap picture = BitmapFactory.decodeStream(getContentResolver().openInputStream(imageUri));
                        Bitmap picture = (Bitmap) data.getExtras().get("data");
                        callNewCaptureData(picture);

                    } catch (Exception e) {
                        e.printStackTrace();
                    } catch (OutOfMemoryError eee) {
                        eee.printStackTrace();
                        Toast.makeText(AmountCollectionEntry.this,
                                "Image Size Must Not Exceed 5MB RTGS", Toast.LENGTH_SHORT).show();
                    }
                }
                else{
                    String responseString = data.getStringExtra("response");
                    JSONObject response = new JSONObject(responseString);

                    switch (requestCode) {
                        case REQUEST_CODE_SALE_TXN:
                            handleCardPaymentResponse(response);
                            break;
//                        case REQUEST_CODE_UPI_TXN:
//                            handleUPIPaymentResponse(response);
//                            break;
                        case REQUEST_CODE_QR_CODE_TXN:
                            handleQRCodePaymentResponse(response);
                            break;
                        case REQUEST_CODE_REMOTE_PAY:
                            handleSMSPaymentResponse(response);
                            break;
                        case REQUEST_CODE_PRINT_RECEIPT:
                        case REQUEST_CODE_PRINT_BITMAP:
                            handlePrintResponse(response);
                            break;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void handlePrintResponse(JSONObject response){
        if(response.optString("status").equals("success")){
            showToastMessage(response.optString("message"));
            callSubmitAmountCollectionData();
        }
        else{
            showToastMessage("Receipt not printed!!");
        }
    }

    private void handleCardPaymentResponse(JSONObject response) {
        Log.d("Log", "QR done, " + response);
        JSONObject result = response.optJSONObject("result");
        String txnId = (result != null) ? Objects.requireNonNull(Objects.requireNonNull(response.optJSONObject("result")).optJSONObject("txn")).optString("txnId", null) : null;
//        printReceipt(txnId);
        sendTransactionNoStr = txnId;
        Log.d("Log", "QR done, " + sendTransactionNoStr);
        sendImgStr = (result != null) ? Objects.requireNonNull(result.optJSONObject("receipt")).optString("receiptUrl", null) : null;
        Log.d("Log", "QR done, " + sendImgStr);
        sendTransactionNoStr_QR = (result != null) ? Objects.requireNonNull(result.optJSONObject("references")).optString("reference1", null) : null;
        Log.d("API",sendImgStr+" "+sendTransactionNoStr);
        status = (result != null) ? response.optString("status", "Failed") : "Failed";
        Log.d("Log", "QR done, " + status);
        showAlert("Status: " + status + "\nTransaction ID: " + txnId);
        displayStatus.setVisibility(View.VISIBLE);
        if(status.equals("success")){
            Log.d("Log", "Successssssss");
            authorized = true;
        }
        displayStatus.setText("Payment "+status);
        hide_qrCodePayment_linear.setVisibility(View.GONE);
        submit_entry_btn.setVisibility(View.VISIBLE);
    }

    private void handleUPIPaymentResponse(JSONObject response) {
        Log.d("Log", "UPI done " + response);
        JSONObject result = response.optJSONObject("result");
        String txnId = (result != null) ? result.optJSONObject("references").optString("reference1", null) : null;
//        printReceipt(txnId);
        sendTransactionNoStr = txnId;
        String recipt_path = (result != null) ? result.optJSONObject("receipt").optString("receiptUrl", null) : null;
        sendImgStr = recipt_path;
        Log.d("API",sendImgStr+" "+sendTransactionNoStr);
        String status = (result != null) ? result.optString("status", "Failed") : "Failed";
        showAlert("Status: " + status + "\nTransaction ID: " + txnId);
        hide_qrCodePayment_linear.setVisibility(View.GONE);
        submit_entry_btn.setVisibility(View.VISIBLE);
    }

    @SuppressLint("SetTextI18n")
    private void handleQRCodePaymentResponse(JSONObject response) {
        Log.d("Log", "QR done, " + response);
        JSONObject result = response.optJSONObject("result");
        String txnId = (result != null) ? Objects.requireNonNull(Objects.requireNonNull(response.optJSONObject("result")).optJSONObject("txn")).optString("txnId", null) : null;
        sendTransactionNoStr = txnId;
//        printReceipt(txnId);
        Log.d("Log", "QR done, " + sendTransactionNoStr);
        sendImgStr = (result != null) ? Objects.requireNonNull(result.optJSONObject("receipt")).optString("receiptUrl", null) : null;
        Log.d("Log", "QR done, " + sendImgStr);
        sendTransactionNoStr_QR = (result != null) ? Objects.requireNonNull(result.optJSONObject("references")).optString("reference1", null) : null;
        Log.d("API",sendImgStr+" "+sendTransactionNoStr);
        status = (result != null) ? response.optString("status", "Failed") : "Failed";
        Log.d("Log", "QR done, " + status);
        showAlert("Status: " + status + "\nTransaction ID: " + txnId);
        displayStatus.setVisibility(View.VISIBLE);
        if(status.equals("success")){
            Log.d("Log", "Successssssss");
            authorized = true;
        }
        displayStatus.setText("Payment "+status);
        hide_qrCodePayment_linear.setVisibility(View.GONE);
        submit_entry_btn.setVisibility(View.VISIBLE);
    }

    @SuppressLint("SetTextI18n")
    private void handleSMSPaymentResponse(JSONObject response) {
        Log.d("Log", "QR done, " + response);
        JSONObject result = response.optJSONObject("result");
        String txnId = (result != null) ? Objects.requireNonNull(Objects.requireNonNull(response.optJSONObject("result")).optJSONObject("txn")).optString("txnId", null) : null;
        sendTransactionNoStr = txnId;
//        printReceipt(txnId);
        Log.d("Log", "QR done, " + sendTransactionNoStr);
        sendImgStr = (result != null) ? Objects.requireNonNull(result.optJSONObject("receipt")).optString("receiptUrl", null) : null;
        Log.d("Log", "QR done, " + sendImgStr);
        sendTransactionNoStr_QR = (result != null) ? Objects.requireNonNull(result.optJSONObject("references")).optString("reference1", null) : null;
        Log.d("API",sendImgStr+" "+sendTransactionNoStr);
        status = (result != null) ? response.optString("status", "Failed") : "Failed";
        Log.d("Log", "QR done, " + status);
        showAlert("Status: " + status + "\nTransaction ID: " + txnId);
        displayStatus.setVisibility(View.VISIBLE);
        if(status.equals("success")){
            Log.d("Log", "Successssssss");
            authorized = true;
        }
        displayStatus.setText("Payment "+status);
        hide_qrCodePayment_linear.setVisibility(View.GONE);
        submit_entry_btn.setVisibility(View.VISIBLE);
    }

    private void showAlert(String message) {
        new AlertDialog.Builder(this)
                .setTitle("Payment Status")
                .setMessage(message)
                .setPositiveButton("OK", null)
                .show();
    }

    private void callNewCaptureData(Bitmap bitmap) {
        try {
//            Bitmap rotationSolvedBitmap = Utility.handleSamplingAndRotationBitmap(AmountCollectionEntry.this, imageUri);
            Bitmap rotationSolvedBitmap = bitmap;

            ByteArrayOutputStream stream = new ByteArrayOutputStream();
//            if (Utility.showLogs == 0) {
//                Log.e(TAG, "Actual bitmap.getWidth(): " + bitmap.getWidth());
//                Log.e(TAG, "Actual bitmap.getHeight(): " + bitmap.getHeight());
//            }
            rotationSolvedBitmap.compress(Bitmap.CompressFormat.JPEG, 40, stream);
            stream.flush();
            stream.close();

//            if (Utility.showLogs == 0) {
//                Log.e(TAG, "rotated bitmap.getWidth(): " + rotationSolvedBitmap.getWidth());
//                Log.e(TAG, "rotated bitmap.getHeight(): " + rotationSolvedBitmap.getHeight());
//            }

            Bitmap mutableBitmap = rotationSolvedBitmap.copy(Bitmap.Config.ARGB_8888, true);

            ByteArrayOutputStream streamMutableBitmap_BAOS = new ByteArrayOutputStream();

            mutableBitmap.compress(Bitmap.CompressFormat.JPEG, 40, streamMutableBitmap_BAOS);
            streamMutableBitmap_BAOS.flush();
            streamMutableBitmap_BAOS.close();

            byte[] byteArray = streamMutableBitmap_BAOS.toByteArray();

            if (photoCalVal == 0)
                photoBase64Str = Base64.encodeToString(byteArray, Base64.DEFAULT);
            else if (photoCalVal == 1)
                photoBase64ChqStr = Base64.encodeToString(byteArray, Base64.DEFAULT);
            else if (photoCalVal == 3)
                photoBase64DDStr = Base64.encodeToString(byteArray, Base64.DEFAULT);
            else if (photoCalVal == 4)
                photoBase64NEFTStr = Base64.encodeToString(byteArray, Base64.DEFAULT);
            else if (photoCalVal == 5)
                photoBase64RGTSStr = Base64.encodeToString(byteArray, Base64.DEFAULT);

            if (Utility.showLogs == 0) {
                Log.e(TAG, "mutableBitmap bitmap.getWidth(): " + mutableBitmap.getWidth());
                Log.e(TAG, "mutableBitmap bitmap.getHeight(): " + mutableBitmap.getHeight());
            }

            if (Utility.showLogs == 0) {
                if (photoCalVal == 0)
                    Log.e(TAG, "photoBase64Str Size: " + photoBase64Str.length());
                else if (photoCalVal == 1)
                    Log.e(TAG, "photoBase64ChqStr Size: " + photoBase64ChqStr.length());
                else if (photoCalVal == 3)
                    Log.e(TAG, "photoBase64DDStr Size: " + photoBase64DDStr.length());
                else if (photoCalVal == 4)
                    Log.e(TAG, "photoBase64NEFTStr Size: " + photoBase64NEFTStr.length());
                else if (photoCalVal == 5)
                    Log.e(TAG, "photoBase64RGTSStr Size: " + photoBase64RGTSStr.length());
                // Log.e(TAG, "photoBase64Str : " + photoBase64Str);
            }

//            saveCapturedPhoto(mutableBitmap);

            if (photoCalVal == 0) {
                // pic_beneficiary_with_animal_imageview.setImageBitmap(bitmap);
                upload_payment_transaction_id_entry_img_view.setImageBitmap(mutableBitmap);
                upload_payment_transaction_id_entry_img_view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        filename = "Testing_" + System.currentTimeMillis();
                        callCameraNew(TAKE_PICTURE_ONE, filename);
                    }
                });
                hide_ln_lay_qr_photo.setVisibility(View.GONE);

            } else if (photoCalVal == 1) {
                uploadChequePayment_entry_img_view.setImageBitmap(mutableBitmap);
                uploadChequePayment_entry_img_view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        filename = "Testing_Chq_" + System.currentTimeMillis();
                        callCameraNew(TAKE_PICTURE_CHEQUE, filename);
                    }
                });
                hide_ln_lay_cheque_photo.setVisibility(View.GONE);
            } else if (photoCalVal == 3) {
                uploadddPayment_entry_img_view.setImageBitmap(mutableBitmap);
                uploadddPayment_entry_img_view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        filename = "Testing_DD_" + System.currentTimeMillis();
                        callCameraNew(TAKE_PICTURE_DD, filename);
                    }
                });
                hide_ln_lay_dd_photo.setVisibility(View.GONE);
            } else if (photoCalVal == 4) {
                uploadneftPayment_entry_img_view.setImageBitmap(mutableBitmap);
//                uploadneftPayment_entry_img_view
                hide_ln_lay_neft_photo.setVisibility(View.GONE);
            } else if (photoCalVal == 5) {
                uploadrtgsPayment_entry_img_view.setImageBitmap(mutableBitmap);
//                uploadrtgsPayment_entry_img_view
                hide_ln_lay_rtgs_photo.setVisibility(View.GONE);
            }

            submit_entry_btn.setVisibility(View.VISIBLE);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return;
        // }
    }

    public void saveCapturedPhoto(Bitmap mBitmap) {

        String filename;
        Date date = new Date(0);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        filename = sdf.format(date);
        try {
            String path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString();
            OutputStream fOut = null;
            File file = new File(getCacheDir(), filename + ".jpg");
            fOut = new FileOutputStream(file);
            mBitmap.compress(Bitmap.CompressFormat.JPEG, 60, fOut);
            fOut.flush();
            fOut.close();

//            MediaStore.Images.Media.insertImage(getContentResolver()
//                    , file.getAbsolutePath(), file.getName(), file.getName());

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private static final int STORAGE_PERMISSION_CODE = 123;

    private void requestStoragePermission() {

        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {
            //If the user has denied the permission previously your code will come to this block
            //Here you can explain why you need this permission
            //Explain here why you need this permission
            Toast.makeText(this, "You need to allow the permissions asked in order to work with this application", Toast.LENGTH_LONG).show();
        }
        //And finally ask for the permission

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{
                            Manifest.permission.CAMERA,
                            Manifest.permission.INTERNET,
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION},
                    STORAGE_PERMISSION_CODE);
        } else {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{
                            Manifest.permission.CAMERA,
                            Manifest.permission.INTERNET,
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.ACCESS_COARSE_LOCATION},
                    STORAGE_PERMISSION_CODE);
        }

        if (allPermissionsGranted) {
            Utility.isGrantedPermissionWRITE_EXTERNAL_STORAGE(AmountCollectionEntry.this);
            isLocationServicesThere();
        }

    }

    boolean allPermissionsGranted = true;

    //This method will be called when the user will tap on allow or deny
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        //If the request code does not match

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);


        /*if (requestCode == MY_PERMISSIONS_REQUEST_SEND_SMS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, now send the SMS
                Toast.makeText(this, "SMS permission granted.", Toast.LENGTH_SHORT).show();
            } else {
                // Permission denied
                Toast.makeText(this, "SMS permission denied.", Toast.LENGTH_SHORT).show();
            }
        }*/

        if (permissions.length == 0) {
            return;
        }
        allPermissionsGranted = true;
        if (grantResults.length > 0) {
            for (int grantResult : grantResults) {
                if (grantResult != PackageManager.PERMISSION_GRANTED) {
                    allPermissionsGranted = false;
                    break;
                }
            }
        }
        if (!allPermissionsGranted) {
            boolean somePermissionsForeverDenied = false;
            for (String permission : permissions) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {
                    //denied
                    Log.e("denied", permission);
                } else {
                    if (ActivityCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED) {
                        //allowed
                        Log.e("allowed", permission);
                    } else {
                        //set to never ask again
                        Log.e("set to never ask again", permission);
                        somePermissionsForeverDenied = true;
                    }
                }
            }
            if (somePermissionsForeverDenied) {
                final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
                alertDialogBuilder.setTitle("Permissions Required")
                        .setMessage("You have forcefully denied some of the required permissions " +
                                "for this action. Please open settings, go to permissions and allow them.")
                        .setPositiveButton("Settings", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                        Uri.fromParts("package", getPackageName(), null));
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        })
                        .setCancelable(false)
                        .create()
                        .show();
            }
        } else {
            switch (requestCode) {
                //act according to the request code used while requesting the permission(s).
            }
        }
    }

    private boolean locationEnbled = false;
    /**
     * FusedLocationProviderApi Save request parameters
     */
    private LocationRequest mLocationRequest;
    /**
     * Provide callbacks for location events.
     */
    private LocationCallback mLocationCallback;
    /**
     * An object representing the current location
     */
    private Location mCurrentLocation;
    //A client that handles connection / connection failures for Google locations
    // (changed from play-services 11.0.0)
    private FusedLocationProviderClient mFusedLocationClient;
    private String provider, latitudeStr = "0.0", longitudeStr = "0.0";
    private boolean isNetworkLocation, isGPSLocation;

    private void isLocationServicesThere() {

        locationEnbled = false;
        LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        boolean gps_enabled = false;
        boolean network_enabled = false;

        try {
            gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch (Exception ex) {
        }

        try {
            network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch (Exception ex) {
        }

        if (!gps_enabled && !network_enabled) {
            // notify user
            AlertDialog.Builder dialog = new AlertDialog.Builder(AmountCollectionEntry.this);
            dialog.setMessage("Enable GPS Settings").setCancelable(false).
                    setPositiveButton("Open Settings", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                            // TODO Auto-generated method stub
                            Intent myIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            startActivity(myIntent);
                            //get gps
                        }
                    });
            /*.setNegativeButton("Cancle", new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                    // TODO Auto-generated method stub

                }
            })*/
            dialog.show();
        } else {


            locationEnbled = true;
            getLocationFusedClient();
        }
    }

    private void getLocationFusedClient() {
        LocationManager mListener = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (mListener != null) {
            isGPSLocation = mListener.isProviderEnabled(LocationManager.GPS_PROVIDER);
            isNetworkLocation = mListener.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            Log.e("gps, network", String.valueOf(isGPSLocation + "," + isNetworkLocation));
        }

        if (isGPSLocation) {

            provider = LocationManager.GPS_PROVIDER;
                   /* Intent intent = new Intent(SplashScreen.this, Login.class);
                    intent.putExtra("provider", LocationManager.GPS_PROVIDER);
                    startActivity(intent);
                    finish();*/
        } else if (isNetworkLocation) {
            provider = LocationManager.NETWORK_PROVIDER;
        } else {
            //Device location is not set
            PermissionUtils.LocationSettingDialog.newInstance().show(getSupportFragmentManager(), "Setting");
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            checkMyPermissionLocation();
        }

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        /**
         * Location Setting API to
         */
        SettingsClient mSettingsClient = LocationServices.getSettingsClient(this);
        /*
         * Callback returning location result
         */
        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult result) {
                super.onLocationResult(result);
                //mCurrentLocation = locationResult.getLastLocation();
                mCurrentLocation = result.getLocations().get(0);


                if (mCurrentLocation != null) {


                    Log.e("Location(Lat)==", "" + mCurrentLocation.getLatitude());
                    Log.e("Location(Long)==", "" + mCurrentLocation.getLongitude());


                    latitudeStr = "" + mCurrentLocation.getLatitude();
                    longitudeStr = "" + mCurrentLocation.getLongitude();

                    //latitudeTxt.setText("Latitude: "+latitudeStr);
                    // longitudeTxt.setText("Longitude: "+longitudeStr);
                }
                /**
                 * To get location information consistently
                 * mLocationRequest.setNumUpdates(1) Commented out
                 * Uncomment the code below
                 */
                mFusedLocationClient.removeLocationUpdates(mLocationCallback);
            }

            //Locatio nMeaning that all relevant information is available
            @Override
            public void onLocationAvailability(LocationAvailability availability) {
                //boolean isLocation = availability.isLocationAvailable();
            }
        };
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(5000);
        mLocationRequest.setFastestInterval(5000);
        //To get location information only once here
        mLocationRequest.setNumUpdates(3);
        if (provider.equalsIgnoreCase(LocationManager.GPS_PROVIDER)) {
            //Accuracy is a top priority regardless of battery consumption
            mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        } else {
            //Acquired location information based on balance of battery and accuracy (somewhat higher accuracy)
            mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        }

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest);
        /**
         * Stores the type of location service the client wants to use. Also used for positioning.
         */
        LocationSettingsRequest mLocationSettingsRequest = builder.build();

        Task<LocationSettingsResponse> locationResponse = mSettingsClient.checkLocationSettings(mLocationSettingsRequest);
        locationResponse.addOnSuccessListener(this, new OnSuccessListener<LocationSettingsResponse>() {
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                Log.e("Response", "Successful acquisition of location information!!");

                if (ActivityCompat.checkSelfPermission(AmountCollectionEntry.this, Manifest.permission.ACCESS_FINE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
            }
        });
        //When the location information is not set and acquired, callback
        locationResponse.addOnFailureListener(this, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                int statusCode = ((ApiException) e).getStatusCode();
                switch (statusCode) {
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        Log.e("onFailure", "Location environment check");
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        String errorMessage = "Check location setting";
                        Log.e("onFailure", errorMessage);
                }
            }
        });


    }

    private void checkMyPermissionLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            //Permission Check
            PermissionUtils.requestPermission(this);
        }
    }

    /**
     * Remove location information
     */
    @Override
    public void onStop() {
        super.onStop();
        if (mFusedLocationClient != null) {
            mFusedLocationClient.removeLocationUpdates(mLocationCallback);
        }
    }


    @Override
    protected void onPause() {
        super.onPause();

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == android.R.id.home) {
            // finish the activity
            onBackPressed();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private GPSReceiver gpsReceivers;

    private void enableGPSReceiver() {
        GPSReceiver gPSReceiver = new GPSReceiver();
        this.gpsReceivers = gPSReceiver;
        registerReceiver(gPSReceiver, new IntentFilter("android.location.PROVIDERS_CHANGED"));
        try {
            if (!Utility.isGPSEnabled1(this)) {
                Utility.checkGPSEnabled(this);
            } else if (hasLocationPermission()) {
                getLocation_two();
            } else {
                requestLocationPermission();
            }
        } catch (ArrayIndexOutOfBoundsException | NullPointerException | NumberFormatException e) {
            e.printStackTrace();
        } catch (Exception e2) {
            e2.printStackTrace();
        }
    }

    private boolean hasLocationPermission() {
        return ContextCompat.checkSelfPermission(getApplicationContext(), "android.permission.ACCESS_FINE_LOCATION") == 0 && ContextCompat.checkSelfPermission(getApplicationContext(), "android.permission.ACCESS_FINE_LOCATION") == 0;
    }

    /* access modifiers changed from: private */
    public void requestLocationPermission() {
        Dexter.withContext(this).withPermissions("android.permission.ACCESS_FINE_LOCATION",
                "android.permission.ACCESS_COARSE_LOCATION").withListener(new MultiplePermissionsListener() {
            public void onPermissionsChecked(MultiplePermissionsReport report) {
                if (report.areAllPermissionsGranted()) {
                    getLocation_two();
                } else if (report.isAnyPermissionPermanentlyDenied()) {
                    showSettingsDialog();
                } else {
                    showPermissionRationale();
                }
            }

            public void onPermissionRationaleShouldBeShown(List<PermissionRequest> list, PermissionToken token) {
                token.continuePermissionRequest();
            }
        }).check();
    }

    public void showSettingsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Required Permissions");
        builder.setMessage("This app require permission to use awesome feature. Grant them in app settings.");
        builder.setPositiveButton("go_to_settings", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int i) {
                dialog.cancel();
                openSettings();
            }
        });
        builder.setNegativeButton(getString(R.string.action_settings), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int i) {
                dialog.cancel();
            }
        });
        builder.show();
    }

    /* access modifiers changed from: private */
    public void showPermissionRationale() {
        new AlertDialog.Builder(this).setTitle((CharSequence) "Permission Required").setMessage((CharSequence) "We need the location permission.").setPositiveButton((CharSequence) "OK", (DialogInterface.OnClickListener) new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                requestLocationPermission();
            }
        }).setNegativeButton((CharSequence) "Cancel", (DialogInterface.OnClickListener) null).show();
    }


    public void openSettings() {
        Intent intent = new Intent("android.settings.APPLICATION_DETAILS_SETTINGS");
        intent.setData(Uri.fromParts("package", getPackageName(), (String) null));
        startActivityForResult(intent, 101);
    }


    public void getLocation_two() {
        try {
            GpsTracker gpsTracker2 = new GpsTracker(this);
            this.gpsTracker = gpsTracker2;
            if (gpsTracker2.canGetLocation()) {
                double latitude = this.gpsTracker.getLatitude();
                double longitude = this.gpsTracker.getLongitude();
                this.current_lat = String.valueOf(latitude);
                this.current_lng = String.valueOf(longitude);
                String fullAddress = getAddressFromLatLng(latitude, longitude);
                //  this.address.setText(fullAddress);
//                this.lat_long_entry_edt.setText(this.current_lat + " , " + this.current_lng);
                Log.e(TAG, "Location lat & long values: " + this.current_lat + ", " + this.current_lng);
                Log.e(TAG, "Full address: " + fullAddress);
            }

            requestStoragePermission();
        } catch (IndexOutOfBoundsException | NullPointerException | NumberFormatException e) {
            e.printStackTrace();
        }
    }

    private String getAddressFromLatLng(double latitude, double longitude) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        StringBuilder addressString = new StringBuilder();
        try {
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            if (addresses == null || addresses.isEmpty()) {
                addressString.append("Unable to find address for the location.");
                return addressString.toString().trim();
            }
            Address address2 = addresses.get(0);
            for (int i = 0; i <= address2.getMaxAddressLineIndex(); i++) {
                addressString.append(address2.getAddressLine(i)).append("\n");
            }
            return addressString.toString().trim();
        } catch (IOException e) {
            e.printStackTrace();
            addressString.append("Error retrieving address: ").append(e.getMessage());
        }
        return addressString.toString().trim();
    }

    private void callPrintFun(String transactionId, String consumerNumber, String zoneNumber,
                              String wardName, String houseNumber, String name, String mobileNumber,
                              String address, String paymentMode, String amountPaid,
                              String collectorName, int numberOfMonths) {
        try {
            Log.d("APP", "Called printing statement");

            // Receipt parameters
            String tollFreeNumber = "18001805821";
            String currentDate = new java.text.SimpleDateFormat("dd-MMM-yyyy HH:mm:ss").format(new java.util.Date());
            java.util.Calendar calendar = java.util.Calendar.getInstance();

            // Dues Upto: Current month
            String duesUpto = new java.text.SimpleDateFormat("MMM-yyyy").format(calendar.getTime());

            // Dues From: Previous months
            calendar.add(java.util.Calendar.MONTH, -(numberOfMonths - 1));
            String duesFrom = new java.text.SimpleDateFormat("MMM-yyyy").format(calendar.getTime());

            // Set up Paint for text
            Paint paint = new TextPaint();
            paint.setColor(Color.BLACK);
            paint.setTextSize(20);
            paint.setTypeface(Typeface.MONOSPACE);
            int lineSpacing = 30; // Line spacing
            int y = 20; // Starting y position

            // Dynamically calculate total height of the bitmap
            int bitmapWidth = 384; // Fixed width
            int estimatedHeight = y;

            // Pre-calculate height for dynamic content
            estimatedHeight += lineSpacing * 3; // For header
            estimatedHeight += lineSpacing; // For dashed line
            estimatedHeight += lineSpacing * 2; // For Tollfree text and dashed line
            estimatedHeight += calculateKeyValueHeight("Trans Date", currentDate, paint, bitmapWidth, lineSpacing);
            estimatedHeight += calculateKeyValueHeight("Trans No", transactionId, paint, bitmapWidth, lineSpacing);
            estimatedHeight += calculateKeyValueHeight("Consumer No", consumerNumber, paint, bitmapWidth, lineSpacing);
            estimatedHeight += calculateKeyValueHeight("Zone", zoneNumber, paint, bitmapWidth, lineSpacing);
            estimatedHeight += calculateKeyValueHeight("Ward", wardName, paint, bitmapWidth, lineSpacing);
            estimatedHeight += calculateKeyValueHeight("House No", houseNumber, paint, bitmapWidth, lineSpacing);
            estimatedHeight += calculateKeyValueHeight("Name", name, paint, bitmapWidth, lineSpacing);
            estimatedHeight += calculateKeyValueHeight("Mobile No", mobileNumber, paint, bitmapWidth, lineSpacing);
            estimatedHeight += calculateKeyValueHeight("Address", address, paint, bitmapWidth, lineSpacing);
            estimatedHeight += calculateKeyValueHeight("Dues From", duesFrom, paint, bitmapWidth, lineSpacing);
            estimatedHeight += calculateKeyValueHeight("Dues Upto", duesUpto, paint, bitmapWidth, lineSpacing);
            estimatedHeight += calculateKeyValueHeight("Payment Mode", paymentMode, paint, bitmapWidth, lineSpacing);
            estimatedHeight += calculateKeyValueHeight("Amount Paid", "Rs. " + amountPaid, paint, bitmapWidth, lineSpacing);
            estimatedHeight += lineSpacing * 7; // Footer

            // Create Bitmap with calculated height
            Bitmap receiptBitmap = Bitmap.createBitmap(bitmapWidth, estimatedHeight, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(receiptBitmap);
            canvas.drawColor(Color.WHITE);

            // Draw Header
            paint.setTextSize(24);
            paint.setTypeface(Typeface.create(Typeface.DEFAULT_BOLD, Typeface.BOLD));
            drawCenteredText(canvas, "Lucknow Municipal Corporation", paint, y, lineSpacing);
            y += lineSpacing;
            drawCenteredText(canvas, "Solid Waste User Charge", paint, y, lineSpacing);
            y += lineSpacing;
            drawCenteredText(canvas, "Payment Receipt", paint, y, lineSpacing);
            y += lineSpacing;

            // Dashed line
            drawDashedLine(canvas, paint, bitmapWidth, y);
            y += lineSpacing;

            // Toll-free number
            paint.setTextSize(22);
            paint.setTypeface(Typeface.MONOSPACE);
            drawCenteredText(canvas, "Tollfree No: " + tollFreeNumber, paint, y, lineSpacing);
            y += lineSpacing;

            // Dashed line below Tollfree
            drawDashedLine(canvas, paint, bitmapWidth, y);
            y += lineSpacing;

            // Draw Key-Value Pairs
            y = drawKeyValue(canvas, "Trans Date", currentDate, paint, bitmapWidth, y, lineSpacing);
            y = drawKeyValue(canvas, "Trans No", transactionId, paint, bitmapWidth, y, lineSpacing);
            y = drawKeyValue(canvas, "Consumer No", consumerNumber, paint, bitmapWidth, y, lineSpacing);
            y = drawKeyValue(canvas, "Zone", zoneNumber, paint, bitmapWidth, y, lineSpacing);
            y = drawKeyValue(canvas, "Ward", wardName, paint, bitmapWidth, y, lineSpacing);
            y = drawKeyValue(canvas, "House No", houseNumber, paint, bitmapWidth, y, lineSpacing);
            y = drawKeyValue(canvas, "Name", name, paint, bitmapWidth, y, lineSpacing);
            y = drawKeyValue(canvas, "Mobile No", mobileNumber, paint, bitmapWidth, y, lineSpacing);
            y = drawKeyValue(canvas, "Address", address, paint, bitmapWidth, y, lineSpacing);
            y = drawKeyValue(canvas, "Dues From", duesFrom, paint, bitmapWidth, y, lineSpacing);
            y = drawKeyValue(canvas, "Dues Upto", duesUpto, paint, bitmapWidth, y, lineSpacing);
            y = drawKeyValue(canvas, "Payment Mode", paymentMode, paint, bitmapWidth, y, lineSpacing);
            y = drawKeyValue(canvas, "Amount Paid", "Rs. " + amountPaid, paint, bitmapWidth, y, lineSpacing);

            drawDashedLine(canvas, paint, bitmapWidth, y);
            y += lineSpacing;

            y = drawKeyValue(canvas, "TC Name", collectorName, paint, bitmapWidth, y, lineSpacing);

            drawDashedLine(canvas, paint, bitmapWidth, y);
            y += lineSpacing;

            paint.setTextSize(20);
            paint.setTypeface(Typeface.MONOSPACE);

            // Footer
            drawCenteredText(canvas, "For more details, visit", paint, y, lineSpacing);
            y += lineSpacing;
            drawCenteredText(canvas, "https://www.lucknowsmartcity.com", paint, y, lineSpacing);
            y += lineSpacing;
            drawCenteredText(canvas, "Thank you!", paint, y, lineSpacing);

            // Encode and Print
            String encodedImage = getEncoded64ImageStringFromBitmap(receiptBitmap);
            JSONObject jsonRequest = new JSONObject();
            JSONObject jsonImageObj = new JSONObject();
            jsonImageObj.put("imageData", encodedImage);
            jsonImageObj.put("imageType", "JPEG");
            jsonRequest.put("image", jsonImageObj);

            EzeAPI.printBitmap(this, REQUEST_CODE_PRINT_BITMAP, jsonRequest);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void drawDashedLine(Canvas canvas, Paint paint, int width, int y) {
        paint.setPathEffect(new DashPathEffect(new float[]{10, 10}, 0));
        canvas.drawLine(0, y, width, y, paint);
        paint.setPathEffect(null);
    }

    private void drawCenteredText(Canvas canvas, String text, Paint paint, int y, int lineSpacing) {
        canvas.drawText(text, (canvas.getWidth() - paint.measureText(text)) / 2, y, paint);
    }

    private int calculateKeyValueHeight(String key, String value, Paint paint, int bitmapWidth, int lineSpacing) {
        float colonX = bitmapWidth / 2;
        float keyWidth = paint.measureText(key);
        float valueX = colonX + 10;
        float maxWidth = bitmapWidth - valueX;
        int totalHeight = 1;
        String remainingText = value;

        while (!remainingText.isEmpty()) {
            int charsToFit = paint.breakText(remainingText, true, maxWidth, null);
            String textToDraw = remainingText.substring(0, charsToFit).trim();
            remainingText = remainingText.substring(charsToFit).trim();
            totalHeight += lineSpacing;
        }
        return totalHeight;
    }

    private String getEncoded64ImageStringFromBitmap(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(byteArray, Base64.NO_WRAP); // Use NO_WRAP to avoid unwanted characters
    }

    // Updated drawKeyValue
    private int drawKeyValue(Canvas canvas, String key, String value, Paint paint, int bitmapWidth, int y, int lineSpacing) {
        float colonX = bitmapWidth / 2;
        float keyWidth = paint.measureText(key);
        float keyX = colonX - keyWidth - 10;

        paint.setTextAlign(Paint.Align.LEFT);
        canvas.drawText(key, keyX, y, paint);
        canvas.drawText(":", colonX, y, paint);

        float valueX = colonX + 10;
        float maxWidth = bitmapWidth - valueX;
        String remainingText = value;

        while (!remainingText.isEmpty()) {
            int charsToFit = paint.breakText(remainingText, true, maxWidth, null);
            String textToDraw = remainingText.substring(0, charsToFit).trim();

            // Handle word splits to maintain readability
            if (charsToFit < remainingText.length() && !Character.isWhitespace(remainingText.charAt(charsToFit))) {
                int lastSpace = textToDraw.lastIndexOf(' ');
                if (lastSpace != -1) {
                    charsToFit = lastSpace;
                    textToDraw = remainingText.substring(0, charsToFit).trim();
                }
            }

            // Draw text and adjust y for the next line
            canvas.drawText(textToDraw, valueX, y, paint);
            y += lineSpacing;
            remainingText = remainingText.substring(charsToFit).trim();
        }

        return y;
    }


//    private String main_url = "https://ramkyapi.beulahsoftware.com/api/PaymentsDetails/", urlReg = "CustomerPaymentDetails";
//
//    //"https://ramesharjampudi-001-site1.ltempurl.com/Api/User/"

    @Override
    public void onBackPressed() {
        super.onBackPressed();

//        startActivity(new Intent(getApplicationContext(), AmountCollection.class));
        finish();
    }
}