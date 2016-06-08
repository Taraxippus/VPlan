package com.taraxippus.vplan;


import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.TabLayout;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.taraxippus.vplan.MainActivity;
import com.taraxippus.vplan.R;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import android.text.format.DateFormat;
import android.os.SystemClock;
import android.graphics.Typeface;
import java.text.ParseException;

public class MainActivity extends AppCompatActivity 
{
	LinearLayout layout_today;
	SwipeRefreshLayout swipeLayout_today;
	
	LinearLayout layout_tomorrow;
	SwipeRefreshLayout swipeLayout_tomorrow;
	
	ViewPager viewPager;
	TabLayout tabLayout;
	
	DBHelper dbHelper;
	
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
		setSupportActionBar((Toolbar) this.findViewById(R.id.toolbar));
		
		dbHelper = new DBHelper(this);
		
		if (PreferenceManager.getDefaultSharedPreferences(this).getLong("notification_time", 0) == 0)
		{
			Calendar calendar = Calendar.getInstance();
			calendar.set(Calendar.HOUR_OF_DAY, 7);
			calendar.set(Calendar.MINUTE, 0);
			
			if (calendar.getTimeInMillis() < Calendar.getInstance().getTimeInMillis())
				calendar.add(Calendar.DATE, 1);
			
			((AlarmManager) getSystemService(Context.ALARM_SERVICE)).setInexactRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, PendingIntent.getBroadcast(this, 0, new Intent(this, AlarmReceiver.class), PendingIntent.FLAG_UPDATE_CURRENT));
		}
		
		final SwipeRefreshLayout.OnRefreshListener refreshListener = new SwipeRefreshLayout.OnRefreshListener()
		{
			@Override
			public void onRefresh()
			{
				update(true);
			}
		};
		
		viewPager = (ViewPager) this.findViewById(R.id.pager);
		viewPager.setAdapter(new PagerAdapter()
		{
				public Object instantiateItem(ViewGroup collection, int position)
				{
					LayoutInflater inflater = LayoutInflater.from(MainActivity.this);
					ViewGroup layout = (ViewGroup) inflater.inflate(R.layout.page, collection, false);
					collection.addView(layout);
					
					if (position == 0)
					{
						layout_today = (LinearLayout) layout.findViewById(R.id.layout_cards);
						
						swipeLayout_today = (SwipeRefreshLayout)layout.findViewById(R.id.layout_swipe);
						swipeLayout_today.setColorSchemeResources(R.color.primary, R.color.accent);
						swipeLayout_today.setOnRefreshListener(refreshListener);
					}
					else
					{
						layout_tomorrow = (LinearLayout) layout.findViewById(R.id.layout_cards);
						
						swipeLayout_tomorrow = (SwipeRefreshLayout)layout.findViewById(R.id.layout_swipe);
						swipeLayout_tomorrow.setColorSchemeResources(R.color.primary, R.color.accent);
						swipeLayout_tomorrow.setOnRefreshListener(refreshListener);
					}
					
					if (swipeLayout_today != null && swipeLayout_tomorrow != null)
						update(false);
					
					return layout;
				}

				@Override
				public void destroyItem(ViewGroup collection, int position, Object view) 
				{
					collection.removeView((View) view);
				}

				@Override
				public int getCount() 
				{
					return 2;
				}

				@Override
				public boolean isViewFromObject(View view, Object object)
				{
					return view == object;
				}

				@Override
				public CharSequence getPageTitle(int position)
				{
					return getString(Info.TABS[position]);
				}
				
		});
		
		if (Calendar.getInstance().get(Calendar.HOUR_OF_DAY) > 16)
		{
			viewPager.setCurrentItem(1);
		}
		
		tabLayout = (TabLayout) this.findViewById(R.id.tab_layout);
		tabLayout.setupWithViewPager(viewPager);
	}
	
	public boolean onCreateOptionsMenu(Menu menu)
	{
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{
			case R.id.settings:
				this.startActivity(new Intent(this, SettingsActivity.class));
				return true;
			
			case R.id.cafeteria:
				this.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(Info.URL_MENU)));
				return true;
				
			case R.id.about:
				
				final AlertDialog alertDialog = new AlertDialog.Builder(this).create();
				alertDialog.setTitle(R.string.about);
				alertDialog.setMessage(getString(R.string.about_app));
				alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, getString(android.R.string.ok), new AlertDialog.OnClickListener()
				{
						@Override
						public void onClick(DialogInterface p1, int p2)
						{
							p1.dismiss();
						}
				});
				alertDialog.show();
				
				return true;
				
			case R.id.test:
				sendBroadcast(new Intent(MainActivity.this, AlarmReceiver.class));
				return true;
				
				
			default:
				return false;
		}
	}

	@Override
	protected void onResume()
	{
		super.onResume();
		
		if (swipeLayout_today != null && swipeLayout_tomorrow != null && System.currentTimeMillis() - PreferenceManager.getDefaultSharedPreferences(this).getLong("lastUpdate", 0) > 1000 * 60 * 5)
			update(false);
	}
	
	
	public boolean hasInternetConnection()
	{
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		
		NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
		return networkInfo != null && networkInfo.isConnected();
	}
	
	public void update(View unused)
	{
		update(false);
	}
	
	public void update(final boolean force)
	{
		swipeLayout_today.setRefreshing(true);
		swipeLayout_tomorrow.setRefreshing(true);
		
		if (!hasInternetConnection())
		{
			Toast.makeText(this, R.string.connection_error, Toast.LENGTH_SHORT).show();
			
			layout_today.removeAllViews();
			getLayoutInflater().inflate(R.layout.card_message, layout_today, true);
			
			ArrayList<View> old = updateCards(true);
			for (View v : old)
				layout_today.addView(v);

			layout_tomorrow.removeAllViews();
			getLayoutInflater().inflate(R.layout.card_message, layout_tomorrow, true);

			old = updateCards(false);
			for (View v : old)
				layout_tomorrow.addView(v);
	
			swipeLayout_today.setRefreshing(false);
			swipeLayout_tomorrow.setRefreshing(false);
			return;
		}
		
		PreferenceManager.getDefaultSharedPreferences(this).edit().putLong("lastUpdate", System.currentTimeMillis()).apply();
		
		try
		{
			new AsyncTask<String, Void, ArrayList<View>>()
			{
				@Override
				protected ArrayList<View> doInBackground(String... urls)
				{
					try
					{
						updateInfo(MainActivity.this, dbHelper, urls[0], true, force);
						return updateCards(true);
					}
					catch (Exception e)
					{
						e.printStackTrace();
						
						final TextView text = new TextView(MainActivity.this);
						final ArrayList<View> list = new ArrayList<>();
						
						text.setText("Error: " + e);
						list.add(text);
						return list;
					}
				}
				
				@Override
				protected void onPostExecute(ArrayList<View> result)
				{
					layout_today.removeAllViews();
					
					for (View v : result)
						layout_today.addView(v);
				
					swipeLayout_today.setRefreshing(false);
				}
			}.execute(Info.URL_TODAY);
			
			new AsyncTask<String, Void, ArrayList<View>>()
			{
				@Override
				protected ArrayList<View> doInBackground(String... urls)
				{
					try
					{
						updateInfo(MainActivity.this, dbHelper, urls[0], false, force);
						return updateCards(false);
					}
					catch (Exception e)
					{
						e.printStackTrace();

						final TextView text = new TextView(MainActivity.this);
						final ArrayList<View> list = new ArrayList<>();

						text.setText("Error: " + e);
						list.add(text);
						return list;
					}
				}

				@Override
				protected void onPostExecute(ArrayList<View> result)
				{
					layout_tomorrow.removeAllViews();

					for (View v : result)
						layout_tomorrow.addView(v);

					swipeLayout_tomorrow.setRefreshing(false);
				}
			}.execute(Info.URL_TOMORROW);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public static void updateInfo(Context context, DBHelper dbHelper, String url, boolean today, boolean force) throws IOException
	{
		BufferedReader reader = null;
		HttpURLConnection conn = null;
		
		try 
		{
			conn = (HttpURLConnection) new URL(url).openConnection();
			conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_10_3) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/44.0.2403.155 Safari/537.36");
			conn.setRequestProperty("Accept-Charset", "ISO-8859-1");
			conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=iso-8859-1");
			conn.setReadTimeout(10000);
			conn.setConnectTimeout(5000);
			conn.setRequestMethod("GET");
			conn.setDoInput(true);
			conn.connect();
			
			if (!force && !wasModified(conn, context, today))
			{
				conn.disconnect();
				return;
			}
			
			reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), "ISO-8859-1"));        

			StringBuffer sb = new StringBuffer();
			String line;

			while((line = reader.readLine()) != null)
				sb.append(line);

			String contentAsString = sb.toString();
			
			dbHelper.delete(today ? 0 : 1);
			dbHelper.delete(today ? 2 : 3);
			
			Matcher m = Pattern.compile("<tr").matcher(contentAsString);
			Matcher m1;
			
			int i = 0, index1;
			String row;
			
			while (m.find())
			{
				if (i == 0)
				{
					row = contentAsString.substring(m.start(), 4 + contentAsString.indexOf("/tr>", m.start()));

					m1 = Pattern.compile("<td").matcher(row);
					m1.find();
					
					index1 = row.indexOf(">", m1.start());
					dbHelper.add("title", 0, row.substring(index1 + 1, row.indexOf("<", index1)).trim(), today ? 0 : 1);
				}
				if (i > 1)
					parseColumn(context, dbHelper, contentAsString.substring(m.start(), 4 + contentAsString.indexOf("/tr>", m.start())), today);
					
				i++;
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			if (reader != null)
				reader.close();
				
			if (conn != null)
				conn.disconnect();
		}
	}

	public ArrayList<View> updateCards(boolean today)
	{
		final ArrayList<View> result = new ArrayList<>();
		
		float dp = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, getResources().getDisplayMetrics());

		TextView text = new TextView(this);
		text.setPadding((int) dp / 2, (int) dp, (int) dp, (int) dp);
		TypedValue appearance = new TypedValue();
		getTheme().resolveAttribute(android.R.attr.textAppearanceMedium, appearance, true);
		text.setTextAppearance(appearance.resourceId);
		text.setTextColor(getResources().getColor(R.color.accent));
		text.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
		text.setText(dbHelper.getContent("title", 0, today ? 0 : 1));

		result.add(text);
		
		ArrayList<String> grades = dbHelper.getGrades(today ? 2 : 3);
		ArrayList<String[]> periods;
		String[] entries;
		StringBuilder content;
		
		String ROW = PreferenceManager.getDefaultSharedPreferences(this).getString("row", "");
		
		View card, text_content;
		LinearLayout layout_content;
		
		for (String grade : grades)
		{
			if (ROW.isEmpty() || ROW.equalsIgnoreCase(grade))
			{
				card = getLayoutInflater().inflate(R.layout.card_content, layout_today, false);
				((TextView) card.findViewById(R.id.text_title)).setText(grade);
				layout_content = (LinearLayout) card.findViewById(R.id.layout_content);
				
				periods = dbHelper.getEntries(grade, today ? 2 : 3);

				if (periods.size() == 1 && periods.get(0)[1].isEmpty())
				{
					text_content = getLayoutInflater().inflate(R.layout.row_content, layout_content, false);

					((TextView) text_content.findViewById(R.id.text_content)).setText(getString(R.string.regular));
					layout_content.addView(text_content);
				}
				else
				{
					for (String[] period : periods)
					{
						if (period[1].isEmpty())
							continue;
							
						content = new StringBuilder();
							
						entries = period[1].split("\\\\");
						for (int i = 0; i < entries.length; ++i)
						{
							if (entries[i].contains("siehe"))
								entries[i] = dbHelper.getContent(entries[i].substring(6, entries[i].length()), Integer.parseInt(period[0].substring(0, period[0].length() - 1)), today ? 2 : 3);
							
							if (i != 0)
								content.append("<br />");
								
							content.append(entries[i]);
						}
						
						text_content = getLayoutInflater().inflate(R.layout.row_content, layout_content, false);

						((TextView) text_content.findViewById(R.id.text_number)).setText(period[0]);
						((TextView) text_content.findViewById(R.id.text_content)).setText(Html.fromHtml(content.toString()));
						layout_content.addView(text_content);
					}
				}
				result.add(card);
			}
		}
		
		text = new TextView(this);
		text.setPadding((int) dp / 2, (int) dp, (int) dp, (int) dp);
		text.setTextAppearance(android.R.attr.textAppearanceSmall);
		text.setTypeface(Typeface.defaultFromStyle(Typeface.ITALIC));
		text.setText(Html.fromHtml(getString(R.string.last_modified) + ": " + Info.formatDate(this, new Date(PreferenceManager.getDefaultSharedPreferences(this).getLong("lastModified" + (today ? "_today" : "_tomorrow"), 0))) + " • " + 
								   getString(R.string.last_updated) + ": " + Info.formatDate(this, new Date(PreferenceManager.getDefaultSharedPreferences(this).getLong("lastUpdate", 0)))));

		result.add(text);
			
		return result;
	}
	
	public static void parseColumn(Context context, DBHelper dbHelper, String row, boolean today)
	{
		Matcher m = Pattern.compile("<td").matcher(row), m1;

		int index;
		int i = 0;
		String column;
		String grade = "5a";
		ArrayList<Integer> periods = new ArrayList<Integer>();
		
		while (m.find())
		{
			index = row.indexOf(">", m.start());
			column = row.substring(index + 1, row.indexOf("<", index)).replace("&nbsp;", "").replace("  ", " ").trim();
			
			if (i == 0)
			{
				grade = column;
				if (grade.isEmpty())
					return;
			}
			else if (i != 9 && !column.isEmpty())
			{
				periods.clear();
				
				if (column.matches("\\d+\\..*"))
				{
					m1 = Pattern.compile("(\\d+)\\.").matcher(column);
					
					while (m1.find())
					{
						periods.add(Integer.parseInt(m1.group(1)));
						index = m1.end(1);
					}
						
					column = column.substring(index + 1).replace("Std.", "").replace("Stunde", "");
				}
				else
					periods.add(i);
				
				for (String entry : column.split("/"))
				{
					for (String s : Info.TEACHERS.keySet())
						entry = entry.replaceAll("\\b" + s + "\\b", "<i>" + Info.TEACHERS.get(s) + "</i>");

					if (entry.equalsIgnoreCase("f"))
					{
						entry = "<i>" + context.getString(R.string.ausfall) + "</i>";
					}
					else if (entry.equalsIgnoreCase("MP"))
					{
						entry = "<i>" + context.getString(R.string.mittagspause) + "</i>";
					}
					else if (entry.equalsIgnoreCase("XXX"))
					{
						entry = "<i>" + context.getString(R.string.ausflug) + "</i>";
					}
					else if (entry.contains("f.a.") || entry.contains("Aufg"))
					{
						entry = "<i>" + entry.replace("f.a.", context.getString(R.string.ausfall)).replace("f.a", context.getString(R.string.ausfall)).replace("Aufg.", "Aufgaben").replace("Aufgaben", context.getString(R.string.aufgaben)) + "</i>";
					}
					else if (entry.equalsIgnoreCase("verl") || entry.equalsIgnoreCase("verl."))
					{
						entry = "<i>" + context.getString(R.string.verlegt) + "</i>";
					}
					
					for (Integer period : periods)
						dbHelper.add(grade, period, entry.trim(), today ? 2 : 3);
				}
			}
			
			++i;
		}
		
		dbHelper.add(grade, 0, "", today ? 2 : 3);
	}
	
	
	public static boolean wasModified(URLConnection conn, Context context, boolean today) throws ParseException
	{
		Map<String, List<String>> headerFields = conn.getHeaderFields();

		for (String s : headerFields.keySet()) 
		{
			if ("Last-Modified".equals(s))
			{
				long time = Info.htmlDateFormat.parse(headerFields.get(s).get(0)).getTime();
				
				if (PreferenceManager.getDefaultSharedPreferences(context).getLong("lastModified" + (today ? "_today" : "_tomorrow"), 0) != time)
				{
					PreferenceManager.getDefaultSharedPreferences(context).edit().putLong("lastModified" + (today ? "_today" : "_tomorrow"), time).apply();
					return true;
				}
				else 
					return false;
			}
		}
		
		return true;
	}
}
