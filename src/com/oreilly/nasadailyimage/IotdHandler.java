package com.oreilly.nasadailyimage;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import com.oreilly.nasadailyimage.util.ImageFileCache;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class IotdHandler extends DefaultHandler {
	private String url = "http://www.nasa.gov/rss/dyn/image_of_the_day.rss";		
	private boolean inTitle = false;
	private boolean inDescription = false;
	private boolean inItem = false;
	private boolean inDate = false;
	private boolean inEnclosure = false;
	private boolean inLink = false;
	private String currImageUrl = null;
	private String imageUrl = null;
	private Bitmap image = null;
	private String title = null;
	private StringBuffer description = null;
	private String date = null;
	private String link = null;
	
	private Context context = null;
	private int index = 0;
	private int currentIndex;
	
	
	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public IotdHandler(Context context){
		this.context = context;	
	}
	
	public void inicialize(){
		// se inicia con 10 siempre para empezar de la imagen mas nueva
		if (index==10) 
			index = 1; 
		else
			index++;
		
		currentIndex = 0;
		inItem = false;
		image = null;
		title = null;
		description = new StringBuffer();
		date = null;
		link =  null;
	}
	
	public void processFeed()  {
		
		inicialize();
		
		try {
			SAXParserFactory factory = SAXParserFactory.newInstance();
			
			SAXParser parser = factory.newSAXParser();
			XMLReader reader = parser.getXMLReader();
			reader.setContentHandler(this);
			
			InputStream inputStream = new URL(url).openStream();
			
			reader.parse(new InputSource(inputStream));
			
		} catch (Exception e ) {
			System.out.println(e.toString());
		}
		
		
	}
	
	private Bitmap getBitmap(String url) {
		try {
				ImageFileCache cache = new ImageFileCache(context);
				Bitmap bitmap = null;
				
				if (!cache.existsImageFile(url)){
					HttpURLConnection connection = (HttpURLConnection)new URL(url).openConnection();
					connection.setDoInput(true);
					connection.connect();
					InputStream input = connection.getInputStream();
					bitmap = BitmapFactory.decodeStream(input);
					cache.saveImage(url, bitmap);
					input.close();
				} else {
					bitmap = cache.loadImage(url);
				}
				
				return bitmap;
			} catch (IOException ioe) { return null; }
			
		
	}
	
	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		
		
		if (localName.startsWith("item")) { 
			inItem = true;
			currentIndex++;			
		}
		else if (inItem) {	
				 
				if (localName.equals("title")) { inTitle = true; }
				else { inTitle = false; }
				
				if (localName.equals("description")) { inDescription = true; }
				else { inDescription = false; }
				
				if (localName.equals("pubDate")) { inDate = true; }
				else { inDate = false; }
				
				if (localName.equals("link")) { inLink = true; }
				else { inLink = false; }
				
				if (localName.startsWith("enclosure")) 
				{ inEnclosure = true;
				  imageUrl =  attributes.getValue("","url");
				}
				else { inEnclosure = false; }
			}
		
	}
	
	@Override
	public void characters(char ch[], int start, int length) {
		String chars = new String(ch).substring(start, start + length);
		
		if(index==currentIndex){
			if (inEnclosure && image==null) { 
				image = getBitmap(imageUrl);
				currImageUrl = imageUrl;
				}
			if (inTitle && title == null) { title = chars; }
			if (inDescription) { description.append(chars); }
			if (inDate && date == null) { date = chars; }			
			if (inLink && link == null) { link = chars; }
		}	
		
		
	}
	
	
	
	
	public Bitmap getImage() { return image; }
	public String getImageUrl() { return currImageUrl; }
	public String getTitle() { return title; }	
	public StringBuffer getDescription() { return description; }
	public String getDate() { return date; }
	public String getLink() { return link; }
	
		
	
}
