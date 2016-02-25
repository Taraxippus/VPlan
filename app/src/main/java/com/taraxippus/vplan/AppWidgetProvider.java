package com.taraxippus.vplan;

import android.appwidget.*;
import android.content.*;
import android.text.*;
import android.widget.*;

public class AppWidgetProvider extends android.appwidget.AppWidgetProvider
{
	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds)
	{
        final int N = appWidgetIds.length;
		
        for (int i = 0; i < N; i++) 
		{
            int appWidgetId = appWidgetIds[i];

    
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.appwidget);
			views.setTextViewText(R.id.text_today_title, Html.fromHtml("<b>" + context.getResources().getString(R.string.today) + "</b>"));
			//views.setTextViewText(R.id.text_today, Html.fromHtml("Today"));
			views.setTextViewText(R.id.text_tomorrow_title, Html.fromHtml("<b>" + context.getResources().getString(R.string.tomorrow) + "</b>"));
			//views.setTextViewText(R.id.text_tomorrow,  Html.fromHtml("Tomorrow"));
			
            appWidgetManager.updateAppWidget(appWidgetId, views);
        }
    }
}
