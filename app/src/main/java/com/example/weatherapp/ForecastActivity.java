package com.example.weatherapp;

import static com.example.weatherapp.WeatherType.fromWMO;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class ForecastActivity extends AppCompatActivity implements ItemClickListener{

    private RecyclerView.Adapter adapterDaily;
    private RecyclerView recyclerView;
    private ArrayList<DailyItem> items;


    private TextView weatherDescription;
    private TextView tmrTemp;
    private TextView maxTemp;
    private TextView minTemp;
    private TextView windSpeed;
    private TextView humidity;
    private TextView rainChance;
    private TextView uvIndex;
    private EditText cityText;
    ImageView tmrWeatherIcon;

    public final okhttp3.OkHttpClient client = new OkHttpClient();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_forecast);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        weatherDescription = findViewById(R.id.tmrWeatherDescription);
        tmrTemp = findViewById(R.id.tmrTempText);
        windSpeed = findViewById(R.id.tmrWindSpeed);
        rainChance = findViewById(R.id.tmrRainChance);
        uvIndex = findViewById(R.id.uvIndexTmr);

        tmrWeatherIcon = findViewById(R.id.tmrImageView);
        items = new ArrayList<>();

        Intent intent = getIntent();
        String cityName = intent.getStringExtra("city_name");

        getLocationData(cityName);

        ConstraintLayout backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(ForecastActivity.this, MainActivity.class));
            }
        });




    }


    private void initRecyclerView( ArrayList<DailyItem> items) {

        recyclerView = findViewById(R.id.recyclerForecast);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));

        adapterDaily = new DailyAdapter(items, this);
        recyclerView.setAdapter(adapterDaily);

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
                                        int code = weatherResponse.getDaily().getWeather_code().get(1);
                                        weatherDescription.setText(fromWMO(code).getWeatherDesc());
                                        tmrWeatherIcon.setImageResource(fromWMO(code).getIconRes());
                                        Double avgTemp = (weatherResponse.getDaily().getTemperature_2m_max().get(1) +
                                                weatherResponse.getDaily().getTemperature_2m_min().get(1)) / 2;
                                        tmrTemp.setText(MainActivity.FormatUtils.formatTemperature(avgTemp));
                                        windSpeed.setText(MainActivity.FormatUtils.formatWindSpeed(weatherResponse.getDaily().getWind_speed_10m_max().get(1)));
                                        rainChance.setText(MainActivity.FormatUtils.formatRainChance(weatherResponse.getDaily().getPrecipitation_probability_max().get(1)));
                                        uvIndex.setText(MainActivity.FormatUtils.formatUvIndex(weatherResponse.getDaily().getUvIndexMax().get(1)));
                                        items.clear();
                                        for (int i =1; i < weatherResponse.getDaily().getTime().size(); i++ )
                                        {
                                            int weatherCode = weatherResponse.getDaily().getWeather_code().get(i);
                                            int resId = fromWMO(weatherCode).getIconRes();
                                            String weatherDescription = fromWMO(weatherCode).getWeatherDesc();
                                            String maxTemp = MainActivity.FormatUtils.formatTemperature(weatherResponse.getDaily().getTemperature_2m_max().get(i));
                                            String minTemp = MainActivity.FormatUtils.formatTemperature(weatherResponse.getDaily().getTemperature_2m_min().get(i));
                                            String dateStr = weatherResponse.getDaily().getTime().get(i);
                                            LocalDate date = LocalDate.parse(dateStr, DateTimeFormatter.ISO_LOCAL_DATE);
                                            String dayOfWeek = date.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.ENGLISH);
                                            items.add(new DailyItem(dayOfWeek, weatherDescription, maxTemp, minTemp, resId));
                                        }
                                        initRecyclerView(items);
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
                            ForecastActivity.this.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {

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
                "&longitude=" + longitude + "&current=temperature_2m,relative_humidity_2m,weather_code,wind_speed_10m&hourly=temperature_2m,precipitation_probability,weather_code,uv_index&daily=weather_code,temperature_2m_max,temperature_2m_min,uv_index_max,precipitation_probability_max,wind_speed_10m_max&timezone=Asia%2FBangkok";
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

    @Override
    public void onItemClick(int position) {

    }
}