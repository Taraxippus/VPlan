package com.taraxippus.vplan;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.preference.PreferenceManager;
import android.text.Html;
import com.taraxippus.vplan.MainActivity;
import com.taraxippus.vplan.R;
import java.util.ArrayList;

public class AlarmReceiver extends BroadcastReceiver
{
	@Override
	public void onReceive(Context c, Intent i)
	{
		DBHelper dbHelper = new DBHelper(c);
		
		NotificationManager nm = (NotificationManager) c.getSystemService(Context.NOTIFICATION_SERVICE);

		Notification.Builder notification = new Notification.Builder(c);

		StringBuilder text = new StringBuilder();

		ArrayList<String> grades = dbHelper.getGrades(2);
		ArrayList<String[]> periods;

		String ROW = PreferenceManager.getDefaultSharedPreferences(c).getString("row", "");

		for (String grade : grades)
		{
			if (ROW.isEmpty() || ROW.equalsIgnoreCase(grade))
			{
				periods = dbHelper.getEntries(grade, 2);

				if (periods.isEmpty())
				{
					text.append(c.getString(R.string.regular));
				}
				else
				{
					for (String[] period : periods)
					{
						if (text.length() > 0)
							text.append("<br />");

						text.append(period[0]).append(" ").append(period[1].replace("\\", " <br /> "));
					}
				}
			}
		}
		notification.setContentTitle("VPlan " + c.getString(R.string.today) + " " + ROW);
		notification.setContentText(Html.fromHtml(text.toString()));
		notification.setStyle(new Notification.BigTextStyle().bigText(Html.fromHtml(text.toString())));
		notification.setColor(c.getResources().getColor(R.color.primary));
		notification.setSmallIcon(R.drawable.ic_launcher);
		notification.setContentIntent(PendingIntent.getActivity(c, 0, new Intent(c, MainActivity.class), 0));

		nm.notify(R.string.notification_id, notification.build());
		
	}
}
