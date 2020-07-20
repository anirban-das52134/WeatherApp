package com.example.weatherapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.weatherapp.Adapters.ForcastAdapter;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class Dashboard extends AppCompatActivity {
    //API to fetch the data
    String URL = "https://api.openweathermap.org/data/2.5/onecall?lat=22.5726&lon=88.3639&units=metric&appid=44a3d2d0c1be6e56b777b391fb36faab";

    //List to store the data fetched from API
    List<String> maxTempList, minTempList, dateList,mainList,descList;
    //Database Keys
    public static  final String KEY_DATE = "date";
    public static  final String KEY_MAX = "max";
    public static  final String KEY_MIN = "min";
    public static final String KEY_MAIN="main";
    public static final String KEY_DESC="desc";

    //Database Instance to have the instance of the database for this particular user stored fo furthur use
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private DocumentReference docRef;

    //Components to show current day's data
    TextView locationTodayTV,mainTodayTV,descTodayTV,maxTodayTV,minTodayTV,dateToday;

    //Recycler View for 7-day forcast
    RecyclerView forcastRV;
    ForcastAdapter forcastAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        //Email passed from login activity to be used as unique document name for collection WeatherData in firebase
        String email = getIntent().getStringExtra("email");
        docRef = db.collection("WeatherData").document(email);

        //Initialize the lists and load the data
        initList();
        loadJSON(URL,true); // True means load data into database else we will simply show the data

        forcastRV = findViewById(R.id.forcastRecyclerView);

        locationTodayTV = findViewById(R.id.locationToday);
        dateToday = findViewById(R.id.dateTodayText);
        mainTodayTV = findViewById(R.id.mainTodayText);
        descTodayTV = findViewById(R.id.descTodayText);
        maxTodayTV = findViewById(R.id.maxTempToday);
        minTodayTV = findViewById(R.id.minTempToday);
    }

    private void initList(){
        maxTempList = new ArrayList<>();
        minTempList = new ArrayList<>();
        dateList = new ArrayList<>();
        mainList = new ArrayList<>();
        descList = new ArrayList<>();
    }
    public void loadJSON(String url, final boolean loadIntoDatabase){
        Log.i("Content","Loading Data from API");
        StringRequest request = new StringRequest(url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.i("Content",response);
                loadData(response,loadIntoDatabase);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
                Log.i("Content","Error1");
            }
        });
        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(request);
    }

    private void loadData(String response,boolean loadIntoDatabase){
        try {
            JSONObject obj = new JSONObject(response);
            JSONArray dailyData = obj.getJSONArray("daily");

            for(int  i = 0;i<dailyData.length();i++){
                JSONObject dailyObj = dailyData.getJSONObject(i);
                JSONObject tempObj = dailyObj.getJSONObject("temp");
                JSONArray weatherArray = dailyObj.getJSONArray("weather");
                JSONObject weatherObj = weatherArray.getJSONObject(0);

                String unixDate = dailyObj.getString("dt");
                long epoch = Long.parseLong( unixDate );
                Date date = new Date( epoch * 1000 );
                SimpleDateFormat formatter = new SimpleDateFormat("EEE, dd MMM yyyy");
                String strDate= formatter.format(date);

                String max = tempObj.getString("max");
                String min = tempObj.getString("min");

                String main = weatherObj.getString("main");
                String description = weatherObj.getString("description");

                dateList.add(strDate);
                maxTempList.add(max);
                minTempList.add(min);
                mainList.add(main);
                descList.add(description);
            }

            if(loadIntoDatabase) queryDB();
            else fetchFromDB();

        } catch (JSONException e) {
            e.printStackTrace();
            Log.i("Content","Error2");
        }
    }

    public void queryDB(){
        Map<String,Object> map = new HashMap<>();

        map.put(KEY_DATE,dateList);
        map.put(KEY_MAX,maxTempList);
        map.put(KEY_MIN,minTempList);
        map.put(KEY_MAIN,mainList);
        map.put(KEY_DESC,descList);

        docRef.set(map)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(getApplicationContext(),"Data Stored",Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getApplicationContext(),"Error",Toast.LENGTH_SHORT).show();
                    }
                });

        fetchFromDB();
    }

    public void fetchFromDB(){
        docRef.get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if(documentSnapshot.exists()){
                            //Fetch the data from firebase into application
                           Object value = documentSnapshot.get(KEY_DATE);
                            if(value != null)  {
                                dateList = (List<String>) convertObjectToList(value);
                            }
                            value = documentSnapshot.get(KEY_MAX);
                            if(value != null)  {
                                maxTempList = (List<String>) convertObjectToList(value);
                            }
                            value = documentSnapshot.get(KEY_MIN);
                            if(value != null)  {
                                minTempList = (List<String>) convertObjectToList(value);
                            }
                            value = documentSnapshot.get(KEY_MAIN);
                            if(value != null)  {
                                mainList = (List<String>) convertObjectToList(value);
                            }
                            value = documentSnapshot.get(KEY_DESC);
                            if(value != null)  {
                                descList = (List<String>) convertObjectToList(value);
                            }
                            // Data Fetched, Show the data
                            showData();
                        }
                        else {
                            Toast.makeText(getApplicationContext(),"No data found!",Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getApplicationContext(),"Error",Toast.LENGTH_SHORT).show();
                    }
                });
    }

    //Method to assign data (Will work for the search functionality also)
    @SuppressLint("SetTextI18n")
    private void showData(){
        Log.i("Content","Showing Data");

        //Set Today's data
        dateToday.setText(dateList.get(0));
        mainTodayTV.setText(mainList.get(0));
        descTodayTV.setText(descList.get(0).toUpperCase());
        maxTodayTV.setText("Max Temp: "+maxTempList.get(0)+  " \u2103" );
        minTodayTV.setText("Min Temp: "+minTempList.get(0)+  " \u2103" );

        //Remove the first element since today's data has already been assigned
        dateList.remove(0);
        maxTempList.remove(0);
        minTempList.remove(0);
        mainList.remove(0);
        descList.remove(0);

        forcastAdapter = new ForcastAdapter(dateList,maxTempList,minTempList,mainList,descList);
        forcastRV.setLayoutManager(new LinearLayoutManager(this));
        forcastRV.setAdapter(forcastAdapter);
    }

    //Logout Method
    public void Logout(View v){
        FirebaseAuth.getInstance().signOut();
        startActivity(new Intent(getApplicationContext(),MainActivity.class));
        finish();
    }


    //Helper Method to convert object from firebase intro an List
    public static List<?> convertObjectToList(Object obj) {
        List<?> list = new ArrayList<>();
        if (obj.getClass().isArray()) {
            list = Arrays.asList((Object[])obj);
        } else if (obj instanceof Collection) {
            list = new ArrayList<>((Collection<?>)obj);
        }
        return list;
    }
}