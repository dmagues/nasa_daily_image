package com.oreilly.nasadailyimage;

import android.os.Bundle;

import android.support.v4.app.FragmentActivity;
import android.view.Menu;

public class NasaApp extends FragmentActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_nasa_app);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.nasa_app, menu);
		return true;
	}

}
