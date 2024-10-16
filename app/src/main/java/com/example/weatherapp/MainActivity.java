package com.example.weatherapp;

import static com.example.weatherapp.WeatherType.fromWMO;

import android.app.Activity;
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

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    private FusedLocationProviderClient fusedLocationClient;
    private static final int REQUEST_CODE = 100;

    private TextView weatherInfo;
    private TextView weatherDescription;
    private TextView currentDateTime;
    private TextView currentTemp;
    private TextView maxminTemp;
    private TextView windSpeed;
    private TextView humidity;
    private TextView rainChance;
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
        currentWeatherIcon = findViewById(R.id.currentWeatherIcon);


        Button getWeatherButton = findViewById(R.id.getWeatherButton);
        getWeatherButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                city = cityText.getText().toString().trim();
                if (!city.isEmpty()) {
                    getLocationData(city);
                } else {
                    weatherInfo.setText(R.string.please_enter_city_name);
                }
            }
        });
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
                                getCityName(latitude, longitude);
                            }
                        });
            } else {
                // Permission is not granted, request it
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE);
            }
    }

    private void getCityName(double latitude, double longitude) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            if (addresses != null && !addresses.isEmpty()) {
                String cityName = addresses.get(0).getAdminArea();
                Log.d("myTag", "City: " + cityName);
                MainActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        cityText.setText(cityName);
                        Log.d("myTag", "City: " + cityName);
                    }
                });
            }
            else {
                Log.d("myTag", "No address found.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
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
                                                TemperatureUtils.formatTemperature(weatherResponse.getDaily().getTemperature_2m_max().get(0))
                                                            + " Lowest: " +
                                                TemperatureUtils.formatTemperature(weatherResponse.getDaily().getTemperature_2m_min().get(0)));
                                        currentTemp.setText(TemperatureUtils.formatTemperature(weatherResponse.getCurrent().getTemperature()));
                                        humidity.setText(TemperatureUtils.formatHumidity(weatherResponse.getCurrent().getRelativeHumidity()));
                                        windSpeed.setText(TemperatureUtils.formatWindSpeed(weatherResponse.getCurrent().getWindSpeed()));
                                        int currentHour = LocalTime.now().getHour();
                                        rainChance.setText(TemperatureUtils.formatRainChance(weatherResponse.getHourly().getPrecipitationProbability().get(currentHour)));
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
                "&longitude=" + longitude + "&current=temperature_2m,relative_humidity_2m,weather_code,wind_speed_10m&hourly=precipitation_probability,uv_index&daily=temperature_2m_max,temperature_2m_min,uv_index_max";
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
//                        JSONObject weatherData = new JSONObject(responseData);
//                        JSONObject currentWeatherData = (JSONObject) weatherData.get("current");
//
//                        String time = (String) currentWeatherData.get("time");
//                        double temperature = (double) currentWeatherData.get("temperature_2m");
//                        int relativeHumidity = (int) currentWeatherData.get("relative_humidity_2m");
//                        double windSpeed = (double) currentWeatherData.get("wind_speed_10m");

//                        Weather weather = new Weather(time, temperature, relativeHumidity, windSpeed);
                    callback.onSuccess(weatherResponse);


                }
                else {
                    callback.onError(new Exception("Response not successful: " + response.code()));
                }
            }


        });

    }

    public static class TemperatureUtils {
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
    }

}