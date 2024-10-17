package com.example.weatherapp;

public class DailyItem {
    private String day;
    private String weatherDescription;
    private String maxTemp;
    private String minTemp;
    private int iconRes;

    public DailyItem(String day, String weatherDescription, String maxTemp, String minTemp, int iconRes) {
        this.day = day;
        this.weatherDescription = weatherDescription;
        this.maxTemp = maxTemp;
        this.minTemp = minTemp;
        this.iconRes = iconRes;
    }

    public String getDay() {
        return day;
    }

    public void setDay(String day) {
        this.day = day;
    }

    public String getWeatherDescription() {
        return weatherDescription;
    }

    public void setWeatherDescription(String weatherDescription) {
        this.weatherDescription = weatherDescription;
    }

    public String getMaxTemp() {
        return maxTemp;
    }

    public void setMaxTemp(String maxTemp) {
        this.maxTemp = maxTemp;
    }

    public String getMinTemp() {
        return minTemp;
    }

    public void setMinTemp(String minTemp) {
        this.minTemp = minTemp;
    }

    public int getIconRes() {
        return iconRes;
    }

    public void setIconRes(int iconRes) {
        this.iconRes = iconRes;
    }
}
