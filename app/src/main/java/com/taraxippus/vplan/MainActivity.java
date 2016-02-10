package com.taraxippus.vplan;

import android.content.*;
import android.net.*;
import android.os.*;
import android.support.design.widget.*;
import android.support.v4.view.*;
import android.support.v4.widget.*;
import android.support.v7.app.*;
import android.support.v7.widget.*;
import android.text.*;
import android.view.*;
import android.widget.*;
import java.io.*;
import java.net.*;
import java.util.regex.*;

import android.support.v7.widget.Toolbar;
import android.preference.*;
import java.util.*;
import android.util.*;
import android.graphics.drawable.*;

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
	
	public static final String[] TABS = new String[] {"HEUTE", "MORGEN"};
	
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
				setText();
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
						setText();
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
					return TABS[position];
				}
				
		});
		
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
	
	public void setText()
	{
		if (!hasInternetConnection())
		{
			Toast.makeText(this, "Keine Internetverbindung", Toast.LENGTH_SHORT).show();
			
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
						return downloadUrl(urls[0]);
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
						return downloadUrl(urls[0]);
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
	
	private ArrayList<View> downloadUrl(String myurl) throws IOException
	{
		final ArrayList<View> result = new ArrayList<>();
		
		InputStream is = null;

		try 
		{
			URL url = new URL(myurl);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setReadTimeout(10000);
			conn.setConnectTimeout(15000);
			conn.setRequestMethod("GET");
			conn.setDoInput(true);
			conn.connect();
			
			is = conn.getInputStream();
			String contentAsString = readIt(is);
			
			Matcher m = Pattern.compile("<tr").matcher(contentAsString);
			
			int i = 0;
			
			while (m.find())
			{
				i++;
				
				if (i > 2)
				{
					addColumns(result, contentAsString.substring(m.start(), 4 + contentAsString.indexOf("/tr>", m.start())));
				}
			}
			
			return result;
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
		reader = new BufferedReader(new InputStreamReader(stream, "UTF-8"));        
		
		StringBuffer sb = new StringBuffer();
		
		String line;
		
		while((line = reader.readLine()) != null)
		{
			sb.append(line).append("\n");
		}
		
		return new String(sb);
	}
	
	public void addColumns(ArrayList<View> result, String row)
	{
		Matcher m = Pattern.compile("<td").matcher(row);

		int index;
		int i = 0;
		String column;
		
		String ROW = PreferenceManager.getDefaultSharedPreferences(this).getString("row", "");
		
		CardView card = new CardView(this);
		
		card.setClickable(true);
		card.setForeground();
		
		float dp = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, getResources().getDisplayMetrics());
		
		card.setCardElevation(dp / 4F);
		card.setUseCompatPadding(true);
		
		StringBuilder sb = new StringBuilder();
		
		while (m.find())
		{
			++i;
			index = row.indexOf(">", m.start());
			column = row.substring(index + 1, row.indexOf("<", index)).replace("&nbsp;", "").replace("\n", "").replace("  ", " ");
			
			if (i == 1 && !ROW.isEmpty() && !column.equalsIgnoreCase(ROW))
			{
				return;
			}
			
			if (i == 1)
			{
				if (column.isEmpty())
				{
					return;
				}
				
				TextView text = new TextView(this);
				text.setTextColor(getResources().getColor(R.color.accent));
				text.setPadding((int) dp, (int) dp, (int) dp, (int) dp);
				text.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
				text.setText(Html.fromHtml("<b>" + column + "</b>"));

				card.addView(text);
			}
			else if (i != 10 && !column.isEmpty())
			{
				if (sb.length() != 0)
					sb.append("<br />");
					
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
				else if (column.contains("f.a.") || column.contains("Aufg."))
				{
					column = "<b>" + column + "</b>";
				}
				
				sb.append((i - 1) + ". ");
				sb.append(column);
			}
		}
		
		TextView text = new TextView(this);
		text.setPadding((int) dp * 2, (int) (dp * 4F), (int) dp, (int) dp);
		text.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 18);
		text.setText(sb.length() == 0 ? "Regulärer Untericht" : Html.fromHtml(sb.toString()));
		
		card.addView(text);
		
		result.add(card);
	}
}
