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

import com.facebook.*;
import com.facebook.widget.FacebookDialog;
import com.facebook.widget.FacebookDialog.PendingCall;
import com.facebook.widget.WebDialog;
import com.facebook.widget.WebDialog.OnCompleteListener;

public class NasaIotd extends Fragment implements OnBackgroundTaskListener {

	private static final String IOTD_STATE = "state";
	private static final String IOTD_TITLE = "title";
	private static final String IOTD_DESCRIPTION = "description";
	private static final String IOTD_DATE = "date";
	private static final String IOTD_IMAGEURL = "image";
	private static final String IOTD_INDEX = "index";
	private static final String TAG = "NasaIotd";
	
	// FACEBOOK SETUP
	private UiLifecycleHelper uiHelper;
	SessionStatusCallback statusCallback = new SessionStatusCallback();



	private class SessionStatusCallback implements Session.StatusCallback {
		@Override
		public void call(Session session, SessionState state, Exception exception) {
			if (exception != null) {
				//handleException(exception);
				Toast.makeText(getActivity(), "Session Error!!", Toast.LENGTH_SHORT).show();
			}
			if (state.isOpened()) {
				//afterLogin();
				Toast.makeText(getActivity(), "Session Opened!", Toast.LENGTH_SHORT).show();
				publishFeedDialog();
			} else if (state.isClosed()) {
				//afterLogout();
				Toast.makeText(getActivity(), "Session Closed!", Toast.LENGTH_SHORT).show();
			}
		}
	}

	public void login() {
		Session session = Session.getActiveSession();
		if (!session.isOpened() && !session.isClosed()) {
			session.openForRead(new Session.OpenRequest(this).setCallback(statusCallback));
		} else {
			Session.openActiveSession(getActivity(), this, true, statusCallback);
		}
	}
	
		
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
		
		/* Facebook inicialization */
		uiHelper = new UiLifecycleHelper(getActivity(), null);
	    uiHelper.onCreate(savedInstanceState);
		
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_nasa_iotd, container,false);
		
		view.setOnTouchListener(new OnSwipeTouchListener(getActivity()) {
			public void onSwipeTop() {
				Toast.makeText(getActivity(), "top", Toast.LENGTH_SHORT)
						.show();
			}

			public void onSwipeRight() {
				Toast.makeText(getActivity(), "right", Toast.LENGTH_SHORT).show();
								
			}

			public void onSwipeLeft() {
				Toast.makeText(getActivity(), "left", Toast.LENGTH_SHORT).show();
				
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
	
	//FACEBOOK SETUP
	/** Configure a callback handler that's invoked when the share dialog closes and control returns to the calling app
	 * 
	 */
	@Override
	public void onActivityResult(int requestCode, int resultCode, android.content.Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		
		uiHelper.onActivityResult(requestCode, resultCode, data, new FacebookDialog.Callback() {
			
			@Override
			public void onError(PendingCall pendingCall, Exception error, Bundle data) {
				Log.e("Activity", String.format("Error: %s", error.toString()));
				
			}
			
			@SuppressWarnings("unused")
			@Override
			public void onComplete(PendingCall pendingCall, Bundle data) {
				Log.i("Activity", "Success!");
				boolean didCancel = FacebookDialog.getNativeDialogDidComplete(data);
				String completionGesture = FacebookDialog.getNativeDialogCompletionGesture(data);
				String postId = FacebookDialog.getNativeDialogPostId(data);
				
			}
		});
		
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
		if (FacebookDialog.canPresentShareDialog(getActivity().getApplicationContext(), 
                FacebookDialog.ShareDialogFeature.SHARE_DIALOG)) {
			// Publish the post using the Share Dialog
			FacebookDialog shareDialog = new FacebookDialog.ShareDialogBuilder(getActivity())
			.setName(iotdHandler.getTitle())
			.setLink(iotdHandler.getLink())	        	        
	        .build();
			uiHelper.trackPendingDialogCall(shareDialog.present());
		}
		else
		{
			// Fallback. For example, publish the post using the Feed Dialog
			login();			
		}
		
	}
	
	
	/** To define a new method that invokes the Feed Dialog for the Facebook instance
	 * 
	 */
	private void publishFeedDialog() {
	    Bundle params = new Bundle();
	    params.putString("name", iotdHandler.getTitle());
	    //params.putString("caption", "Build great social apps and get more installs.");
	    //params.putString("description", "The Facebook SDK for Android makes it easier and faster to develop Facebook integrated Android apps.");
	    params.putString("link", iotdHandler.getLink());
	    //params.putString("picture", "https://raw.github.com/fbsamples/ios-3.x-howtos/master/Images/iossdk_logo.png");

	    WebDialog feedDialog = (
	        new WebDialog.FeedDialogBuilder(getActivity(),
	            Session.getActiveSession(),
	            params))
	        .setOnCompleteListener(new OnCompleteListener() {

	            @Override
	            public void onComplete(Bundle values, FacebookException error) {
	                if (error == null) {
	                    // When the story is posted, echo the success
	                    // and the post Id.
	                    final String postId = values.getString("post_id");
	                    if (postId != null) {
	                        Toast.makeText(getActivity(),
	                            "Posted story, " + iotdHandler.getTitle(),
	                            Toast.LENGTH_SHORT).show();
	                    } else {
	                        // User clicked the Cancel button
	                        Toast.makeText(getActivity().getApplicationContext(), 
	                            "Publish cancelled", 
	                            Toast.LENGTH_SHORT).show();
	                    }
	                } else if (error instanceof FacebookOperationCanceledException) {
	                    // User clicked the "x" button
	                    Toast.makeText(getActivity().getApplicationContext(), 
	                        "Publish cancelled", 
	                        Toast.LENGTH_SHORT).show();
	                } else {
	                    // Generic, ex: network error
	                    Toast.makeText(getActivity().getApplicationContext(), 
	                        "Error posting story", 
	                        Toast.LENGTH_SHORT).show();
	                }
	            }

	        })
	        .build();
	    feedDialog.show();
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
		//FACEBOOK SETUP		
		uiHelper.onSaveInstanceState(outState); 
		
		
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
	
	//FACEBOOK SETUP
	@Override	
	public void onResume() {
		super.onResume();
		uiHelper.onResume();
	}
	
	@Override
	public void onPause() {
		super.onPause();
		uiHelper.onPause();		
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		uiHelper.onDestroy();
	}
	



}
