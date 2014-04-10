package com.oreilly.nasadailyimage;

import java.io.IOException;

import android.os.Bundle;
import android.os.Handler;
import android.app.Activity;
import android.app.ProgressDialog;
import android.app.WallpaperManager;
import android.graphics.Bitmap;
import android.view.Menu;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

	IotdHandler iotdHandler;
	Handler handler;
	ProgressDialog dialog;
	Bitmap image;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		handler =  new Handler();		
		refreshFromFeed();
		
	}

	private void refreshFromFeed() {
		
		dialog = ProgressDialog.show(this, "Loading", "Loading the Image of the Day");
		
		Thread th = new Thread(){
			@Override
			public void run()
			{
				if (iotdHandler== null){
						iotdHandler = new IotdHandler(getApplicationContext());}				
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
			
		
		
		
		
	}
	
	public void onRefresh(View view){
		refreshFromFeed();
	}
	
	public void onSetWallpaper(View view) {
		Thread th =  new Thread(){
			@Override
			public void run() {
				WallpaperManager wpm = WallpaperManager.getInstance(MainActivity.this);
				
				try {
					wpm.setBitmap(image);
					
					handler.post(new Runnable() {
						
						@Override
						public void run() {
							Toast.makeText(MainActivity.this, "Wallpaper Set", Toast.LENGTH_SHORT).show();
							
						}
					});
					
				} catch (IOException e) {					
					e.printStackTrace();
					handler.post(new Runnable() {
						
						@Override
						public void run() {
							Toast.makeText(MainActivity.this, "Error setting Wallpaper", Toast.LENGTH_SHORT).show();							
						}
					});
				}
				
			}
		};
		
		th.start();
	}
	

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	private void resetDisplay(String title, String date, Bitmap image, String description)	{
		TextView titleView = (TextView) findViewById(R.id.imageTitle);
		titleView.setText(title);
		
		TextView dateView = (TextView) findViewById(R.id.imageDate);
		dateView.setText(date);
		
		ImageView imageView = (ImageView) findViewById(R.id.imageDisplay);
		imageView.setImageBitmap(image);
		
		TextView descriptionView = (TextView) findViewById(R.id.imageDescription);
		descriptionView.setText(description);		
		
	}

}
