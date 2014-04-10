package com.oreilly.nasadailyimage;

import java.io.IOException;

import android.os.AsyncTask.Status;
import android.os.Bundle;
import android.os.Handler;

import android.support.v4.app.Fragment;
import android.app.ProgressDialog;
import android.app.WallpaperManager;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.LayoutInflater;

import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.oreilly.nasadailyimage.R;
import com.oreilly.nasadailyimage.util.DownloadFeedTask;
import com.oreilly.nasadailyimage.util.ImageFileCache;
import com.oreilly.nasadailyimage.util.OnBackgroundTaskListener;
import com.oreilly.nasadailyimage.util.OnSwipeTouchListener;

public class NasaIotd extends Fragment implements OnBackgroundTaskListener {

	private static final String IOTD_STATE = "state";
	private static final String IOTD_TITLE = "title";
	private static final String IOTD_DESCRIPTION = "description";
	private static final String IOTD_DATE = "date";
	private static final String IOTD_IMAGEURL = "image";
	private static final String IOTD_INDEX = "index";
	private static final String TAG = "NasaIotd";
	
	Bundle iotdBundle;
	IotdHandler iotdHandler;	
	Handler handler;
	ProgressDialog dialog;
	Bitmap image;
	DownloadFeedTask download;
	int Index=0;
	
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);		
		handler =  new Handler();
		setHasOptionsMenu(true);
		
		if(savedInstanceState!= null)
		{
			iotdBundle = savedInstanceState.getBundle(IOTD_STATE);
			Index = iotdBundle.getInt(IOTD_INDEX);
		}
		
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_nasa_iotd, container,false);
		
		view.setOnTouchListener(new OnSwipeTouchListener(getActivity()) {
			public void onSwipeTop() {
				Toast.makeText(getActivity(), "top", Toast.LENGTH_SHORT).show();
			}

			public void onSwipeRight() {
				Toast.makeText(getActivity(), "right", Toast.LENGTH_SHORT)
						.show();
			}

			public void onSwipeLeft() {
				Toast.makeText(getActivity(), "left", Toast.LENGTH_SHORT)
						.show();
			}

			public void onSwipeBottom() {
				Toast.makeText(getActivity(), "bottom", Toast.LENGTH_SHORT)
						.show();
			}

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				return getGestureDetector().onTouchEvent(event);
			}
		});
				
		return view;
	};

	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.nasa_iotd, menu);
		
	};
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
				
		switch(item.getItemId())
		{
		case R.id.action_refresh:
			refreshFromFeed();
			return true;
		case R.id.action_wallpaper:
			setWallPaper();
			return true;
		case R.id.action_share:
			shareIotd();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}	
	
	};
	
	
	@Override
	public void onStart() {
		super.onStart();
		
		if (iotdBundle!=null)
		{
			Index = iotdBundle.getInt(IOTD_INDEX);
			try{
				ImageFileCache imageCache =  new ImageFileCache(getActivity());
				resetDisplay(iotdBundle.getString(IOTD_TITLE),
								iotdBundle.getString(IOTD_DATE),
								imageCache.loadImage(iotdBundle.getString(IOTD_IMAGEURL)),
								iotdBundle.getString(IOTD_DESCRIPTION));
				return;
				
			}catch(Exception e){
				Log.e(TAG, "Error al restaurar la actividad");
				iotdBundle = null;
			}
			
			
		}
		
		refreshFromFeed();
	};
	
	private void refreshFromFeed() {
		
		if (iotdHandler== null){
			iotdHandler = new IotdHandler(getActivity());}				
	
		iotdHandler.setIndex(Index);
		
		if(download==null || download.getStatus()==Status.FINISHED)
		{
			download = new DownloadFeedTask(getActivity());
			download.setOnBackgroundTaskListener(this);
			download.execute(iotdHandler);
		}
		
		/*
		
		dialog = ProgressDialog.show(getActivity(), "Loading", "Loading the Image of the Day");
		
		Thread th = new Thread(){
			@Override
			public void run()
			{
				if (iotdHandler== null){
						iotdHandler = new IotdHandler(getActivity());}				
				iotdHandler.processFeed();
				
				handler.post(new Runnable() {
					
					@Override
					public void run() {
						
						resetDisplay(iotdHandler.getTitle(), iotdHandler.getDate(), iotdHandler.getImage(), iotdHandler.getDescription().toString());
						image = iotdHandler.getImage();
						dialog.dismiss();						
					}
				});
				
			}
		};
		
		th.start();
			
		*/
	}
	
	
	
	/*
	public void onRefresh(View view){
		refreshFromFeed();
	}
	
	public void onSetWallpaper(View view) {
		setWallPaper();
	}
	 */
	
	private void setWallPaper() {
		
		if(download!=null && download.getStatus()==Status.RUNNING) return;
		
		Thread th =  new Thread(){
			@Override
			public void run() {
				WallpaperManager wpm = WallpaperManager.getInstance(getActivity());
				
				try {
					wpm.setBitmap(iotdHandler.getImage());
					
					handler.post(new Runnable() {
						
						@Override
						public void run() {
							Toast.makeText(getActivity(), "Wallpaper Set", Toast.LENGTH_SHORT).show();
							
						}
					});
					
				} catch (IOException e) {					
					e.printStackTrace();
					handler.post(new Runnable() {
						
						@Override
						public void run() {
							Toast.makeText(getActivity(), "Error setting Wallpaper", Toast.LENGTH_SHORT).show();							
						}
					});
				}
				
			}
		};
		
		th.start();
	}
	

	private void shareIotd()
	{
		
	}
	
		
	@Override
	public void onSaveInstanceState(Bundle outState) {
		if (iotdHandler!= null)
		{
			iotdBundle = new Bundle();
			iotdBundle.putString(IOTD_TITLE, iotdHandler.getTitle());
			iotdBundle.putString(IOTD_DESCRIPTION, iotdHandler.getDescription().toString());
			iotdBundle.putString(IOTD_DATE, iotdHandler.getDate());
			iotdBundle.putString(IOTD_IMAGEURL, iotdHandler.getImageUrl());
			iotdBundle.putInt(IOTD_INDEX, iotdHandler.getIndex());			
			
			outState.putBundle(IOTD_STATE, iotdBundle);
		} else
		{
			if (iotdBundle!=null)
			{
				outState.putBundle(IOTD_STATE, iotdBundle);
			}
		}
		
		
		
		super.onSaveInstanceState(outState);
		
	}
	
	
	
	private void resetDisplay(String title, String date, Bitmap image, String description)	{
		TextView titleView = (TextView) getActivity().findViewById(R.id.imageTitle);
		titleView.setText(title);
		
		TextView dateView = (TextView) getActivity().findViewById(R.id.imageDate);
		dateView.setText(date);
		
		ImageView imageView = (ImageView) getActivity().findViewById(R.id.imageDisplay);
		imageView.setImageBitmap(image);
		
		TextView descriptionView = (TextView) getActivity().findViewById(R.id.imageDescription);
		descriptionView.setText(description);

		
	}

	@Override
	public void OnCompleted(Object result) {
		
		if (result != null)
			Index = ((Integer)result).intValue();
		
	}



}
