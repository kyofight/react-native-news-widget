package com.newswidget;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.os.Bundle;
import android.content.Intent;
import android.util.Log;

/**
 * Implementation of App Widget functionality.
 */
public class Widget extends AppWidgetProvider {
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            Log.d("Debug", "appWidgetId: " + appWidgetId);
            Intent intent = new Intent(context, WidgetService.class);
            intent.putExtra("appWidgetId", appWidgetId);
            intent.putExtra("packageName", context.getPackageName());
            Log.d("Debug", "onUpdate " + appWidgetId);
            context.startService(intent);
        }
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }

    @Override
    public void onAppWidgetOptionsChanged(Context context, AppWidgetManager appWidgetManager,
                                          int appWidgetId, Bundle newOptions) {
//
//        int minWidth = newOptions.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH);
//        int maxWidth = newOptions.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH);
//        int minHeight = newOptions.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT);
//        int maxHeight = newOptions.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT);
//
//        RemoteViews layout;
//        if (minHeight < 100) {
//            mIsLargeLayout = false;
//        } else {
//            mIsLargeLayout = true;
//        }
//        layout = buildLayout(context, appWidgetId, mIsLargeLayout);
//        appWidgetManager.updateAppWidget(appWidgetId, layout);
    }
}
