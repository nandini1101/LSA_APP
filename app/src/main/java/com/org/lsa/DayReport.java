package com.org.lsa;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.org.lsa.Interface.ItemClickListener;
import com.org.lsa.custom.Utility;
import com.org.lsa.model.DayEndCollection;


import org.json.JSONArray;

import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class DayReport extends AppCompatActivity {

    private ProgressDialog progressDialog;

    private TextView tvHeading;
    private EditText etSelectedDate;
    private Button btnSelectDate;
    private RecyclerView recyclerView;

    String zoneName,wardName;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dayendreport);

        Toolbar toolbar2 = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar2);

//        getSupportActionBar().setTitle((CharSequence) "Day wise reports");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(getResources().getDrawable(R.drawable.back_arrow));

        tvHeading = findViewById(R.id.tvHeading);
        etSelectedDate = findViewById(R.id.etSelectedDate);
        btnSelectDate = findViewById(R.id.btnSelectDate);


        TextView textView3 = findViewById(R.id.textView3);
        // Get the Intent that started this activity
        Intent intent = getIntent();

        // Retrieve the zoneName and wardName from the intent
        zoneName = intent.getStringExtra("zoneName");
        wardName = intent.getStringExtra("wardName");

        // Check if the values are not null, then set the text to the TextView
        if (zoneName != null && wardName != null) {
            textView3.setText("Searching for Zone: " + zoneName + ", Ward: " + wardName);
        } else {
            textView3.setText("Zone and Ward details not available");
        }



        btnSelectDate.setOnClickListener(v -> showDatePickerDialog());

        assignAssetJsonData();

    }

    public void hideKeyboard() {
        InputMethodManager imm;
        View view = getCurrentFocus();
        if (view != null && (imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE)) != null) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private void fetchDataFromAPI(final String zoneId, final String wardId, final String selectedDate,final String username) {
        // Making the network call on a new thread
        new Thread(new Runnable() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        hideKeyboard();
                        progressDialog = new ProgressDialog(DayReport.this);
                        progressDialog.setMessage("Searching citizens...");
                        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                        progressDialog.setCancelable(false);
                        progressDialog.show();
                    }
                });
                try {
                    // Build the URL with query parameters, including the selected date
                    String apiUrl = "http://45.114.246.201:6005/api/PaymentsDetails/GetDayEndSummary?ZoneId=" + zoneId +
                            "&WardNo=" + wardId +"&RepDate=" + selectedDate +"&CollectedBy="+username;
                    Log.d("Citizen", "Citizen response " + apiUrl);

                    // Make a GET request to the API
                    URL url = new URL(apiUrl);
                    HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();

                    httpURLConnection.setRequestProperty("Accept", "*/*");
                    httpURLConnection.setRequestMethod("GET");
                    httpURLConnection.setDoOutput(false);

                    int responseCode = httpURLConnection.getResponseCode();

                    Log.d("Citizen", "Citizen response " + responseCode);

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
                                try {
                                    Log.d("API", "Response data " + response.toString());
//                                    TextView tvNoDataFound = findViewById(R.id.tv_no_data_found);

                                    String cleaned = response.toString().trim();
                                    JSONArray jsonArray = new JSONArray(cleaned.replace("\\\"", "\""));
                                    if (jsonArray.toString().equals("[]")) {
                                        Toast.makeText(getApplicationContext(),"No data found",Toast.LENGTH_SHORT).show();
                                    } else {
//                                        tvNoDataFound.setVisibility(View.GONE);
//                                        hide_linear_layout_habitations_rc.setVisibility(View.VISIBLE);

                                        // Parse the JSON response into DayEndCollection model
                                        Gson gson = new Gson();
                                        Type listType = new TypeToken<List<DayEndCollection>>(){}.getType();
                                        List<DayEndCollection> apiDataList = gson.fromJson(response.toString(), listType);

                                        Log.d("API", "Response data " + apiDataList.toString());

                                        for (DayEndCollection dec : apiDataList) {
                                            System.out.println(dec.getdate() + " " + dec.getAmount_paid() + " " + dec.getCollectedBy());
                                        }

                                        // Now update the RecyclerView with the API data
                                        updateRecyclerView(apiDataList);
                                    }
                                } catch (Exception e) {
                                    Toast.makeText(DayReport.this, "Error parsing data", Toast.LENGTH_SHORT).show();
                                    Log.d("Error","Exception occured "+e);
                                    Log.d("Error","Exception occured "+e.getMessage());

                                } finally {
                                    if (progressDialog.isShowing())
                                        progressDialog.dismiss();
                                }
                            }
                        });
                    } else {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (progressDialog.isShowing())
                                    progressDialog.dismiss();
                                Toast.makeText(DayReport.this, "Failed to fetch data", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void updateRecyclerView(List<DayEndCollection> apiDataList) {
        if (courseModelArrayList != null) {
            courseModelArrayList.clear();
        }
        courseModelArrayList.addAll(apiDataList);
        Log.d("Adapter", "Data size after update: " + courseModelArrayList.size());
        adapter.notifyDataSetChanged();
    }


    private SearchView searchViewFrag;
    private EditText searchFragEditText;
    private LinearLayout hide_linear_layout_habitations_rc;

    private RecyclerView habitationRecyclerview;
    private List<DayEndCollection> pourerDetailsList = new ArrayList<DayEndCollection>();
    private ArrayList<DayEndCollection> courseModelArrayList= new ArrayList<DayEndCollection>();;
    private DayReport.CourseAdapter adapter= new DayReport.CourseAdapter(courseModelArrayList,DayReport.this);
    private ArrayList<DayEndCollection> countryList;
    private Gson gson;

    private void assignAssetJsonData() {

//        hide_linear_layout_habitations_rc = findViewById(R.id.hide_lin_lay_search_result_rv);
//        hide_linear_layout_habitations_rc.setVisibility(View.VISIBLE);

        /*searchViewFrag = findViewById(R.id.reg_id_house_no_search_view);
        //searchViewFrag.setVisibility(View.GONE);
        searchFragEditText = searchViewFrag.findViewById(androidx.appcompat.R.id.search_src_text);
        searchFragEditText.setTextSize(14.0f);
        //searchFragEditText.setTextColor(getResources().getColor(R.color.white));
        //searchFragEditText.setHintTextColor(getResources().getColor(R.color.white));

        searchViewFrag.setIconifiedByDefault(false);
        // listening to search query text change
        searchViewFrag.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // filter recycler view when query submitted
                // milkPourerAdapter.getFilter().filter(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String query) {
                // filter recycler view when text is changed
                // milkPourerAdapter.getFilter().filter(query);

                if (!query.isEmpty()) {
                    filter(query);
                    hide_linear_layout_habitations_rc.setVisibility(View.VISIBLE);
                } else {
                    hide_linear_layout_habitations_rc.setVisibility(View.GONE);
                }


                return false;
            }
        });*/
        countryList = new ArrayList<>();


        gson = new Gson();
        try {
            initializeRC();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initializeRC() {
        habitationRecyclerview = findViewById(R.id.search_result_collection_amount_recyclerview);
        habitationRecyclerview.setVisibility(View.VISIBLE);
        buildRecyclerView();
    }
    private void buildRecyclerView() {

        // below line we are creating a new array list
        courseModelArrayList = new ArrayList<DayEndCollection>();

        Log.d("AmountCollection", "courseModelArrayList.size(): " + courseModelArrayList.size());

        // initializing our adapter class.
        adapter = new DayReport.CourseAdapter(courseModelArrayList,DayReport.this);

        // adding layout manager to our recycler view.
        LinearLayoutManager manager = new LinearLayoutManager(DayReport.this);
        habitationRecyclerview.setHasFixedSize(true);

        // setting layout manager
        // to our recycler view.
        habitationRecyclerview.setLayoutManager(manager);

        // setting adapter to
        // our recycler view.
        habitationRecyclerview.setAdapter(adapter);

        adapter.notifyDataSetChanged();
    }

    public class CourseAdapter extends RecyclerView.Adapter<DayReport.CourseAdapter.ViewHolder> {

        // creating a variable for array list and context.
        private ArrayList<DayEndCollection> MilkPourerDetailsArrayList;

        // creating a constructor for our variables.
        public CourseAdapter(ArrayList<DayEndCollection> MilkPourerDetailsArrayList, Context context) {
            this.MilkPourerDetailsArrayList = MilkPourerDetailsArrayList;
        }

        // method for filtering our recyclerview items.
        public void filterList(ArrayList<DayEndCollection> filterlist) {
            // below line is to add our filtered
            // list in our course array list.
            MilkPourerDetailsArrayList = filterlist;
            // below line is to notify our adapter
            // as change in recycler view data.
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public DayReport.CourseAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            // below line is to inflate our layout.
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.day_report, parent, false);
            return new DayReport.CourseAdapter.ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull DayReport.CourseAdapter.ViewHolder holder, int position) {
            // Getting the current item
            DayEndCollection milkPourerDetails = MilkPourerDetailsArrayList.get(position);

            // Setting data to your views
            holder.title_property_house_no_txt.setText(milkPourerDetails.getHouseNo() != null ? milkPourerDetails.getHouseNo() : "");
            holder.title_zone_ward_txt.setText((zoneName != null ? zoneName : "") + " and " + (wardName != null ? wardName : ""));
            holder.title_owner_name_txt.setText(milkPourerDetails.getOwnerName() != null ? milkPourerDetails.getOwnerName() : "");
            holder.title_owner_contact_no_txt.setText(milkPourerDetails.getOwnerContactNo() != null ? milkPourerDetails.getOwnerContactNo() : "");
            holder.title_amount_paid_txt.setText(milkPourerDetails.getAmount_paid() + "");
            holder.title_months_paid_txt.setText(milkPourerDetails.getMonths_covered() + "");
            holder.title_payment_mode_txt.setText(milkPourerDetails.getPayment_details() != null ? milkPourerDetails.getPayment_details() + "" : "");
            holder.title_reference_id_txt.setText(milkPourerDetails.getPayment_referenceNo() != null ? milkPourerDetails.getPayment_referenceNo() + "" : "");
            holder.title_transaction_id_txt.setText(milkPourerDetails.getChq_dd_ne_rt_oth_no() != null ? milkPourerDetails.getChq_dd_ne_rt_oth_no() + "" : "");


//            holder.establishment_txt.setText(milkPourerDetails.getEstablishmentType());



        }


        @Override
        public int getItemCount() {
            // returning the size of array list.
            return MilkPourerDetailsArrayList.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            // creating variables for our views.
            public TextView title_property_house_no_txt, title_zone_ward_txt, title_owner_name_txt, title_owner_contact_no_txt,
                    title_amount_paid_txt,title_months_paid_txt,title_payment_mode_txt,title_reference_id_txt,title_transaction_id_txt;
            public TextView establishment_txt, category_sub_txt;
            private ItemClickListener itemClickListener;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                // initializing our views with their ids.
                this.title_property_house_no_txt = itemView.findViewById(R.id.property_house_no_txt);
                this.title_zone_ward_txt = itemView.findViewById(R.id.zone_ward_txt);
                this.title_owner_name_txt = itemView.findViewById(R.id.owner_name_txt);
                this.title_owner_contact_no_txt = itemView.findViewById(R.id.owner_contact_no_txt);
                this.title_amount_paid_txt=itemView.findViewById(R.id.amount_paid_txt);
                this.title_months_paid_txt=itemView.findViewById(R.id.months_paid_txt);
                this.title_payment_mode_txt=itemView.findViewById(R.id.payment_mode_txt);
                this.title_reference_id_txt=itemView.findViewById(R.id.reference_id_txt);
                this.title_transaction_id_txt=itemView.findViewById(R.id.transaction_id_txt);
//                this.establishment_txt = itemView.findViewById(R.id.establishment_txt);
//                this.establishment_txt = itemView.findViewById(R.id.establishment_txt);

                itemView.setOnClickListener(this);
            }

            @Override
            public void onClick(View v) {

//                itemClickListener.onClick(v, getAdapterPosition(), false);

            }

            public void setItemClickListener(ItemClickListener itemClickListener) {
                this.itemClickListener = itemClickListener;
            }
        }
    }

    private void filter(String text) {
        ArrayList<DayEndCollection> filteredlist = new ArrayList<>();
        for (DayEndCollection item : courseModelArrayList) {
            if (item.getHouseNo().toLowerCase().contains(text.toLowerCase()) ||
                    item.getOwnerName().toLowerCase().contains(text.toLowerCase()) ||
                    item.getOwnerContactNo().contains(text)) {
                filteredlist.add(item);
            }
        }
        if (filteredlist.isEmpty()) {
            Toast.makeText(DayReport.this, "No Data Found..", Toast.LENGTH_SHORT).show();
        } else {
            adapter.filterList(filteredlist);
        }
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


    @Override
    public void onBackPressed() {
        super.onBackPressed();
//        startActivity(new Intent(getApplicationContext(), DashboardActivity.class));
        finish();
    }

    private void showDatePickerDialog() {
        final Calendar calendar = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, year, month, dayOfMonth) -> {
                    String selectedDate = String.format(Locale.US, "%d-%02d-%02d", year, month + 1, dayOfMonth);
                    etSelectedDate.setText(selectedDate);
                    Intent intent = getIntent();
                    String zoneId = intent.getStringExtra("zoneId");
                    if(zoneId==null || zoneId.isEmpty()){
                        zoneId = "1";
                        System.out.println("zoneid is null"+zoneId);
                    }

                    SharedPreferences prefs = Utility.getSharedPreferences(DayReport.this);
                    String userName = prefs.getString("UserName", "");

                    String wardId = intent.getStringExtra("wardId");
                    if(wardId==null || wardId.isEmpty()) {
//--+                        wardId = "17";
                        System.out.println("wardId is null" + wardId);
                    }
                    fetchDataFromAPI(zoneId, wardId,selectedDate, userName);
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.show();
    }
}
