package com.oreilly.nasadailyimage.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;


public class ImageFileCache {
	private File cacheDir;
	
	public ImageFileCache(Context context){
		cacheDir = context.getCacheDir();		
	}
	
	public void saveImage(String url, Bitmap image) throws IOException{
		
			File cacheImage =  new File(cacheDir,getFileNameFromUrl(url)+".jpg");
			FileOutputStream fOut = new FileOutputStream(cacheImage);
			
			image.compress(Bitmap.CompressFormat.JPEG, 100, fOut);
			fOut.flush();
			fOut.close();				
			
	}
	
	
	public Bitmap loadImage(String url){
		Bitmap bitmap = BitmapFactory.decodeFile(cacheDir+"/"+getFileNameFromUrl(url)+".jpg");
		return bitmap;
	}
	
	
	public boolean existsImageFile(String url){
		File cacheImage =  new File(cacheDir,getFileNameFromUrl(url)+".jpg");
		return cacheImage.exists();
	}
	
	private String getFileNameFromUrl(String url)
	{
		Pattern p = Pattern.compile("itok=(.*)");
		Matcher m = p.matcher(url);
		String name = null;
		
		while (m.find()){
			name = m.group(1);
		}
				
		return name;
				
	}
	
	
	
	
}
