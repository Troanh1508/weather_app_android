package com.example.weatherapp;

public class Weather {
    private String time;
    private double temperature;
    private int humidity;
    private double windSpeed;

    public Weather(String time, double temperature, int humidity, double windSpeed) {
        this.time = time;
        this.temperature = temperature;
        this.humidity = humidity;
        this.windSpeed = windSpeed;
    }

    // Getters
    public String getTime() { return time; }
    public double getTemperature() { return temperature; }
    public int getHumidity() { return humidity; }
    public double getWindSpeed() { return windSpeed; }
}