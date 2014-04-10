package com.oreilly.nasadailyimage;

/**
 * NasaEdNews
 * This class is the central class for the fragment for the News Of The Day
 * 
 * Based on, but customised, based on the requirements of the book. 
 * 
 *  @author Geroen Joris - http://www.headfirstandroid.com/
 * 
 */

import java.net.URL;
import java.util.ArrayList;

import com.oreilly.nasadailyimage.EdNewsHandler.NewsItem;

import android.support.v4.app.ListFragment;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class NasaEdNews extends ListFragment {

	private static final String URL = "http://www.nasa.gov/rss/educationnews.rss";
	@SuppressWarnings("unused")
	private Handler handler;
	private ArrayList<NewsItem> list = new ArrayList<EdNewsHandler.NewsItem>();
	static private EdNewsAdapter listAdapter;

	public ArrayList<NewsItem> getValues() {
		return list;
	}

	public void setValues(ArrayList<NewsItem> values) {
		this.list = values;
	}

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		handler = new Handler();
	}

	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_ed_news, container,
				false);
		return view;
	}

	public void onStart() {
		super.onStart();
	}

	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		refreshFromFeed();
		listAdapter = new EdNewsAdapter(getActivity(), R.layout.ed_news_item,
				getValues());
		setListAdapter(listAdapter);

	}
	
	@Override
	public boolean onOptionsItemSelected(android.view.MenuItem item) {
		switch(item.getItemId())
		{
		case R.id.action_refresh:
			refreshFromFeed();
			return true;		
		default:
			return super.onOptionsItemSelected(item);
		}	
	};

	private void refreshFromFeed() {
		Thread th = new Thread(new Runnable() {
			public void run() {
				EdNewsHandler edNewsHandler = new EdNewsHandler();
				try {
					edNewsHandler.processFeed(getActivity(), new URL(URL));
					setValues(edNewsHandler.getNewsItemList());
					listAdapter.setNewsItemList(getValues());
					listAdapter.notifyDataSetChanged();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		th.start();

	}

}
