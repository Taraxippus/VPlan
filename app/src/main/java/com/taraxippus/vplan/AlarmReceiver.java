package com.taraxippus.vplan;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v7.app.NotificationCompat;
import android.text.Html;
import com.taraxippus.vplan.MainActivity;
import com.taraxippus.vplan.R;
import java.util.ArrayList;
import android.text.format.DateFormat;
import java.util.Date;

public class AlarmReceiver extends BroadcastReceiver
{
	@Override
	public void onReceive(Context c, Intent i)
	{
		new UpdateTask(c).execute();
	}
	
	public class UpdateTask extends AsyncTask<String, Void, Boolean>
	{
		final Context context;
		final DBHelper dbHelper;
		
		public UpdateTask(Context context)
		{
			this.context = context;
			this.dbHelper = new DBHelper(context);
		}
		
		@Override
		protected Boolean doInBackground(String[] p1)
		{
			try
			{
				MainActivity.updateInfo(context, dbHelper, Info.URL_TODAY, true, false);
				return true;
			}
			catch (Exception e)
			{
				e.printStackTrace();
				return false;
			}
		}

		@Override
		protected void onPostExecute(Boolean result)
		{
			NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
			NotificationCompat.Builder notification = new NotificationCompat.Builder(context);
			
			if (result)
			{
				StringBuilder text = new StringBuilder();

				ArrayList<String> grades = dbHelper.getGrades(2);
				ArrayList<String[]> periods;

				String ROW = PreferenceManager.getDefaultSharedPreferences(context).getString("row", "");

				for (String grade : grades)
				{
					if (ROW.isEmpty() || ROW.equalsIgnoreCase(grade))
					{
						if (text.length() > 0)
							text.append("<br />");
						
						if (ROW.isEmpty())
							text.append("<b>").append(grade).append("</b> - ");
						
						periods = dbHelper.getEntries(grade, 2);

						if (periods.size() == 1 && periods.get(0)[1].isEmpty())
						{
							text.append(context.getString(R.string.regular));
						}
						else
						{
							for (String[] period : periods)
							{
								if (period[1].isEmpty())
									continue;
									
								if (text.length() > 0)
									text.append("<br />");
								
								text.append(period[0]).append(" ").append(period[1].replace("\\", " <br /> "));
							}
						}
					}
				}
				notification.setContentTitle(context.getString(R.string.app_name) + " " + context.getString(R.string.today) + " " + ROW);
				notification.setContentText(Html.fromHtml(text.toString()));
				notification.setStyle(new NotificationCompat.BigTextStyle().bigText(Html.fromHtml(text.toString())));
				notification.setColor(context.getResources().getColor(R.color.primary));
				notification.setSmallIcon(R.drawable.ic_launcher);
				notification.setContentIntent(PendingIntent.getActivity(context, 0, new Intent(context, MainActivity.class), 0));
				
				nm.notify(R.string.notification_id, notification.build());
			}
			else
			{
				notification.setContentTitle(context.getString(R.string.app_name));
				notification.setContentText(context.getString(R.string.failed_to_update));
				notification.setColor(context.getResources().getColor(R.color.primary));
				notification.setSmallIcon(R.drawable.ic_launcher);
				notification.setContentIntent(PendingIntent.getActivity(context, 0, new Intent(context, MainActivity.class), 0));

				nm.notify(R.string.notification_id, notification.build());
			}
		}
	}
}
