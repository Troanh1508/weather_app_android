package com.example.weatherapp;

import static com.example.weatherapp.WeatherType.fromWMO;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    private TextView weatherInfo;
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


        weatherInfo = findViewById(R.id.weatherInfoText);
        cityText = findViewById(R.id.cityText);
        currentWeatherIcon = findViewById(R.id.currentWeatherIcon);



        Button getWeatherButton = findViewById(R.id.getWeatherButton);
        getWeatherButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                city = cityText.getText().toString().trim();
                if (!city.isEmpty()){
                    getLocationData(city);
                }
                else {
                    weatherInfo.setText(R.string.please_enter_city_name);
                }
            }
        });
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
                                        currentWeatherIcon.setImageResource(fromWMO(code).getIconRes()); // Use appropriate resource ID
                                    });
//                                    Log.i("myTag", "Current humidity: " + weather.getHumidity());
//                                    Log.i("myTag", "Current wind speed: " + weather.getWindSpeed());
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
                "&longitude=" + longitude + "&current=temperature_2m,relative_humidity_2m,weather_code,wind_speed_10m&hourly=precipitation_probability,uv_index&daily=uv_index_max";
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
//                        Log.i("myTag", "Current time: " + time);
//                        Log.i("myTag", "Current temp: " + temperature);
//                        Log.i("myTag", "Current humidity: " + relativeHumidity);
//                        Log.i("myTag", "Current wind speed: " + windSpeed);


                }
                else {
                    callback.onError(new Exception("Response not successful: " + response.code()));
                }
            }


        });

    }
}