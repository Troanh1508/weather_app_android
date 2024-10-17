package com.example.weatherapp;

import androidx.annotation.DrawableRes;

public class HourlyItem {
    private String hour;
    private String temperature;
    private int iconRes;

    public HourlyItem(String hour, String temperature, @DrawableRes int iconRes) {
        this.hour = hour;
        this.temperature = temperature;
        this.iconRes = iconRes;
    }

    public String getHour() {
        return hour;
    }

    public void setHour(String hour) {
        this.hour = hour;
    }

    public String getTemperature() {
        return temperature;
    }

    public void setTemperature(String temperature) {
        this.temperature = temperature;
    }

    public int getIconRes() {
        return iconRes;
    }

    public void setIconRes(int iconRes) {
        this.iconRes = iconRes;
    }
}
