package com.org.lsa;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.org.lsa.custom.Utility;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Login extends AppCompatActivity implements View.OnClickListener {
    EditText et_mail;
    EditText et_pass;
    EditText et_otp;
    Button loginBtn;
    Button otpBtn;
    LinearLayout mobileLayout;
    LinearLayout otpLayout;
    TextView tvOtp;

    String userName, password, otp,otp_temp;

    private String api_key = "http://45.114.246.201:6005";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        this.et_mail = (EditText) findViewById(R.id.et_mail);
        this.et_pass = (EditText) findViewById(R.id.et_password);
        this.et_otp = (EditText) findViewById(R.id.et_otp);
        this.mobileLayout = (LinearLayout) findViewById(R.id.layoutMobile);
        this.otpLayout = (LinearLayout) findViewById(R.id.otp_et);
        this.loginBtn = (Button) findViewById(R.id.login_btn);
        this.otpBtn = (Button) findViewById(R.id.otp_btn);
        this.tvOtp = (TextView) findViewById(R.id.appVersion);

        loginBtn.setOnClickListener(this);
        otpBtn.setOnClickListener(this);
//        et_mail.setText("Bilal.ansari");
//        et_mail.setText("sonu.kumar");
//        et_pass.setText("Lucknow");
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.login_btn:
                hideKeyboard();
                if (!validation()) {
                    return;
                }
                if (isNetworkAvailable(this)) {

                    progressDialog = new ProgressDialog(Login.this);
                    progressDialog.setMessage("Authenticating...");
                    progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                    progressDialog.setCancelable(false);
                    progressDialog.show();
                    verifyPasswordAsync(userName,password);
                    /*new CountDownTimer(1000, 1000) {
                        @Override
                        public void onTick(long l) {

                        }

                        @Override
                        public void onFinish() {
                            if (progressDialog.isShowing())
                                progressDialog.dismiss();
                            SharedPreferences.Editor editor = Utility.getSharedPreferences(Login.this).edit();
                            editor.putString("UserName", et_mail.getText().toString().trim());
                            editor.putString("status", "1");
                            editor.commit();
                            Intent intent = new Intent(getApplicationContext(), DashboardActivity.class);
                            startActivity(intent);
                            finish();
                        }
                    }.start();*/

                    return;
                } else {
                    showToastMessage("Please check the internet connection! ");
                    return;
                }
            case R.id.otp_btn:
                hideKeyboard();
                otp = this.et_otp.getText().toString().trim();
                if (TextUtils.isEmpty(otp)) {
                    showToastMessage("Enter Your OTP ..!");
                    this.et_otp.requestFocus();
                    return;
                }

                if (isNetworkAvailable(this)) {

                    progressDialog = new ProgressDialog(Login.this);
                    progressDialog.setMessage("Authenticating...");
                    progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                    progressDialog.setCancelable(false);
                    progressDialog.show();
                    validateOtpAsync(userName,password,otp);
                }
                else {
                    showToastMessage("Please check the internet connection! ");
                }
                return;

            default:
                return;
        }
    }

    private void verifyPasswordAsync(String loginId, String password) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            String response = verifyPassword(loginId, password);
            Log.d("VerifyPassword", "response code " + response);
            runOnUiThread(() -> parsePasswordResponse(response));
        });
    }

    private void validateOtpAsync(String loginId, String passwordHash, String otp) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            String response = validateOtp(loginId, passwordHash, otp);
            Log.d("VerifyPassword", "response code " + response);
            runOnUiThread(() -> parseOTPResponse(response));
        });
    }


    private boolean validation() {
        userName = this.et_mail.getText().toString().trim();
        password = this.et_pass.getText().toString().trim();
        if (TextUtils.isEmpty(userName)) {
            showToastMessage("Enter Your UserName ..!");
            this.et_mail.requestFocus();
            return false;
        } else if (!TextUtils.isEmpty(password)) {
            return true;
        } else {
            showToastMessage("Enter Your Password...!");
            this.et_pass.requestFocus();
            return false;
        }
    }

    private String verifyPassword(String loginId, String password) {
        String urlString = api_key+"/api/PaymentsDetails/VerifyPWD";
        HttpURLConnection httpURLConnection = null;
        try {
            URL url = new URL(urlString);
            httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.setRequestProperty("Accept-Charset", "UTF-8");
            httpURLConnection.setRequestProperty("Content-Type", "application/json");
            httpURLConnection.setRequestMethod("POST");
            httpURLConnection.setDoOutput(true);

            // Prepare the JSON payload
            JSONObject jsonPayload = new JSONObject();
            jsonPayload.put("LoginID", loginId);
            jsonPayload.put("Password", password);

            Log.d("VerifyPassword", "json object " + jsonPayload);

            OutputStream os = httpURLConnection.getOutputStream();
            os.write(jsonPayload.toString().getBytes(StandardCharsets.UTF_8));
            os.close();

            int responseCode = httpURLConnection.getResponseCode();
            Log.d("VerifyPassword", "response code " + responseCode);

            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    response.append(line);
                }
                bufferedReader.close();
                return response.toString();
            } else {
                return "0";
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "0";
        } finally {
            if (httpURLConnection != null) {
                httpURLConnection.disconnect();
            }
        }
    }

    private void parsePasswordResponse(String response){
        try{
            if(response.equals("0")){
                showToastMessage("Something went wrong, please try again");
                if (progressDialog.isShowing())
                    progressDialog.dismiss();
            }
            else{
                String cleanedResponse = response.trim();
                JSONObject jsonObject = new JSONObject(
                        cleanedResponse.replace("\\\"", "\"")
                );
                String msg = jsonObject.getString("message");
//                otp_temp = jsonObject.getString("otp");

                if(msg.equals("Login successful. OTP sent.")){
                    showToastMessage("Credentials verified, please entre OTP");
                    loginBtn.setVisibility(View.GONE);
                    if (progressDialog.isShowing())
                        progressDialog.dismiss();
                    tvOtp.setVisibility(View.VISIBLE);
                    otpLayout.setVisibility(View.VISIBLE);
                    otpBtn.setVisibility(View.VISIBLE);

//                    this.et_otp.setText(otp_temp);
                }

            }

        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    // This method will call the /validate-otp API
    private String validateOtp(String loginId, String passwordHash, String otp) {
        String urlString = api_key+"/api/PaymentsDetails/validate-otp";
        HttpURLConnection httpURLConnection = null;
        try {
            URL url = new URL(urlString);
            httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.setRequestProperty("Accept-Charset", "UTF-8");
            httpURLConnection.setRequestProperty("Content-Type", "application/json");
            httpURLConnection.setRequestMethod("POST");
            httpURLConnection.setDoOutput(true);

            // Prepare the JSON payload
            JSONObject jsonPayload = new JSONObject();
            jsonPayload.put("LoginID", loginId);
            jsonPayload.put("PasswordHash", passwordHash);
            jsonPayload.put("OTP", otp);

//            Log.d("VerifyPassword", "response code " + jsonPayload);

            OutputStream os = httpURLConnection.getOutputStream();
            os.write(jsonPayload.toString().getBytes(StandardCharsets.UTF_8));
            os.close();

            int responseCode = httpURLConnection.getResponseCode();
//            Log.d("VerifyPassword", "response code " + responseCode);

            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    response.append(line);
                }
                bufferedReader.close();
                return response.toString();
            } else {
                return "0";
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "0";
        } finally {
            if (httpURLConnection != null) {
                httpURLConnection.disconnect();
            }
        }
    }

    private void parseOTPResponse(String response){
        try{
            if(response.equals("0")){
                showToastMessage("Something went wrong, please try again");
                if (progressDialog.isShowing())
                    progressDialog.dismiss();
            }
            else{
                String cleanedResponse = response.trim();
                JSONObject jsonObject = new JSONObject(
                        cleanedResponse.replace("\\\"", "\"")
                );
                String msg = jsonObject.getString("message");

                if(msg.equals("OTP validation successful. User authenticated.")){
                    showToastMessage(msg);
                    if (progressDialog.isShowing())
                        progressDialog.dismiss();
                    SharedPreferences.Editor editor = Utility.getSharedPreferences(Login.this).edit();
                    editor.putString("UserName", userName);
//                    editor.putString("status", "1");
                    editor.putString("status", ""); //Temporary change
                    editor.commit();
                    Intent intent = new Intent(getApplicationContext(), DashboardActivity.class);
                    startActivity(intent);
                    finish();
                }
                else{
                    showToastMessage(msg);
                }

            }

        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean isNetworkAvailable(Context context) {
        NetworkInfo activeNetworkInfo2 = ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
        return activeNetworkInfo2 != null && activeNetworkInfo2.isConnected();
    }

    public void showToastMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private ProgressDialog progressDialog;
    private Dialog dialog;

    public void hideKeyboard() {
        InputMethodManager imm;
        View view = getCurrentFocus();
        if (view != null && (imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE)) != null) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    /* access modifiers changed from: protected */
    public void onPause() {
        super.onPause();
        Dialog dialog2 = this.dialog;
        if (dialog2 != null && dialog2.isShowing()) {
            this.dialog.dismiss();
        }
    }

    /* access modifiers changed from: protected */
    public void onDestroy() {
        super.onDestroy();
        Dialog dialog2 = this.dialog;
        if (dialog2 != null && dialog2.isShowing()) {
            this.dialog.dismiss();
        }
    }

}