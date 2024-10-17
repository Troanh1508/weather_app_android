package com.example.weatherapp;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.Context;
import android.widget.RemoteViews;

public class WeatherWidget extends AppWidgetProvider {
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {

            SharedPreferences sharedPreferences = context.getSharedPreferences("WeatherPrefs", Context.MODE_PRIVATE);
            String temperature = sharedPreferences.getString("temperature", "N/A");
            String weatherDescription = sharedPreferences.getString("weather_desc", "N/A");
            String uvIndex = sharedPreferences.getString("uvIndex", "N/A");
            Integer widgetIconRes = sharedPreferences.getInt("widgetIconRes", R.drawable.sunny);

            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_layout);

            // Update the widget views with weather data
            views.setTextViewText(R.id.widgetUvIndex, "UV: " + uvIndex);
            views.setTextViewText(R.id.widgetTemperature, temperature);
            views.setTextViewText(R.id.widgetWeatherDesc, weatherDescription);
            views.setImageViewResource(R.id.widgetIconRes, widgetIconRes);

            // Intent to open the app
            Intent appIntent = new Intent(context, MainActivity.class);
            PendingIntent appPendingIntent = PendingIntent.getActivity(context, 0, appIntent, PendingIntent.FLAG_IMMUTABLE);
            views.setOnClickPendingIntent(R.id.widget_layout, appPendingIntent);

            // Intent to refresh the widget
            Intent refreshIntent = new Intent(context, WeatherWidget.class);
            refreshIntent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
            refreshIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds);
            PendingIntent refreshPendingIntent = PendingIntent.getBroadcast(context, 0, refreshIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
            views.setOnClickPendingIntent(R.id.refreshButton, refreshPendingIntent);

            appWidgetManager.updateAppWidget(appWidgetId, views);
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        if (AppWidgetManager.ACTION_APPWIDGET_UPDATE.equals(intent.getAction())) {
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            ComponentName thisWidget = new ComponentName(context, WeatherWidget.class);
            int[] appWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);
            onUpdate(context, appWidgetManager, appWidgetIds);
        }
    }
}
