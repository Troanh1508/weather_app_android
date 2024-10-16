package com.example.weatherapp;

import java.util.List;

public class WeatherResponse {
    private Current current;
    private Daily daily;
    private Hourly hourly;

    public Current getCurrent() {
        return current;
    }

    public Daily getDaily() {
        return daily;
    }

    public Hourly getHourly() {
        return hourly;
    }

    public static class Daily {
        private List<String> time;
        private List<Double> uv_index_max;
        public List<Double> temperature_2m_max;
        private List<Double> temperature_2m_min;

        public List<Double> getTemperature_2m_max() {
            return temperature_2m_max;
        }
        public List<Double> getTemperature_2m_min() {
            return temperature_2m_min;
        }
        public List<String> getTime() {
            return time;
        }
        public List<Double> getUvIndexMax() {
            return uv_index_max;
        }
    }

    public static class Hourly {
        private List<String> time;
        private List<Integer> precipitation_probability;
        private List<Double> uv_index;

        public List<String> getTime() {
            return time;
        }

        public List<Integer> getPrecipitationProbability() {
            return precipitation_probability;
        }

        public List<Double> getUvIndex() {
            return uv_index;
        }
    }

    public static class Current {
        private String time;
        private double temperature_2m;
        private int relative_humidity_2m;
        private double wind_speed_10m;
        private int weather_code;

        public String getTime() {
            return time;
        }

        public double getTemperature() {
            return temperature_2m;
        }

        public int getRelativeHumidity() {
            return relative_humidity_2m;
        }

        public double getWindSpeed() {
            return wind_speed_10m;
        }

        public int getWeatherCode() {
            return weather_code;
        }
    }
}