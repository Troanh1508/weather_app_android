package com.example.weatherapp;

import static com.example.weatherapp.WeatherType.fromWMO;

import android.annotation.SuppressLint;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity implements ItemClickListener {

    private FusedLocationProviderClient fusedLocationClient;
    private static final int REQUEST_CODE = 100;

    private RecyclerView.Adapter adapterHourly;
    private RecyclerView recyclerView;
    private ArrayList<HourlyItem> items;

    private TextView weatherInfo;
    private TextView weatherDescription;
    private TextView currentDateTime;
    private TextView currentTemp;
    private TextView maxminTemp;
    private TextView windSpeed;
    private TextView humidity;
    private TextView rainChance;
    private TextView uvIndex;
    private EditText cityText;
    ImageView currentWeatherIcon;

    public final OkHttpClient client = new OkHttpClient();
    String city;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });



        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Check for location permissions
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE);
        }
        else {
            getLastKnownLocation();
        }


        weatherInfo = findViewById(R.id.weatherInfoText);
        weatherDescription = findViewById(R.id.weatherDescription);
        currentDateTime = findViewById(R.id.currentDateTime);
        currentTemp = findViewById(R.id.currentTemp);
        maxminTemp = findViewById(R.id.maxminTemp);
        cityText = findViewById(R.id.cityText);
        windSpeed = findViewById(R.id.windSpeed);
        humidity = findViewById(R.id.humidity);
        rainChance = findViewById(R.id.rainChance);
        uvIndex = findViewById(R.id.uvIndexCurrent);

        currentWeatherIcon = findViewById(R.id.currentWeatherIcon);
        items = new ArrayList<>();


        Button getWeatherButton = findViewById(R.id.getWeatherButton);
        getWeatherButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                city = cityText.getText().toString().trim();
                if (!city.isEmpty()) {
                    getLocationData(city);
                } else {
                    Toast.makeText(MainActivity.this, R.string.please_enter_city_name, Toast.LENGTH_SHORT).show();
                }
            }
        });

        TextView nextButton = findViewById(R.id.Next7Days);
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                city = cityText.getText().toString().trim();
                if (!city.isEmpty()) {
                    Intent intent = new Intent(MainActivity.this, ForecastActivity.class);
                    intent.putExtra("city_name", city);
                    startActivity(intent);
                } else {
                    Toast.makeText(MainActivity.this, R.string.please_enter_city_name, Toast.LENGTH_SHORT).show();
                }
            }
        });

    }



    private void initRecyclerView(ArrayList<HourlyItem> items) {

        recyclerView = findViewById(R.id.RecyclerHourly);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        adapterHourly = new HourlyAdapter(items, this);
        recyclerView.setAdapter(adapterHourly);

    }

    private void getLastKnownLocation() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED)
            {
                // Permission is granted, proceed with the location-related task
                fusedLocationClient.getLastLocation()
                        .addOnSuccessListener(location -> {
                            if (location != null) {
                                double latitude = location.getLatitude();
                                double longitude = location.getLongitude();
                                getLocationData(getCityName(latitude, longitude));
                            }
                        });
            } else {
                // Permission is not granted, request it
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE);
            }
    }

    private String getCityName(double latitude, double longitude) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            if (addresses != null && !addresses.isEmpty()) {

                String cityName = addresses.get(0).getAdminArea();
                SharedPreferences sharedPreferences = getSharedPreferences("WeatherPrefs", MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("city_name", cityName);
                editor.apply();

                MainActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        cityText.setText(cityName);
                    }
                });
                Log.d("myTag", "City: " + cityName);
                return cityName;
            }
            else {
                Log.d("myTag", "No address found.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLastKnownLocation();
            } else {
                // Permission denied
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void getLocationData(String city) {
        city = city.replaceAll(" ","+");
        String url = "https://geocoding-api.open-meteo.com/v1/search?name=" +
                city + "&count=1&language=en&format=json";
        Request request = new Request.Builder()
                .url(url)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful() & response.body() != null) {
                    final String responseData = response.body().string();
                    try {
                        JSONObject json = new JSONObject(responseData);
                        if (json.has("results") && !json.isNull("results")) {
                            JSONArray results = json.getJSONArray("results");
                            JSONObject locationData = results.getJSONObject(0);
                            double latitude = locationData.getDouble("latitude");
                            double longitude = locationData.getDouble("longitude");
                            Log.i("myTag", locationData.getString("name") + ", " + locationData.getString("country"));
                            getWeatherData(latitude, longitude, new WeatherCallback() {
                                @SuppressLint("SetTextI18n")
                                @Override
                                public void onSuccess(WeatherResponse weatherResponse) {
                                    // Handle the successful retrieval of weather data
                                    Log.i("myTag", "Current time: " + weatherResponse.getCurrent().getTime().toString());
                                    Log.i("myTag", "Current temp: " + weatherResponse.getCurrent().getTemperature());
                                    runOnUiThread(() -> {
                                        int code = weatherResponse.getCurrent().getWeatherCode();
                                        weatherDescription.setText(fromWMO(code).getWeatherDesc());
                                        currentWeatherIcon.setImageResource(fromWMO(code).getIconRes());
                                        currentDateTime.setText(weatherResponse.getCurrent().getTime());
                                        maxminTemp.setText("Highest: " +
                                                FormatUtils.formatTemperature(weatherResponse.getDaily().getTemperature_2m_max().get(0))
                                                            + " Lowest: " +
                                                FormatUtils.formatTemperature(weatherResponse.getDaily().getTemperature_2m_min().get(0)));
                                        currentTemp.setText(FormatUtils.formatTemperature(weatherResponse.getCurrent().getTemperature()));
                                        humidity.setText(FormatUtils.formatHumidity(weatherResponse.getCurrent().getRelativeHumidity()));
                                        windSpeed.setText(FormatUtils.formatWindSpeed(weatherResponse.getCurrent().getWindSpeed()));
                                        int currentHour = LocalTime.now().getHour();
                                        rainChance.setText(FormatUtils.formatRainChance(weatherResponse.getHourly().getPrecipitationProbability().get(currentHour)));
                                        uvIndex.setText(FormatUtils.formatUvIndex(weatherResponse.getHourly().getUvIndex().get(currentHour)));

                                        // save data to sharedpref
                                        SharedPreferences sharedPreferences = getSharedPreferences("WeatherPrefs", MODE_PRIVATE);
                                        SharedPreferences.Editor editor = sharedPreferences.edit();
                                        editor.putString("temperature", FormatUtils.formatTemperature(weatherResponse.getCurrent().getTemperature()));
                                        editor.putString("weather_desc", fromWMO(code).getWeatherDesc());
                                        editor.putString("uvIndex", FormatUtils.formatUvIndex(weatherResponse.getHourly().getUvIndex().get(currentHour)));
                                        editor.putInt("widgetIconRes", fromWMO(code).getIconRes());
                                        editor.apply();

                                        items.clear();
                                        for (int i = currentHour; i < 24; i++ )
                                        {
                                            int weatherCode = weatherResponse.getHourly().getWeather_code().get(i);
                                            int resId = fromWMO(weatherCode).getIconRes();
                                            String temperature = FormatUtils.formatTemperature(weatherResponse.getHourly().getTemperature_2m().get(i));
                                            LocalTime time = LocalTime.of( i,0);
                                            String formattedHour = String.format(Locale.US, "%02d:00", i);


                                            items.add(new HourlyItem(formattedHour, temperature, resId));
                                        }
                                        initRecyclerView(items);
                                        Log.i("myTag", String.valueOf(LocalTime.now().getHour()));
                                    });
                                    // Access daily weather data
                                    if (weatherResponse.getDaily() != null) {
                                        List<String> dates = weatherResponse.getDaily().getTime();
                                        List<Double> uvIndexMax = weatherResponse.getDaily().getUvIndexMax();

                                        for (int i = 0; i < dates.size(); i++) {
                                            Log.i("myTag", "Date: " + dates.get(i) + ", Max UV Index: " + uvIndexMax.get(i));
                                        }
                                    } else
                                        Log.i("myTag", "No daily data found.");
                                }

                                @Override
                                public void onError(Exception e) {
                                    Log.e("myTag", "Error retrieving weather data: " + e.getMessage());

                                }
                            });
                        }
                        else {
                            MainActivity.this.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    weatherInfo.setText("City not found.");
                                }
                            });
                        }

                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        });
    }

    private void getWeatherData(double latitude, double longitude, WeatherCallback callback) {
        String url = "https://api.open-meteo.com/v1/forecast?latitude=" + latitude +
                "&longitude=" + longitude + "&current=temperature_2m,relative_humidity_2m,weather_code,wind_speed_10m&hourly=temperature_2m,precipitation_probability,weather_code,uv_index&daily=weather_code,temperature_2m_max,temperature_2m_min,uv_index_max,precipitation_probability_max,wind_speed_10m_max&timezone=Asia%2FBangkok&forecast_days=14";
        Request request = new Request.Builder()
                .url(url)
                .build();


        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onError(e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    final String responseData = response.body().string();
                    Gson gson = new Gson();


                    WeatherResponse weatherResponse = gson.fromJson(responseData, WeatherResponse.class);
                    callback.onSuccess(weatherResponse);


                }
                else {
                    callback.onError(new Exception("Response not successful: " + response.code()));
                }
            }


        });

    }

    @Override
    public void onItemClick(int position) {
        Toast.makeText(this, "Clicked item at position " + position, Toast.LENGTH_SHORT).show();
    }

    public static class FormatUtils {
        public static String formatTemperature(double temperature) {
            return String.format(Locale.US, "%.0f °C", temperature);
        }
        public static String formatHumidity(double humidity) {
            return String.format(Locale.US, "%.0f%%", humidity);
        }

        public static String formatWindSpeed(double windSpeed) {
            return String.format(Locale.US, "%.0f km/h", windSpeed);
        }

        public static String formatRainChance(int rainChance) {
            return String.format(Locale.US, "%d%%", rainChance);
        }

        public static String formatUvIndex(double uvIndex) {
            return String.format(Locale.US, "%.0f", uvIndex);
        }
    }

}