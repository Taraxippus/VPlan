package com.taraxippus.vplan;

import android.appwidget.*;
import android.content.*;
import android.text.*;
import android.widget.*;
import android.preference.*;

public class AppWidgetProvider extends android.appwidget.AppWidgetProvider
{
	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds)
	{
        final int N = appWidgetIds.length;
		
        for (int i = 0; i < N; i++) 
		{
            int appWidgetId = appWidgetIds[i];

			SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
			
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.appwidget);
			views.setTextViewText(R.id.text_today_title, Html.fromHtml("<b>" + preferences.getString("today_title", "") + "</b>"));
			views.setTextViewText(R.id.text_today, Html.fromHtml(preferences.getString("today_0_content", "")));
			views.setTextViewText(R.id.text_tomorrow_title, Html.fromHtml("<b>" + preferences.getString("tomorrow_title", "") + "</b>"));
			views.setTextViewText(R.id.text_tomorrow,  Html.fromHtml(preferences.getString("tomorrow_0_content", "")));
			
            appWidgetManager.updateAppWidget(appWidgetId, views);
        }
    }
}
