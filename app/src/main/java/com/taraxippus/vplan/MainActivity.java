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

public class MainActivity extends AppCompatActivity 
{
	TextView text_today;
	SwipeRefreshLayout swipeLayout_today;
	
	TextView text_tomorrow;
	SwipeRefreshLayout swipeLayout_tomorrow;
	
	ViewPager viewPager;
	TabLayout tabLayout;
	
	public static final String URL_TODAY = "http://306.joomla.schule.bremen.de/ServerSync/V-Plan-heute.htm";
	public static final String URL_TOMORROW = "http://306.joomla.schule.bremen.de/ServerSync/V-Plan-morgen.htm";
	
	public static final String ROW = "";
	
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
						text_today = (TextView) layout.findViewById(R.id.text);
						
						swipeLayout_today = (SwipeRefreshLayout)layout.findViewById(R.id.layout_swipe);
						swipeLayout_today.setColorSchemeResources(R.color.accent);
						swipeLayout_today.setOnRefreshListener(refreshListener);
					}
					else
					{
						text_tomorrow = (TextView) layout.findViewById(R.id.text);
						
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
			new AsyncTask<String, Void, String>()
			{
				@Override
				protected String doInBackground(String... urls)
				{
					try
					{
						return downloadUrl(urls[0]);
					}
					catch (Exception e)
					{
						e.printStackTrace();
						
						return "Error: " + e;
					}
				}
				
				@Override
				protected void onPostExecute(String result)
				{
					text_today.setText(Html.fromHtml(result));
					swipeLayout_today.setRefreshing(false);
				}
			}.execute(URL_TODAY);
			
			new AsyncTask<String, Void, String>()
			{
				@Override
				protected String doInBackground(String... urls)
				{
					try
					{
						return downloadUrl(urls[0]);
					}
					catch (Exception e)
					{
						e.printStackTrace();

						return "Error: " + e;
					}
				}

				@Override
				protected void onPostExecute(String result)
				{
					text_tomorrow.setText(Html.fromHtml(result));
					swipeLayout_tomorrow.setRefreshing(false);
				}
			}.execute(URL_TOMORROW);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	private String downloadUrl(String myurl) throws IOException
	{
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
			
			int response = conn.getResponseCode();
			
			
			
			is = conn.getInputStream();
			String contentAsString = readIt(is);
			
			StringBuilder sb = new StringBuilder();
			
			Matcher m = Pattern.compile("<tr").matcher(contentAsString);
			
			int i = 0;
			
			while (m.find())
			{
				i++;
				if (i > 2)
				{
					addColumns(sb, contentAsString.substring(m.start(), 4 + contentAsString.indexOf("/tr>", m.start())));
				}
			}
			
			return sb.toString();
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
	
	public void addColumns(StringBuilder sb, String row)
	{
		Matcher m = Pattern.compile("<td").matcher(row);

		int index;
		int i = 0;
		String column;
		
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
				
				sb.append(column);
				sb.append(":<br />");
			}
			else if (i != 10 && !column.isEmpty())
			{
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
				sb.append("<br />");
			}
		}
		
		sb.append("<br />");
	}
}
