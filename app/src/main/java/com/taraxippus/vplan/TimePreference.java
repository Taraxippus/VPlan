package com.taraxippus.vplan;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.preference.DialogPreference;
import android.text.format.DateFormat;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TimePicker;
import com.taraxippus.vplan.AlarmReceiver;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class TimePreference extends DialogPreference
 {
    private Calendar calendar;
    private TimePicker picker = null;

    public TimePreference(Context ctxt) 
	{
        this(ctxt, null);
    }

    public TimePreference(Context ctxt, AttributeSet attrs) 
	{
        this(ctxt, attrs, android.R.attr.dialogPreferenceStyle);
    }

    public TimePreference(Context ctxt, AttributeSet attrs, int defStyle) 
	{
        super(ctxt, attrs, defStyle);

		setDialogTitle(R.string.empty);
        setPositiveButtonText(android.R.string.ok);
        setNegativeButtonText(android.R.string.cancel);
        calendar = new GregorianCalendar();
    }

    @Override
    protected View onCreateDialogView()
	{
        picker = new TimePicker(getContext());
        return (picker);
    }

    @Override
    protected void onBindDialogView(View v) 
	{
        super.onBindDialogView(v);
        picker.setCurrentHour(calendar.get(Calendar.HOUR_OF_DAY));
        picker.setCurrentMinute(calendar.get(Calendar.MINUTE));
    }

    @Override
    protected void onDialogClosed(boolean positiveResult)
	{
        super.onDialogClosed(positiveResult);

        if (positiveResult) 
		{
            calendar.set(Calendar.HOUR_OF_DAY, picker.getCurrentHour());
            calendar.set(Calendar.MINUTE, picker.getCurrentMinute());
			if (calendar.getTimeInMillis() < Calendar.getInstance().getTimeInMillis())
				calendar.add(Calendar.DATE, 1);
			
            setSummary(getSummary());
			
            if (callChangeListener(calendar.getTimeInMillis())) 
			{
                persistLong(calendar.getTimeInMillis());
                notifyChanged();
				
				((AlarmManager) getContext().getSystemService(Context.ALARM_SERVICE)).setInexactRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, PendingIntent.getBroadcast(getContext(), 0, new Intent(getContext(), AlarmReceiver.class), PendingIntent.FLAG_UPDATE_CURRENT));
            }
        }
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index)
	{
        return (a.getString(index));
    }

    @Override
    protected void onSetInitialValue(boolean restoreValue, Object defaultValue)
	{
        if (restoreValue)
		{
            if (defaultValue == null)
			{
                calendar.setTimeInMillis(getPersistedLong(System.currentTimeMillis()));
            }
			else
			{
                calendar.setTimeInMillis(Long.parseLong(getPersistedString((String) defaultValue)));
            }
        } 
		else
		{
            if (defaultValue == null) 
			{
                calendar.set(Calendar.HOUR_OF_DAY, 7);
				calendar.set(Calendar.MINUTE, 0);
            }
			else 
			{
                calendar.setTimeInMillis(Long.parseLong((String) defaultValue));
            }
        }
        setSummary(getSummary());
    }

    @Override
    public CharSequence getSummary()
	{
        if (calendar == null)
		{
            return super.getSummary();
        }
        return DateFormat.getTimeFormat(getContext()).format(new Date(calendar.getTimeInMillis()));
    }
} 
