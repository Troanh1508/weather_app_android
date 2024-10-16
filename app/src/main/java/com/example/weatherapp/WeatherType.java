package com.example.weatherapp;

import androidx.annotation.DrawableRes;

public abstract class WeatherType {
    private final String weatherDesc;
    @DrawableRes
    private final int iconRes;

    protected WeatherType(String weatherDesc, @DrawableRes int iconRes) {
        this.weatherDesc = weatherDesc;
        this.iconRes = iconRes;
    }

    public String getWeatherDesc() {
        return weatherDesc;
    }

    public int getIconRes() {
        return iconRes;
    }

    public static class ClearSky extends WeatherType {
        public ClearSky() {
            super("Clear sky", R.drawable.sunny);
        }
    }

    public static class MainlyClear extends WeatherType {
        public MainlyClear() {
            super("Mainly clear", R.drawable.sunny);
        }
    }

    public static class PartlyCloudy extends WeatherType {
        public PartlyCloudy() {
            super("Partly cloudy", R.drawable.cloudy_sunny);
        }
    }

    public static class Overcast extends WeatherType {
        public Overcast() {
            super("Overcast", R.drawable.cloudy);
        }
    }

    public static class Foggy extends WeatherType {
        public Foggy() {
            super("Foggy", R.drawable.ic_very_cloudy);
        }
    }

    public static class DepositingRimeFog extends WeatherType {
        public DepositingRimeFog() {
            super("Depositing rime fog", R.drawable.ic_very_cloudy);
        }
    }

    public static class LightDrizzle extends WeatherType {
        public LightDrizzle() {
            super("Light drizzle", R.drawable.ic_rainshower);
        }
    }

    public static class ModerateDrizzle extends WeatherType {
        public ModerateDrizzle() {
            super("Moderate drizzle", R.drawable.ic_rainshower);
        }
    }

    public static class DenseDrizzle extends WeatherType {
        public DenseDrizzle() {
            super("Dense drizzle", R.drawable.ic_rainshower);
        }
    }

    public static class LightFreezingDrizzle extends WeatherType {
        public LightFreezingDrizzle() {
            super("Slight freezing drizzle", R.drawable.ic_snowyrainy);
        }
    }

    public static class DenseFreezingDrizzle extends WeatherType {
        public DenseFreezingDrizzle() {
            super("Dense freezing drizzle", R.drawable.ic_snowyrainy);
        }
    }

    public static class SlightRain extends WeatherType {
        public SlightRain() {
            super("Slight rain", R.drawable.ic_rainy);
        }
    }

    public static class ModerateRain extends WeatherType {
        public ModerateRain() {
            super("Rainy", R.drawable.ic_rainy);
        }
    }

    public static class HeavyRain extends WeatherType {
        public HeavyRain() {
            super("Heavy rain", R.drawable.ic_rainy);
        }
    }

    public static class HeavyFreezingRain extends WeatherType {
        public HeavyFreezingRain() {
            super("Heavy freezing rain", R.drawable.ic_snowyrainy);
        }
    }

    public static class SlightSnowFall extends WeatherType {
        public SlightSnowFall() {
            super("Slight snow fall", R.drawable.ic_snowy);
        }
    }

    public static class ModerateSnowFall extends WeatherType {
        public ModerateSnowFall() {
            super("Moderate snow fall", R.drawable.ic_heavysnow);
        }
    }

    public static class HeavySnowFall extends WeatherType {
        public HeavySnowFall() {
            super("Heavy snow fall", R.drawable.ic_heavysnow);
        }
    }

    public static class SnowGrains extends WeatherType {
        public SnowGrains() {
            super("Snow grains", R.drawable.ic_heavysnow);
        }
    }

    public static class SlightRainShowers extends WeatherType {
        public SlightRainShowers() {
            super("Slight rain showers", R.drawable.ic_rainshower);
        }
    }

    public static class ModerateRainShowers extends WeatherType {
        public ModerateRainShowers() {
            super("Moderate rain showers", R.drawable.ic_rainshower);
        }
    }

    public static class ViolentRainShowers extends WeatherType {
        public ViolentRainShowers() {
            super("Violent rain showers", R.drawable.ic_rainshower);
        }
    }

    public static class SlightSnowShowers extends WeatherType {
        public SlightSnowShowers() {
            super("Light snow showers", R.drawable.ic_snowy);
        }
    }

    public static class HeavySnowShowers extends WeatherType {
        public HeavySnowShowers() {
            super("Heavy snow showers", R.drawable.ic_snowy);
        }
    }

    public static class ModerateThunderstorm extends WeatherType {
        public ModerateThunderstorm() {
            super("Moderate thunderstorm", R.drawable.ic_thunder);
        }
    }

    public static class SlightHailThunderstorm extends WeatherType {
        public SlightHailThunderstorm() {
            super("Thunderstorm with slight hail", R.drawable.ic_rainythunder);
        }
    }

    public static class HeavyHailThunderstorm extends WeatherType {
        public HeavyHailThunderstorm() {
            super("Thunderstorm with heavy hail", R.drawable.ic_rainythunder);
        }
    }

    public static WeatherType fromWMO(int code) {
        switch (code) {
            case 0: return new ClearSky();
            case 1: return new MainlyClear();
            case 2: return new PartlyCloudy();
            case 3: return new Overcast();
            case 45: return new Foggy();
            case 48: return new DepositingRimeFog();
            case 51: return new LightDrizzle();
            case 53: return new ModerateDrizzle();
            case 55: return new DenseDrizzle();
            case 56: return new LightFreezingDrizzle();
            case 57: return new DenseFreezingDrizzle();
            case 61: return new SlightRain();
            case 63: return new ModerateRain();
            case 65: return new HeavyRain();
            case 66: return new LightFreezingDrizzle(); // Duplicate case
            case 67: return new HeavyFreezingRain();
            case 71: return new SlightSnowFall();
            case 73: return new ModerateSnowFall();
            case 75: return new HeavySnowFall();
            case 77: return new SnowGrains();
            case 80: return new SlightRainShowers();
            case 81: return new ModerateRainShowers();
            case 82: return new ViolentRainShowers();
            case 85: return new SlightSnowShowers();
            case 86: return new HeavySnowShowers();
            case 95: return new ModerateThunderstorm();
            case 96: return new SlightHailThunderstorm();
            case 99: return new HeavyHailThunderstorm();
            default: return new ClearSky(); // Default case
        }
    }
}