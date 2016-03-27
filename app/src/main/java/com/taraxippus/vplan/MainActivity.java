package com.taraxippus.vplan;

import android.app.*;
import android.content.*;
import android.net.*;
import android.os.*;
import android.preference.*;
import android.support.design.widget.*;
import android.support.v4.view.*;
import android.support.v4.widget.*;
import android.support.v7.app.*;
import android.support.v7.widget.*;
import android.text.*;
import android.util.*;
import android.view.*;
import android.widget.*;
import java.io.*;
import java.net.*;
import java.nio.charset.*;
import java.util.*;
import java.util.regex.*;

import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;

public class MainActivity extends AppCompatActivity 
{
	LinearLayout layout_today;
	SwipeRefreshLayout swipeLayout_today;
	
	LinearLayout layout_tomorrow;
	SwipeRefreshLayout swipeLayout_tomorrow;
	
	ViewPager viewPager;
	TabLayout tabLayout;
	
	public static final String URL_TODAY = "http://306.joomla.schule.bremen.de/ServerSync/V-Plan-heute.htm";
	public static final String URL_TOMORROW = "http://306.joomla.schule.bremen.de/ServerSync/V-Plan-morgen.htm";
	
	public static final int[] TABS = new int[] {R.string.today, R.string.tomorrow};
	
	public static final HashMap<String, String> TEACHERS = new HashMap<>();
	
	static
	{
		TEACHERS.put("Asl", "Aslan");
		TEACHERS.put("Bac", "Bachmann");
		TEACHERS.put("Bal", "Balster");
		TEACHERS.put("Bas", "Basedow");
		TEACHERS.put("Bcm", "Beckmann");
		TEACHERS.put("Bes", "von Bestenbostel");
		TEACHERS.put("Brt", "Birth");
		TEACHERS.put("Böh", "Böhm");
		TEACHERS.put("Bnt", "Brannath");
		TEACHERS.put("Gom", "Cano Gómez");
		TEACHERS.put("DA", "Dall Asta");
		TEACHERS.put("Dit", "Dittrich");
		TEACHERS.put("Dry", "Dreyer");
		TEACHERS.put("Ehl", "Ehlers");
		TEACHERS.put("Fng", "Fangmann");
		TEACHERS.put("Far", "Farke");
		TEACHERS.put("Gev", "Gevers");
		TEACHERS.put("Gro", "Groothius");
		TEACHERS.put("Grü", "Grüschow");
		TEACHERS.put("Hah", "Hahn");
		TEACHERS.put("Hai", "Haiduck");
		TEACHERS.put("Han", "Hain");
		TEACHERS.put("Ham", "Hamann");
		TEACHERS.put("Haf", "Harf");
		TEACHERS.put("Hsh", "Heilshorn");
		TEACHERS.put("Hsm", "Hesemann");
		TEACHERS.put("Hle", "Hille");
		TEACHERS.put("Hub", "Hubig");
		TEACHERS.put("Ktv", "Kalitovics");
		TEACHERS.put("Kau", "Kaufhold");
		TEACHERS.put("Kli", "Klingbeil");
		TEACHERS.put("Kön", "König");
		TEACHERS.put("Küt", "Küthmann");
		TEACHERS.put("Lan", "Lange");
		TEACHERS.put("Leh", "Lehmann");
		TEACHERS.put("Leo", "Leo");
		TEACHERS.put("Mmr", "Marxmeier");
		TEACHERS.put("Mey", "Meyer");
		TEACHERS.put("Mlr", "Marielle Müller");
		TEACHERS.put("Mür", "Verena Müller");
		TEACHERS.put("Orl", "Orlik");
		TEACHERS.put("Qui", "Quiring");
		TEACHERS.put("Red", "Rediske");
		TEACHERS.put("Rem", "Reimer");
		TEACHERS.put("RK", "Riemann-Kurtz");
		TEACHERS.put("San", "Sander");
		TEACHERS.put("Shb", "Scheibe");
		TEACHERS.put("Sla", "Schlachter");
		TEACHERS.put("Sct", "Schulte");
		TEACHERS.put("Shw", "Schwarze");
		TEACHERS.put("Spi", "Spiecker");
		TEACHERS.put("Ste", "Steinberg");
		TEACHERS.put("Tlb", "Thielebein");
		TEACHERS.put("Tre", "Treichel");
		TEACHERS.put("Les", "van Lessen");
		TEACHERS.put("Vig", "Vignais");
		TEACHERS.put("Wlb", "Wallbach");
		TEACHERS.put("Wes", "Wessels");
		TEACHERS.put("Wnh", "Windhorst");
		TEACHERS.put("Wit", "Witmeier");
		TEACHERS.put("Xan", "Xanthopoulos");
		TEACHERS.put("Zha", "Zhang");
	}
	
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
		setSupportActionBar((Toolbar) this.findViewById(R.id.toolbar));
		
		final SwipeRefreshLayout.OnRefreshListener refreshListener = new SwipeRefreshLayout.OnRefreshListener()
		{
			@Override
			public void onRefresh()
			{
				update();
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
						swipeLayout_today.setColorSchemeResources(R.color.accent);
						swipeLayout_today.setOnRefreshListener(refreshListener);
					}
					else
					{
						layout_tomorrow = (LinearLayout) layout.findViewById(R.id.layout_cards);
						
						swipeLayout_tomorrow = (SwipeRefreshLayout)layout.findViewById(R.id.layout_swipe);
						swipeLayout_tomorrow.setColorSchemeResources(R.color.accent);
						swipeLayout_tomorrow.setOnRefreshListener(refreshListener);
					}
					
					if (swipeLayout_today != null && swipeLayout_tomorrow != null)
					{
						swipeLayout_today.setRefreshing(true);
						swipeLayout_tomorrow.setRefreshing(false);
						update();
					}
					
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
					return getString(TABS[position]);
				}
				
		});
		
		if (Calendar.getInstance().get(Calendar.HOUR_OF_DAY) > 16)
		{
			viewPager.setCurrentItem(1);
		}
		
		tabLayout = (TabLayout) this.findViewById(R.id.sliding_tabs);
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
				Intent intent = new Intent().setClass(this, SettingsActivity.class);
				this.startActivityForResult( intent, 0 );
				return true;
			
			case R.id.about:
				
				final AlertDialog alertDialog = new AlertDialog.Builder(this, R.style.AlertDialogTheme).create();
				alertDialog.setTitle(R.string.about);
				alertDialog.setMessage(getString(R.string.about_app));
				alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK", new AlertDialog.OnClickListener()
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
				NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
				
				Notification.Builder notification = new Notification.Builder(this);
				
				notification.setContentTitle("Test");
				notification.setContentText("test");
				notification.setColor(getResources().getColor(R.color.primary));
				notification.setSmallIcon(R.drawable.ic_launcher);
				
				nm.notify(R.string.notification_id, notification.build());
				
				
				return true;
				
				
			default:
				return false;
		}
	}
	
	public boolean hasInternetConnection()
	{
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		
		NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
		return networkInfo != null && networkInfo.isConnected();
	}
	
	public void update()
	{
		swipeLayout_today.setRefreshing(true);
		swipeLayout_tomorrow.setRefreshing(true);
		
		if (!hasInternetConnection())
		{
			Toast.makeText(this, R.string.connection_error, Toast.LENGTH_SHORT).show();
			
			layout_today.removeAllViews();
			
			CardView card = new CardView(this);
			card.setClickable(true);

			TypedValue outValue = new TypedValue();
			getTheme().resolveAttribute(android.R.attr.selectableItemBackground, outValue, true);

			card.setForeground(getResources().getDrawable(outValue.resourceId, getTheme()));

			float dp = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, getResources().getDisplayMetrics());

			card.setCardElevation(dp / 4F);
			card.setUseCompatPadding(true);
			card.setPadding((int) dp, (int) dp, (int) dp, (int) dp);

			TextView text = new TextView(this);
			text.setTextColor(getResources().getColor(R.color.accent));
			text.setPadding((int) dp, (int) dp, (int) dp, (int) dp);
			text.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
			text.setText(Html.fromHtml("<b>" + getString(R.string.connection_error) + "</b>"));
			text.setGravity(Gravity.CENTER);

			card.addView(text);
			layout_today.addView(card);

			ArrayList<View> old = updateCards("today_");
			for (View v : old)
				layout_today.addView(v);

			layout_tomorrow.removeAllViews();
				
			card = new CardView(this);
			card.setClickable(true);
			card.setForeground(getResources().getDrawable(outValue.resourceId, getTheme()));
			card.setCardElevation(dp / 4F);
			card.setUseCompatPadding(true);
			card.setPadding((int) dp, (int) dp, (int) dp, (int) dp);

			text = new TextView(this);
			text.setTextColor(getResources().getColor(R.color.accent));
			text.setPadding((int) dp, (int) dp, (int) dp, (int) dp);
			text.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
			text.setText(Html.fromHtml("<b>" + getString(R.string.connection_error) + "</b>"));
			text.setGravity(Gravity.CENTER);

			card.addView(text);
			layout_tomorrow.addView(card);

			old = updateCards("tomorrow_");
			for (View v : old)
				layout_tomorrow.addView(v);
	
			swipeLayout_today.setRefreshing(false);
			swipeLayout_tomorrow.setRefreshing(false);
			return;
		}
		
		try
		{
			new AsyncTask<String, Void, ArrayList<View>>()
			{
				@Override
				protected ArrayList<View> doInBackground(String... urls)
				{
					try
					{
						updateInfo(urls[0], "today_");
						return updateCards("today_");
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
					{
						layout_today.addView(v);
					}
				
					swipeLayout_today.setRefreshing(false);
				}
			}.execute(URL_TODAY);
			
			new AsyncTask<String, Void, ArrayList<View>>()
			{
				@Override
				protected ArrayList<View> doInBackground(String... urls)
				{
					try
					{
						updateInfo(urls[0], "tomorrow_");
						return updateCards("tomorrow_");
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
					{
						layout_tomorrow.addView(v);
					}

					swipeLayout_tomorrow.setRefreshing(false);
				}
			}.execute(URL_TOMORROW);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	private void updateInfo(String myurl, String prefix) throws IOException
	{
		
		InputStream is = null;

		try 
		{
			URL url = new URL(myurl);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_10_3) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/44.0.2403.155 Safari/537.36");
			conn.setRequestProperty("Accept-Charset", "ISO-8859-1");
			conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=iso-8859-1");
			conn.setReadTimeout(10000);
			conn.setConnectTimeout(15000);
			conn.setRequestMethod("GET");
			conn.setDoInput(true);
			conn.connect();
			
			is = conn.getInputStream();
			String contentAsString = readIt(is);
			
			Matcher m = Pattern.compile("<tr").matcher(contentAsString);
			
			int i = 0;
			int index = 0;
			
			while (m.find())
			{
				i++;
				
				if (i == 1)
				{
					String row = contentAsString.substring(m.start(), 4 + contentAsString.indexOf("/tr>", m.start()));

					Matcher m1 = Pattern.compile("<td").matcher(row);
					m1.find();
					
					int index1 = row.indexOf(">", m1.start());
					String column = row.substring(index1 + 1, row.indexOf("<", index1));
					
					PreferenceManager.getDefaultSharedPreferences(this).edit().putString(prefix + "title", column).commit();
				}
				if (i > 2)
					index = parseColumn(contentAsString.substring(m.start(), 4 + contentAsString.indexOf("/tr>", m.start())), index, prefix + index + "_");
			}
			
			PreferenceManager.getDefaultSharedPreferences(this).edit().putInt(prefix + "count", index).commit();
		}
		finally
		{
			if (is != null)
			{
				is.close();
			} 
		}
	}
	
	public String readIt(InputStream stream) throws IOException, UnsupportedEncodingException 
	{
		BufferedReader reader = null;
		reader = new BufferedReader(new InputStreamReader(stream, "ISO-8859-1"));        
		
		StringBuffer sb = new StringBuffer();
		
		String line;
		
		while((line = reader.readLine()) != null)
		{
			sb.append(line).append("\n");
		}
		
		return new String(sb);
	}
	
	public ArrayList<View> updateCards(String prefix)
	{
		final ArrayList<View> result = new ArrayList<>();
		
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
		
		float dp = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, getResources().getDisplayMetrics());

		TextView text = new TextView(this);
		text.setTextColor(getResources().getColor(R.color.accent));
		text.setPadding((int) dp / 2, (int) dp, (int) dp, (int) dp);
		text.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
		text.setText(Html.fromHtml("<b>" + preferences.getString(prefix + "title", "...") + "</b>"));

		result.add(text);
		
		int cards = preferences.getInt(prefix + "count", 0);
		
		for (int i = 0; i < cards; ++i)
			addCard(result, preferences.getString(prefix + i +  "_content", ""), preferences.getString(prefix + i +  "_title", ""));
		
		return result;
	}
	
	public int parseColumn(String row, int rowIndex, String prefix)
	{
		Matcher m = Pattern.compile("<td").matcher(row);

		int index;
		int i = 0;
		String column;
		
		String ROW = PreferenceManager.getDefaultSharedPreferences(this).getString("row", "");
		
		StringBuilder sb = new StringBuilder();
		String title = "";
		
		while (m.find())
		{
			++i;
			index = row.indexOf(">", m.start());
			column = row.substring(index + 1, row.indexOf("<", index)).replace("&nbsp;", "").replace("\n", "").replace("  ", " ");
			
			if (i == 1)
			{
				if (column.isEmpty() || !ROW.isEmpty() && !column.equalsIgnoreCase(ROW) && !column.contains(ROW))
				{
					return rowIndex;
				}
				
				title = column;
			}
			else if (i != 10 && !column.isEmpty())
			{
				if (sb.length() != 0)
					sb.append("<br />");
					
				for (String s : TEACHERS.keySet())
					column = column.replace(s, "<i>" + TEACHERS.get(s) + "</i>");
					
				if (column.equalsIgnoreCase("f"))
				{
					column = "<b>Ausfall</b>";
				}
				else if (column.equalsIgnoreCase("MP"))
				{
					column = "<b>Mittagspause</b>";
				}
				else if (column.equalsIgnoreCase("XXX"))
				{
					column = "Ausflug";
				}
				else if (column.contains("f.a.") || column.contains("Aufg"))
				{
					column = "<b>" + column.replace("f.a.", "Ausfall").replace("f.a", "Ausfall").replace("Aufg.", "Aufgaben") + "</b>";
				}
				else if (column.contains("siehe"))
				{
					column = PreferenceManager.getDefaultSharedPreferences(this).getString(prefix.substring(0, prefix.indexOf('_') + 1) + getPrefixForGrade(column.replace("siehe", "").trim()) + "content", column);
				}
				
				sb.append((i - 1) + ". ");
				sb.append(column);
			}
		}
		
		PreferenceManager.getDefaultSharedPreferences(this).edit()
		.putString(prefix + "title", title)
		.putString(prefix + "content", sb.toString())
		.commit();
		
		return ++rowIndex;
	}
	
	public String getPrefixForGrade(String grade)
	{
		if (grade.equals("5a"))
			return "0_";
			
		else if (grade.equals("5b"))
			return "1_";
			
		else if (grade.equals("5c"))
			return "2_";
			
		else if (grade.equals("6a"))
			return "3_";

		else if (grade.equals("6b"))
			return "4_";

		else if (grade.equals("6c"))
			return "5_";
			
		else if (grade.equals("7a"))
			return "6_";

		else if (grade.equals("7b"))
			return "7_";

		else if (grade.equals("7c"))
			return "8_";
			
		else if (grade.equals("8a"))
			return "9_";

		else if (grade.equals("8b"))
			return "10_";

		else if (grade.equals("8c"))
			return "11_";
			
		else if (grade.equals("9a"))
			return "12_";

		else if (grade.equals("9b"))
			return "13_";

		else if (grade.equals("9c"))
			return "14_";
			
		return "-1_";
	}
	
	public void addCard(ArrayList<View> result, String content, String title)
	{
		CardView card = new CardView(this);

		card.setClickable(true);

		TypedValue outValue = new TypedValue();
		getTheme().resolveAttribute(android.R.attr.selectableItemBackground, outValue, true);

		card.setForeground(getResources().getDrawable(outValue.resourceId, getTheme()));

		float dp = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, getResources().getDisplayMetrics());

		card.setCardElevation(dp / 4F);
		card.setUseCompatPadding(true);
		card.setPadding((int) dp, (int) dp, (int) dp, (int) dp);
		
		TextView text = new TextView(this);
		text.setTextColor(getResources().getColor(R.color.accent));
		text.setPadding((int) dp, (int) dp, (int) dp, (int) dp);
		text.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
		text.setText(Html.fromHtml("<b>" + title + "</b>"));

		card.addView(text);
		
		text = new TextView(this);
		text.setPadding((int) dp * 2, (int) (dp * 4F), (int) dp, (int) dp);
		text.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 18);
		text.setText(content.length() == 0 ? "Regulärer Unterricht" : Html.fromHtml(content));

		card.addView(text);

		result.add(card);
	}
}
