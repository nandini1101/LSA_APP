package com.org.lsa;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
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

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.org.lsa.Interface.ItemClickListener;
import com.org.lsa.model.SurveyDataModel;

import org.json.JSONArray;

import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class AmountCollection extends AppCompatActivity {
    private ProgressDialog progressDialog;

    private EditText etName, etPhoneNumber, etHouseNumber;
    private Button btnSearch;

    String zoneName,wardName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_amount_collection);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        Toolbar toolbar2 = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar2);

        getSupportActionBar().setTitle((CharSequence) "Amount Collection");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(getResources().getDrawable(R.drawable.back_arrow));

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

        etName = findViewById(R.id.etName);
        etPhoneNumber = findViewById(R.id.etPhoneNumber);
        etHouseNumber = findViewById(R.id.etHouseNumber);
        btnSearch = findViewById(R.id.btnSearch);

        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = etName.getText().toString();
                String phoneNumber = etPhoneNumber.getText().toString();
                String houseNumber = etHouseNumber.getText().toString();

                if (name.isEmpty() && phoneNumber.isEmpty() && houseNumber.isEmpty()) {
                    Toast.makeText(AmountCollection.this, "Please enter at least one field", Toast.LENGTH_SHORT).show();
                }
                else if(!name.isEmpty() && name.length()<3){
                    Toast.makeText(AmountCollection.this, "Please enter atleast 3 letters in name", Toast.LENGTH_SHORT).show();
                }
                else if(!phoneNumber.isEmpty() && phoneNumber.length()<3){
                    Toast.makeText(AmountCollection.this, "Please enter atleast 3 letters in Phone number", Toast.LENGTH_SHORT).show();
                }
                else if(!houseNumber.isEmpty() && houseNumber.length()<3){
                    Toast.makeText(AmountCollection.this, "Please enter atleast 3 letters in House number", Toast.LENGTH_SHORT).show();
                }else {
                    // Call the method to search
                    Intent intent = getIntent();
                    String zoneId = intent.getStringExtra("zoneId");
                    if(zoneId==null || zoneId.isEmpty()){
                        zoneId = "1";
                        System.out.println("zoneid is null"+zoneId);
                    }

                    String wardId = intent.getStringExtra("wardId");
                    if(wardId==null || wardId.isEmpty()){
                        wardId = "17";
                        System.out.println("wardId is null"+wardId);
                    }

                    searchAPI(name, phoneNumber, houseNumber, zoneId, wardId);
                }
            }
        });



        progressDialog = new ProgressDialog(AmountCollection.this);
        progressDialog.setMessage("Loading...");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setCancelable(false);
        progressDialog.show();
        // LoginValidation();
        new CountDownTimer(500, 500) {
            @Override
            public void onTick(long l) {

            }

            @Override
            public void onFinish() {

                assignAssetJsonData();

                if (progressDialog.isShowing())
                    progressDialog.dismiss();
            }
        }.start();


    }

    public void hideKeyboard() {
        InputMethodManager imm;
        View view = getCurrentFocus();
        if (view != null && (imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE)) != null) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private void searchAPI(final String name, final String phoneNumber, final String houseNumber, final String zoneId, final String wardId) {
        // Making the network call on a new thread
        new Thread(new Runnable() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        hideKeyboard();
                        progressDialog = new ProgressDialog(AmountCollection.this);
                        progressDialog.setMessage("Searching citizens...");
                        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                        progressDialog.setCancelable(false);
                        progressDialog.show();
                    }
                });
                try {
                    // Build the URL with query parameters
                    // http://45.114.246.137:85/api/PaymentsDetails/GetCitizenForPayment?ZoneId=1&houseNo=%20&name=a&mobileNo=9
                    String apiUrl = "http://45.114.246.201:6005/api/PaymentsDetails/GetCitizenForPayment?ZoneId="+ zoneId +"&WardNo="+wardId+"&houseNo="+houseNumber+"&name="+name+"&mobileNo="+phoneNumber;
//                    Log.d("Citizen","Citizen response "+apiUrl);
                    // Make a GET request to the API
                    URL url = new URL(apiUrl);
                    HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();

                    httpURLConnection.setRequestProperty("Accept", "*/*");
                    httpURLConnection.setRequestMethod("GET");
                    httpURLConnection.setDoOutput(false);

                    int responseCode = httpURLConnection.getResponseCode();

//                    Log.d("Citizen","Citizen response "+responseCode);

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

//                                    Log.d("API","Response data "+response.toString());
                                    TextView tvNoDataFound = findViewById(R.id.tv_no_data_found);

                                    String cleaned = response.toString().trim();
                                    JSONArray jsonArray = new JSONArray(cleaned.replace("\\\"", "\""));
                                    if (jsonArray.toString().equals("[]")) {
                                        hide_linear_layout_habitations_rc.setVisibility(View.GONE);
                                        tvNoDataFound.setVisibility(View.VISIBLE);
                                        return;
                                    }
                                    else{
                                        tvNoDataFound.setVisibility(View.GONE);
                                        hide_linear_layout_habitations_rc.setVisibility(View.VISIBLE);
                                        // Parse the JSON response
                                        Gson gson = new Gson();
                                        Type listType = new TypeToken<List<SurveyDataModel>>(){}.getType();
                                        List<SurveyDataModel> apiDataList = gson.fromJson(response.toString(), listType);

//                                        Log.d("API","Response data "+apiDataList.toString());

                                        for(SurveyDataModel sd:apiDataList){
                                            System.out.println(sd.getOwnerName()+" "+sd.getSDId()+" "+sd.getZoneId()+" "+sd.getDefault_months()+" "+sd.getTotal_Outstadnding_amount()+" "+sd.getEstablishmentType());
                                        }

                                        // Now update the RecyclerView with the API data
                                        updateRecyclerView(apiDataList);
                                    }
                                } catch (Exception e) {
                                    Toast.makeText(AmountCollection.this, "Error parsing data", Toast.LENGTH_SHORT).show();
                                }
                                finally{
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
                                Toast.makeText(AmountCollection.this, "Failed to fetch wards", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void updateRecyclerView(List<SurveyDataModel> apiDataList) {
        // Clear the existing data
        courseModelArrayList.clear();
        // Add the new data
        courseModelArrayList.addAll(apiDataList);
        // Notify the adapter about the data change
        adapter.notifyDataSetChanged();
    }


    private SearchView searchViewFrag;
    private EditText searchFragEditText;
    private LinearLayout hide_linear_layout_habitations_rc;

    private RecyclerView habitationRecyclerview;
    private List<SurveyDataModel> pourerDetailsList = new ArrayList<SurveyDataModel>();
    private CourseAdapter adapter;
    private ArrayList<SurveyDataModel> countryList;
    private Gson gson;


    private void assignAssetJsonData() {

        hide_linear_layout_habitations_rc = findViewById(R.id.hide_lin_lay_search_result_rv);
        hide_linear_layout_habitations_rc.setVisibility(View.VISIBLE);

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
//        countryList = (ArrayList<SurveyDataModel>) db.getAllSurveyData();

        gson = new Gson();
        try {
            initializeRC();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initializeRC() {
        habitationRecyclerview = findViewById(R.id.search_result_collection_amount_recyclerview);
        buildRecyclerView();
    }

    private ArrayList<SurveyDataModel> courseModelArrayList;

    private void buildRecyclerView() {

        // below line we are creating a new array list
        courseModelArrayList = new ArrayList<SurveyDataModel>();

//        Log.d("AmountCollection", "courseModelArrayList.size(): " + courseModelArrayList.size());

        // initializing our adapter class.
        adapter = new CourseAdapter(courseModelArrayList, AmountCollection.this);

        // adding layout manager to our recycler view.
        LinearLayoutManager manager = new LinearLayoutManager(AmountCollection.this);
        habitationRecyclerview.setHasFixedSize(true);

        // setting layout manager
        // to our recycler view.
        habitationRecyclerview.setLayoutManager(manager);

        // setting adapter to
        // our recycler view.
        habitationRecyclerview.setAdapter(adapter);

        adapter.notifyDataSetChanged();
    }

    public class CourseAdapter extends RecyclerView.Adapter<CourseAdapter.ViewHolder> {

        // creating a variable for array list and context.
        private ArrayList<SurveyDataModel> MilkPourerDetailsArrayList;

        // creating a constructor for our variables.
        public CourseAdapter(ArrayList<SurveyDataModel> MilkPourerDetailsArrayList, Context context) {
            this.MilkPourerDetailsArrayList = MilkPourerDetailsArrayList;
        }

        // method for filtering our recyclerview items.
        public void filterList(ArrayList<SurveyDataModel> filterlist) {
            // below line is to add our filtered
            // list in our course array list.
            MilkPourerDetailsArrayList = filterlist;
            // below line is to notify our adapter
            // as change in recycler view data.
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public CourseAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            // below line is to inflate our layout.
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.amount_collection_items_recyclerview, parent, false);
            return new CourseAdapter.ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull CourseAdapter.ViewHolder holder, int position) {
            // Getting the current item
            SurveyDataModel milkPourerDetails = MilkPourerDetailsArrayList.get(position);

            // Setting data to your views
            holder.property_house_no_txt.setText(milkPourerDetails.getHouseNo());
            holder.zone_ward_txt.setText(zoneName+" and "+wardName);
            holder.owner_name_txt.setText(milkPourerDetails.getOwnerName());
            holder.owner_contact_no_txt.setText(milkPourerDetails.getOwnerContactNo());
//            holder.establishment_txt.setText(milkPourerDetails.getEstablishmentType());

            // Set the item click listener
            holder.setItemClickListener(new ItemClickListener() {
                @Override
                public void onClick(View view, int position, boolean isLongClick) {
                    Intent intent = new Intent(view.getContext(), AmountCollectionEntry.class); // Replace with your next activity class

                    // Put all the necessary data into the intent
                    intent.putExtra("SD_id", milkPourerDetails.getSDId());
                    intent.putExtra("house_no", milkPourerDetails.getHouseNo());
                    intent.putExtra("zoneId", milkPourerDetails.getZoneId());
                    intent.putExtra("zoneName", zoneName);
                    intent.putExtra("wardNo", milkPourerDetails.getWardno());
                    intent.putExtra("establishmentType", milkPourerDetails.getEstablishmentType());
                    intent.putExtra("wardName", wardName);
                    intent.putExtra("owner_name", milkPourerDetails.getOwnerName());
                    intent.putExtra("owner_contact", milkPourerDetails.getOwnerContactNo());
                    intent.putExtra("total_outstanding_amount", milkPourerDetails.getTotal_Outstadnding_amount());
                    intent.putExtra("address", milkPourerDetails.getAddress());
                    intent.putExtra("defaultMonths",milkPourerDetails.getDefault_months());

                    // Start the new activity
                    view.getContext().startActivity(intent);
                }
            });
        }


        @Override
        public int getItemCount() {
            // returning the size of array list.
            return MilkPourerDetailsArrayList.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            // creating variables for our views.
            public TextView property_house_no_txt, zone_ward_txt, owner_name_txt, namePourerTxt, uidPourerTxt;
            public TextView owner_contact_no_txt, establishment_txt, category_sub_txt;
            private ItemClickListener itemClickListener;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                // initializing our views with their ids.
                this.property_house_no_txt = itemView.findViewById(R.id.property_house_no_txt);
                this.zone_ward_txt = itemView.findViewById(R.id.zone_ward_txt);
                this.owner_name_txt = itemView.findViewById(R.id.owner_name_txt);
                this.owner_contact_no_txt = itemView.findViewById(R.id.owner_contact_no_txt);
//                this.establishment_txt = itemView.findViewById(R.id.establishment_txt);
//                this.establishment_txt = itemView.findViewById(R.id.establishment_txt);

                itemView.setOnClickListener(this);
            }

            @Override
            public void onClick(View v) {

                itemClickListener.onClick(v, getAdapterPosition(), false);

            }

            public void setItemClickListener(ItemClickListener itemClickListener) {
                this.itemClickListener = itemClickListener;
            }
        }
    }

    private void filter(String text) {
        ArrayList<SurveyDataModel> filteredlist = new ArrayList<>();
        for (SurveyDataModel item : courseModelArrayList) {
            if (item.getHouseNo().toLowerCase().contains(text.toLowerCase()) ||
                    item.getOwnerName().toLowerCase().contains(text.toLowerCase()) ||
                    item.getOwnerContactNo().contains(text)) {
                filteredlist.add(item);
            }
        }
        if (filteredlist.isEmpty()) {
            Toast.makeText(AmountCollection.this, "No Data Found..", Toast.LENGTH_SHORT).show();
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
}