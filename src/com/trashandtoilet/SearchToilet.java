package com.trashandtoilet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIUtils;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RadioGroup.LayoutParams;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.trashandtoilet.dto.Component;
import com.trashandtoilet.service.CustomizedAdapter;
import com.trashandtoilet.service.GPSTracker;
import com.trashandtoilet.windowadapter.TestingParsing;

@SuppressLint("NewApi")
public class SearchToilet extends FragmentActivity   /*
													 * implements'
													 * OnMapClickListener,
													 * OnMapLongClickListener,
													 * OnCameraChangeListener
													 */{
	static final LatLng SecondToilet = new LatLng(17.447729806707645,
			78.3633230254054);
	static final LatLng FirstToilet = new LatLng(17.4438208734482,
			78.36638074368238);

	private GoogleMap map;
	private static ArrayList<Component> toilets = new ArrayList<Component>();
	private static ArrayList<Component> trashcans = new ArrayList<Component>();
	private static double longitude;
	private static double latitude;
	private Handler progressBarHandler = new Handler();
	private int progressBarStatus;
	private  ProgressDialog progressBar;
	public GoogleMap getMap() {
		return map;
	}

	public void setMap(GoogleMap map) {
		this.map = map;
	}

	public ArrayList<Component> getToilets() {
		return toilets;
	}

	public void setToilets(ArrayList<Component> toilets) {
		SearchToilet.toilets = toilets;
	}

	@SuppressLint("NewApi")
	protected void onCreate(Bundle savedInstanceState) {
		System.out.println("Starting Search");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_search_toilet);
		GPSTracker gps = new GPSTracker(SearchToilet.this);
		// check if GPS enabled
		if (gps.canGetLocation()) {

			latitude = gps.getLatitude();
			longitude = gps.getLongitude();
			CameraPosition cLocation = new CameraPosition.Builder()
					.target(new LatLng(latitude, longitude)).zoom(12.5f)
					.bearing(300).tilt(50).build();

			map = ((SupportMapFragment) getSupportFragmentManager()
					.findFragmentById(R.id.map)).getMap();

			map.moveCamera(CameraUpdateFactory.newCameraPosition(cLocation));
			map.addMarker(new MarkerOptions()
					.position(new LatLng(latitude, longitude))
					.title("You")
					.icon(BitmapDescriptorFactory
							.fromResource(R.drawable.icon_locator)));
			  progressBar = new ProgressDialog(this);
			progressBar.setCancelable(true);
			progressBar.setMessage("Loading Toilets & TrashCans");
			progressBar.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			progressBar.setProgress(0);
			progressBar.setMax(100);
			progressBar.show();
			progressBarHandler.post(new Runnable() {
				public void run() {
				  progressBar.setProgress(progressBarStatus);
				}
			  });
			if (toilets.size() > 0 || trashcans.size() > 0) {
				progressBar.dismiss();
				for (Iterator iterator = toilets.iterator(); iterator.hasNext();) {
					Component toilet = (Component) iterator.next();
					map.addMarker(new MarkerOptions()
							.position(
									new LatLng(toilet.getLatitude(), toilet
											.getLongitude()))
							.title(toilet.getName())
							.icon(BitmapDescriptorFactory
									.fromResource(R.drawable.icon_toilet_marker)));
				}
				for (Iterator iterator = trashcans.iterator(); iterator
						.hasNext();) {
					Component toilet = (Component) iterator.next();
					map.addMarker(new MarkerOptions()
							.position(
									new LatLng(toilet.getLatitude(), toilet
											.getLongitude()))
							.title(toilet.getName())
							.icon(BitmapDescriptorFactory
									.fromResource(R.drawable.icon_dustbin_marker)));
				}

			} else {
				addToiletsAndDustbins(String.valueOf(latitude),
						String.valueOf(longitude), map);
			}
			/*
			 * map.setOnMapClickListener(this);
			 * map.setOnMapLongClickListener(this);
			 * map.setOnCameraChangeListener(this);
			 */
			// \n is for new line
		} else {
			// can't get location
			// GPS or Network is not enabled
			// Ask user to enable GPS/network in settings
			gps.showSettingsAlert();
		}

	}

	private void addToiletsAndDustbins(String lat, String lag, GoogleMap map) {
		new GetToilets(GlobalConstants.ONLY_TOILETS).execute(getUrl(lat, lag,
				GlobalConstants.ONLY_TOILETS));
		new GetToilets(GlobalConstants.ONLY_TRASH).execute(getUrl(lat, lag,
				GlobalConstants.ONLY_TRASH));

	}

	private URI getUrl(String _latitude, String _longitude, String _searchKey) {
		String _location = _latitude + "," + _longitude;
		List<NameValuePair> qparams = new ArrayList<NameValuePair>();
		qparams.add(new BasicNameValuePair("key", GlobalConstants.APIKey));
		qparams.add(new BasicNameValuePair("location", _location));
		qparams.add(new BasicNameValuePair("sensor", "true"));
		qparams.add(new BasicNameValuePair("name", _searchKey));
		qparams.add(new BasicNameValuePair("radius", GlobalConstants.RADIUS));

		URI uri = null;
		try {
			uri = URIUtils.createURI("https", "maps.googleapis.com", -1,
					"/maps/api/place/nearbysearch/json",
					URLEncodedUtils.format(qparams, "UTF-8"), null);
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return uri;

	}

	/*
	 * @Override public void onMapClick(LatLng point) {
	 * System.out.println("tapped, point=" + point); }
	 * 
	 * @Override public void onMapLongClick(LatLng point) { count++;
	 * System.out.println(count+" long pressed, point=" + point); }
	 * 
	 * @Override public void onCameraChange(final CameraPosition position) {
	 * System.out.println(position.toString()); } public static int count=0;
	 */

	public List<Component> getDustbins() {
		return trashcans;
	}

	public void setDustbins(ArrayList<Component> trashcans) {
		SearchToilet.trashcans = trashcans;
	}

	private class GetToilets extends AsyncTask<URI, Integer, String> {
		private String mode;

		public GetToilets(String mode) {
			this.mode = mode;
			// TODO Auto-generated constructor stub
		}

		@SuppressWarnings("unused")
		public GetToilets() {
			// TODO Auto-generated constructor stub
		}
		@Override
	     protected void onProgressUpdate(Integer... progress) {
	         setProgressPercent(progress[0]);
	     }


		private void setProgressPercent(Integer integer) {
			progressBarStatus=integer;		
		}

		@Override
		protected String doInBackground(URI... params) {
			HttpClient httpclient = new DefaultHttpClient();
			JSONObject finalResult = null;
			HttpGet httpget = new HttpGet(params[0]);
			try {
				HttpResponse response = httpclient.execute(httpget);
				BufferedReader reader = new BufferedReader(
						new InputStreamReader(
								response.getEntity().getContent(), "UTF-8"));
				StringBuilder builder = new StringBuilder();
				for (String line = null; (line = reader.readLine()) != null;) {
					builder.append(line).append("\n");
				}
				finalResult = new JSONObject(builder.toString());
				if (mode.equals(GlobalConstants.ONLY_TOILETS)) {
					toilets = new TestingParsing().parseJSONObject(finalResult,mode);
				} else if (mode.equals(GlobalConstants.ONLY_TRASH)) {
					setDustbins(new TestingParsing()
							.parseJSONObject(finalResult,mode));

				}

				System.out.println(finalResult);
			} catch (ClientProtocolException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
			}
			return null;
		}

		@Override
		protected void onPostExecute(String result) {
			progressBar.dismiss();

			if (mode.equals(GlobalConstants.ONLY_TOILETS)) {
				for (Iterator iterator = toilets.iterator(); iterator.hasNext();) {
					Component toilet = (Component) iterator.next();
					map.addMarker(new MarkerOptions()
							.position(
									new LatLng(toilet.getLatitude(), toilet
											.getLongitude()))
							.title(toilet.getName())
							.icon(BitmapDescriptorFactory
									.fromResource(R.drawable.icon_toilet_marker)));
				}
			} else if (mode.equals(GlobalConstants.ONLY_TRASH)) {
				for (Iterator iterator = trashcans.iterator(); iterator
						.hasNext();) {
					Component toilet = (Component) iterator.next();
					map.addMarker(new MarkerOptions()
							.position(
									new LatLng(toilet.getLatitude(), toilet
											.getLongitude()))
							.title(toilet.getName())
							.icon(BitmapDescriptorFactory
									.fromResource(R.drawable.icon_dustbin_marker)));
				}
			}

		}

		@SuppressWarnings("unused")
		public String getMode() {
			return mode;
		}

		@SuppressWarnings("unused")
		public void setMode(String mode) {
			this.mode = mode;
		}

	}

	public void onlyToilets(View view) {
		ImageView imageView=(ImageView) findViewById(R.id.imageView5);
		imageView.setImageResource(R.drawable.icon_toliets_only);
		imageView=(ImageView) findViewById(R.id.imageView4);
		imageView.setImageResource(R.drawable.icon_view_all_deselect);
		imageView=(ImageView) findViewById(R.id.imageView6);
		imageView.setImageResource(R.drawable.icon_dustbin_deselct);
		map.clear();
		if (toilets.size() > 0 && latitude != 0 && longitude != 0) {
			map.addMarker(new MarkerOptions()
					.position(new LatLng(latitude, longitude))
					.title("You")
					.icon(BitmapDescriptorFactory
							.fromResource(R.drawable.icon_locator)));
			for (Iterator iterator = toilets.iterator(); iterator.hasNext();) {
				Component toilet = (Component) iterator.next();
				map.addMarker(new MarkerOptions()
						.position(
								new LatLng(toilet.getLatitude(), toilet
										.getLongitude()))
						.title(toilet.getName())
						.icon(BitmapDescriptorFactory
								.fromResource(R.drawable.icon_toilet_marker)));
			}
		}
	}
	public void onlyTrashCans(View view) {
		ImageView imageView=(ImageView) findViewById(R.id.imageView6);
		imageView.setImageResource(R.drawable.icon_dustbin);
		imageView=(ImageView) findViewById(R.id.imageView4);
		imageView.setImageResource(R.drawable.icon_view_all_deselect);
		imageView=(ImageView) findViewById(R.id.imageView5);
		imageView.setImageResource(R.drawable.icon_toliets_only_deselct);
		map.clear();
		if (trashcans.size() > 0 && latitude != 0 && longitude != 0) {
			map.addMarker(new MarkerOptions()
					.position(new LatLng(latitude, longitude))
					.title("You")
					.icon(BitmapDescriptorFactory
							.fromResource(R.drawable.icon_locator)));
			for (Iterator iterator = trashcans.iterator(); iterator.hasNext();) {
				Component toilet = (Component) iterator.next();
				map.addMarker(new MarkerOptions()
						.position(
								new LatLng(toilet.getLatitude(), toilet
										.getLongitude()))
						.title(toilet.getName())
						.icon(BitmapDescriptorFactory
								.fromResource(R.drawable.icon_dustbin_marker)));
			}
		}
		
	}
	public void viewAll(View view) {
		ImageView imageView=(ImageView) findViewById(R.id.imageView4);
		imageView.setImageResource(R.drawable.icon_view_all);
		imageView=(ImageView) findViewById(R.id.imageView5);
		imageView.setImageResource(R.drawable.icon_toliets_only_deselct);
		imageView=(ImageView) findViewById(R.id.imageView6);
		imageView.setImageResource(R.drawable.icon_dustbin_deselct);
		map.clear();
		if ((toilets.size() >0 || trashcans.size() > 0) && latitude != 0 && longitude != 0) {
			map.addMarker(new MarkerOptions()
					.position(new LatLng(latitude, longitude))
					.title("You")
					.icon(BitmapDescriptorFactory
							.fromResource(R.drawable.icon_locator)));
			for (Iterator iterator = trashcans.iterator(); iterator.hasNext();) {
				Component toilet = (Component) iterator.next();
				map.addMarker(new MarkerOptions()
						.position(
								new LatLng(toilet.getLatitude(), toilet
										.getLongitude()))
						.title(toilet.getName())
						.icon(BitmapDescriptorFactory
								.fromResource(R.drawable.icon_dustbin_marker)));
			}
		}
		if (toilets.size() > 0 && latitude != 0 && longitude != 0) {
			for (Iterator iterator = toilets.iterator(); iterator.hasNext();) {
				Component toilet = (Component) iterator.next();
				map.addMarker(new MarkerOptions()
						.position(
								new LatLng(toilet.getLatitude(), toilet
										.getLongitude()))
						.title(toilet.getName())
						.icon(BitmapDescriptorFactory
								.fromResource(R.drawable.icon_toilet_marker)));
			}
		}
	}
	public void listView(View view){
	findViewById(R.id.map).setLayoutParams(new LayoutParams(0,0));;
	ListView listView=(ListView) findViewById(R.id.list1);
	listView.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.FILL_PARENT));
	
	ImageView imageView=(ImageView) findViewById(R.id.imageView3);
	imageView.setImageResource(R.drawable.icon_list_view);
	imageView=(ImageView) findViewById(R.id.imageView2);
	imageView.setImageResource(R.drawable.icon_map_view_notselect);
	
	ArrayList<Component> components=toilets;
	components.addAll(trashcans);
    CustomizedAdapter adapter=new CustomizedAdapter(this, R.layout.activity_list_view,toilets);
    listView.setAdapter(adapter);
	
	}
	public void mapView(View view){
		findViewById(R.id.list1).setLayoutParams(new LayoutParams(0,0));
		findViewById(R.id.map).setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.FILL_PARENT));;		
		ImageView imageView=(ImageView) findViewById(R.id.imageView2);
		imageView.setImageResource(R.drawable.icon_map_view);
		imageView=(ImageView) findViewById(R.id.imageView3);
		imageView.setImageResource(R.drawable.icon_list_view_deselect);
		}
}