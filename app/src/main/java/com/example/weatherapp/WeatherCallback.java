package com.example.weatherapp;

public interface WeatherCallback {
    abstract void onSuccess(WeatherResponse weatherResponse);
    abstract void onError(Exception e);
}