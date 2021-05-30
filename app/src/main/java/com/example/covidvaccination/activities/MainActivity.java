package com.example.covidvaccination.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.covidvaccination.Adapters.CentreDetailsAdapter;
import com.example.covidvaccination.Adapters.DatePickerFragment;
import com.example.covidvaccination.R;
import com.example.covidvaccination.models.CentreDetails;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import static android.view.View.VISIBLE;

public class MainActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener {

    private EditText mPincode;
    private Button mNext;
    private RecyclerView mRecyclerview;
    //private CentreDetailsAdapter mAdapter;
    public String pincode, mDate;
    private ArrayList<CentreDetails> centres;
    private ProgressBar mProgressBar;

    private String api_url = "https://cdn-api.co-vin.in/api/v2/appointment/sessions/public/findByPin?";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mProgressBar = findViewById(R.id.progress_circular);
        mPincode = findViewById(R.id.inputPin);
        mNext = findViewById(R.id.submitBtn);
        mRecyclerview = findViewById(R.id.recyclerView);
        mRecyclerview.setHasFixedSize(true);


        centres = new ArrayList<CentreDetails>();

        mNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mProgressBar.setVisibility(VISIBLE);
                DialogFragment datePicker = new DatePickerFragment();
                datePicker.show(getSupportFragmentManager(), "date picker");
            }
        });

    }

    private void sendRequest() {
        //mRecyclerview.invalidate();
        centres.clear();
        pincode = mPincode.getText().toString();
        String request_url = api_url + "pincode=" + pincode + "&date=" + mDate;
        StringRequest stringRequest = new StringRequest(Request.Method.GET, request_url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject obj = new JSONObject(response);
                    JSONArray sessionsArray = obj.getJSONArray("sessions");
                    for(int i=0; i< sessionsArray.length(); i++){
                        JSONObject sessionObject = sessionsArray.getJSONObject(i);
                        CentreDetails cd = new CentreDetails();
                        cd.setCenterName(sessionObject.getString("name"));
                        cd.setCenterAddress(sessionObject.getString("address"));
                        cd.setCenterFromTime(sessionObject.getString("from"));
                        cd.setCenterToTime(sessionObject.getString("to"));
                        cd.setVaccineName(sessionObject.getString("vaccine"));
                        cd.setFee_type(sessionObject.getString("fee_type"));
                        cd.setAgeLimit(String.valueOf(sessionObject.getInt("min_age_limit")));
                        cd.setAvaiableCapacity(sessionObject.getString("available_capacity"));
                        centres.add(cd);
                    }

                    CentreDetailsAdapter centreDetailsAdapter = new CentreDetailsAdapter(getApplicationContext(), centres);
                    mRecyclerview.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
                    mRecyclerview.setAdapter(centreDetailsAdapter);
                    mProgressBar.setVisibility(View.INVISIBLE);
                } catch (JSONException e) {
                    mProgressBar.setVisibility(View.INVISIBLE);
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                mProgressBar.setVisibility(View.INVISIBLE);
                Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }
    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        Calendar z = Calendar.getInstance();
        z.set(Calendar.YEAR, year);
        z.set(Calendar.MONTH, month);
        z.set(Calendar.DAY_OF_MONTH, dayOfMonth);

        SimpleDateFormat dateformat = new SimpleDateFormat("dd-MM-YYYY");
        dateformat.setTimeZone(z.getTimeZone());
        String date  = dateformat.format(z.getTime());
        utility(date);
    }
    private void utility(String date) {
        mDate = date;
        sendRequest();
    }
}