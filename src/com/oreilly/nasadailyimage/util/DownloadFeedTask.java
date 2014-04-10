package com.oreilly.nasadailyimage.util;

import com.oreilly.nasadailyimage.IotdHandler;
import com.oreilly.nasadailyimage.R;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;


public class DownloadFeedTask extends AsyncTask<IotdHandler, Void, IotdHandler> {

	private FragmentActivity container;
	private ProgressBar prgBar;
	private OnBackgroundTaskListener caller;
	
	public DownloadFeedTask(FragmentActivity container) {
		this.container = container;
		prgBar = (ProgressBar) this.container.findViewById(R.id.prgBar);
	}
	
	
	@Override
	protected void onPreExecute() {		
		prgBar.setVisibility(View.VISIBLE);
	};
	
	@Override
	protected IotdHandler doInBackground(IotdHandler... params) {
				
		if (params.length==0) return null;
		
		IotdHandler iotdHandler;		
		iotdHandler = params[0];		
		iotdHandler.processFeed();
		
		return iotdHandler;
	}
	
	@Override	
	protected void onPostExecute(IotdHandler result) {
		prgBar.setVisibility(View.GONE);
		
		resetDisplay(result.getTitle(), result.getDate(), result.getImage(), result.getDescription().toString());
		
		if (caller!=null) 
			caller.OnCompleted(Integer.valueOf(result.getIndex()));
		
	}
	
	private void resetDisplay(String title, String date, Bitmap image, String description)	{
		TextView titleView = (TextView) container.findViewById(R.id.imageTitle);
		titleView.setText(title);
		
		TextView dateView = (TextView) container.findViewById(R.id.imageDate);
		dateView.setText(date);
		
		ImageView imageView = (ImageView) container.findViewById(R.id.imageDisplay);
		imageView.setImageBitmap(image);
		
		TextView descriptionView = (TextView) container.findViewById(R.id.imageDescription);
		descriptionView.setText(description);
		
	}
	
	public void setOnBackgroundTaskListener(OnBackgroundTaskListener listener){
		caller = listener;
	}
	
	

}
