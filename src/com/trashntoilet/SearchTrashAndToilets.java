package com.trashntoilet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
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
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.GoogleMap.OnMapLongClickListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.trashntoilet.R;
import com.trashntoilet.dto.Component;
import com.trashntoilet.dto.ComponentComparatore;
import com.trashntoilet.service.CustomizedAdapter;
import com.trashntoilet.service.GPSTracker;
import com.trashntoilet.windowadapter.TestingParsing;

@SuppressLint("NewApi")
public class SearchTrashAndToilets extends FragmentActivity implements
		OnInfoWindowClickListener, OnMapLongClickListener/*
														 * implements'
														 * OnMapClickListener,
														 * OnMapLongClickListener
														 * ,
														 * OnCameraChangeListener
														 */{
	LatLng cLocation;
	public static String viewType = GlobalConstants.MAP_VIEW;
	public static String filterType = GlobalConstants.VIEW_ALL;
	public GoogleMap map;
	public static ArrayList<Component> toilets = new ArrayList<Component>();
	public static ArrayList<Component> trashcans = new ArrayList<Component>();
	public static double longitude;
	public static double latitude;
	public static float accuracy;
	private Handler progressBarHandler = new Handler();
	private int progressBarStatus;
	private ProgressDialog progressBar;
	public static String reportingType;
	public static String fromView;

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
		SearchTrashAndToilets.toilets = toilets;
	}

	@SuppressLint("NewApi")
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_search_toilet);
		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			fromView = extras.getString(GlobalConstants.FROM_VIEW);
			reportingType = extras.getString(GlobalConstants.REPORT_TYPE);
		}

		GPSTracker gps = new GPSTracker(SearchTrashAndToilets.this);
		// check if GPS enabled
		if (gps.canGetLocation()) {

			latitude = gps.getLatitude();
			longitude = gps.getLongitude();
			cLocation = new LatLng(latitude, longitude);
			CameraPosition cLocation = new CameraPosition.Builder()
					.target(new LatLng(latitude, longitude)).zoom(12.5f)
					.bearing(300).tilt(50).build();

			map = ((SupportMapFragment) getSupportFragmentManager()
					.findFragmentById(R.id.map)).getMap();
			map.moveCamera(CameraUpdateFactory.newCameraPosition(cLocation));
			if (gps.getLocation() != null)
				accuracy = gps.getLocation().getAccuracy();
			// Vaish
			if (!GlobalConstants.ADD_NEW.equals(fromView)
					&& !GlobalConstants.SUGGEST_NEW.equals(fromView)) {

				map.setOnInfoWindowClickListener(this);
				map.addMarker(new MarkerOptions()
						.position(new LatLng(latitude, longitude))
						.title("You")
						.icon(BitmapDescriptorFactory
								.fromResource(R.drawable.icon_locator)));
				if ((toilets.size() > 0 || trashcans.size() > 0)
						&& !GlobalConstants.ADD_OR_SUGGESTED) {
					for (Iterator iterator = toilets.iterator(); iterator
							.hasNext();) {
						Component toilet = (Component) iterator.next();
						map.addMarker(new MarkerOptions()
								.position(
										new LatLng(toilet.getLatitude(), toilet
												.getLongitude()))
								.title(toilet.getName())
								.icon(BitmapDescriptorFactory
										.fromResource(R.drawable.icons_toilet_marker)));
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
					progressBar = new ProgressDialog(this);
					progressBar.setMessage("Loading Toilets & TrashCans");
					progressBar.setCancelable(false);
					progressBar.setProgressStyle(ProgressDialog.STYLE_SPINNER);
					progressBar.setProgress(0);
					progressBar.setMax(100);
					progressBar.show();
					progressBarHandler.post(new Runnable() {
						public void run() {
							progressBar.setProgress(progressBarStatus);
						}
					});

					addToiletsAndDustbins(String.valueOf(latitude),
							String.valueOf(longitude), map);
				}
			} else {
				findViewById(R.id.imageView2).setLayoutParams(
						new LayoutParams(0, 0));
				findViewById(R.id.imageView3).setLayoutParams(
						new LayoutParams(0, 0));
				findViewById(R.id.imageView4).setLayoutParams(
						new LayoutParams(0, 0));
				findViewById(R.id.imageView5).setLayoutParams(
						new LayoutParams(0, 0));
				findViewById(R.id.imageView6).setLayoutParams(
						new LayoutParams(0, 0));
				findViewById(R.id.mapText).setLayoutParams(
						new LayoutParams(LayoutParams.FILL_PARENT,
								LayoutParams.FILL_PARENT));

				map.setOnMapLongClickListener(this);
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
		SearchTrashAndToilets.trashcans = trashcans;
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
			progressBarStatus = integer;
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
					toilets = new TestingParsing().parseJSONObject(finalResult,
							mode, latitude, longitude);
				} else if (mode.equals(GlobalConstants.ONLY_TRASH)) {
					setDustbins(new TestingParsing().parseJSONObject(
							finalResult, mode, latitude, longitude));
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

			if (mode.equals(GlobalConstants.ONLY_TOILETS)) {
				for (Iterator iterator = toilets.iterator(); iterator.hasNext();) {
					Component toilet = (Component) iterator.next();
					map.addMarker(new MarkerOptions()
							.position(
									new LatLng(toilet.getLatitude(), toilet
											.getLongitude()))
							.title(toilet.getName())
							.icon(BitmapDescriptorFactory
									.fromResource(R.drawable.icons_toilet_marker)));
				}
				//progressBar.dismiss();
			} else if (mode.equals(GlobalConstants.ONLY_TRASH)) {
				GlobalConstants.ADD_OR_SUGGESTED=false;
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
			progressBar.dismiss();
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
		ImageView imageView = (ImageView) findViewById(R.id.imageView5);
		imageView.setImageResource(R.drawable.icon_toliets_only);
		imageView = (ImageView) findViewById(R.id.imageView4);
		imageView.setImageResource(R.drawable.icon_view_all_deslect);
		imageView = (ImageView) findViewById(R.id.imageView6);
		imageView.setImageResource(R.drawable.icon_dustbin_deselect);
		filterType = GlobalConstants.ONLY_TOILETS;
		if (viewType.equals(GlobalConstants.MAP_VIEW)) {
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
									.fromResource(R.drawable.icons_toilet_marker)));
				}
			}
		} else if (viewType.equals(GlobalConstants.LIST_VIEW)) {
			listView(GlobalConstants.ONLY_TOILETS);
		}
	}

	public void onlyTrashCans(View view) {
		filterType = GlobalConstants.ONLY_TRASH;
		ImageView imageView = (ImageView) findViewById(R.id.imageView6);
		imageView.setImageResource(R.drawable.icon_dustbin);
		imageView = (ImageView) findViewById(R.id.imageView4);
		imageView.setImageResource(R.drawable.icon_view_all_deslect);
		imageView = (ImageView) findViewById(R.id.imageView5);
		imageView.setImageResource(R.drawable.icon_toilet_deselect);
		if (viewType.equals(GlobalConstants.MAP_VIEW)) {
			map.clear();
			if (trashcans.size() > 0 && latitude != 0 && longitude != 0) {
				map.addMarker(new MarkerOptions()
						.position(new LatLng(latitude, longitude))
						.title("You")
						.icon(BitmapDescriptorFactory
								.fromResource(R.drawable.icon_locator)));
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
		} else if (viewType.equals(GlobalConstants.LIST_VIEW)) {
			listView(GlobalConstants.ONLY_TRASH);
		}

	}

	public void viewAll(View view) {
		ImageView imageView = (ImageView) findViewById(R.id.imageView4);
		imageView.setImageResource(R.drawable.icon_view_all);
		imageView = (ImageView) findViewById(R.id.imageView5);
		imageView.setImageResource(R.drawable.icon_toilet_deselect);
		imageView = (ImageView) findViewById(R.id.imageView6);
		imageView.setImageResource(R.drawable.icon_dustbin_deselect);
		filterType = GlobalConstants.VIEW_ALL;
		if (viewType.equals(GlobalConstants.MAP_VIEW)) {
			map.clear();
			if ((toilets.size() > 0 || trashcans.size() > 0) && latitude != 0
					&& longitude != 0) {
				map.addMarker(new MarkerOptions()
						.position(new LatLng(latitude, longitude))
						.title("You")
						.icon(BitmapDescriptorFactory
								.fromResource(R.drawable.icon_locator)));
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
			if (toilets.size() > 0 && latitude != 0 && longitude != 0) {
				for (Iterator iterator = toilets.iterator(); iterator.hasNext();) {
					Component toilet = (Component) iterator.next();
					map.addMarker(new MarkerOptions()
							.position(
									new LatLng(toilet.getLatitude(), toilet
											.getLongitude()))
							.title(toilet.getName())
							.icon(BitmapDescriptorFactory
									.fromResource(R.drawable.icons_toilet_marker)));
				}
			}
		} else if (viewType.equals(GlobalConstants.LIST_VIEW)) {
			listView(GlobalConstants.VIEW_ALL);
		}
	}

	public void listView(View view) {
		viewType = GlobalConstants.LIST_VIEW;
		findViewById(R.id.map).setLayoutParams(
				new android.widget.RelativeLayout.LayoutParams(0, 0));
		ListView listView = (ListView) findViewById(R.id.list1);
		listView.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,
				LayoutParams.FILL_PARENT));

		ImageView imageView = (ImageView) findViewById(R.id.imageView3);
		imageView.setImageResource(R.drawable.icon_list_view);
		imageView = (ImageView) findViewById(R.id.imageView2);
		imageView.setImageResource(R.drawable.icon_map_view_deselect);
		ArrayList<Component> components = null;
		if (filterType.equals(GlobalConstants.VIEW_ALL)) {
			components = new ArrayList<Component>(toilets);
			components.addAll(trashcans);
		} else if (filterType.equals(GlobalConstants.ONLY_TOILETS)) {
			components = toilets;
		} else if (filterType.equals(GlobalConstants.ONLY_TRASH)) {
			components = trashcans;
		}
		Collections.sort(components, new ComponentComparatore());
		CustomizedAdapter adapter = new CustomizedAdapter(this,
				R.layout.activity_list_view, components);
		listView.setAdapter(adapter);

	}

	public void listView(String filter) {
		findViewById(R.id.map).setLayoutParams(
				new android.widget.RelativeLayout.LayoutParams(0, 0));
		ListView listView = (ListView) findViewById(R.id.list1);
		listView.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,
				LayoutParams.FILL_PARENT));
		ImageView imageView = (ImageView) findViewById(R.id.imageView3);
		imageView.setImageResource(R.drawable.icon_list_view);
		imageView = (ImageView) findViewById(R.id.imageView2);
		imageView.setImageResource(R.drawable.icon_map_view_deselect);
		ArrayList<Component> components = null;
		if (filter.equals(GlobalConstants.VIEW_ALL)) {
			components = new ArrayList<Component>(toilets);
			components.addAll(trashcans);
		} else if (filter.equals(GlobalConstants.ONLY_TOILETS)) {
			components = toilets;
		} else if (filter.equals(GlobalConstants.ONLY_TRASH)) {
			components = trashcans;
		}
		Collections.sort(components, new ComponentComparatore());
		CustomizedAdapter adapter = new CustomizedAdapter(this,
				R.layout.activity_list_view, components);
		listView.setAdapter(adapter);

	}

	public void mapView(View view) {
		viewType = GlobalConstants.MAP_VIEW;
		findViewById(R.id.list1).setLayoutParams(new LayoutParams(0, 0));
		findViewById(R.id.map)
				.setLayoutParams(
						new android.widget.RelativeLayout.LayoutParams(
								android.widget.RelativeLayout.LayoutParams.FILL_PARENT,
								android.widget.RelativeLayout.LayoutParams.FILL_PARENT));
		ImageView imageView = (ImageView) findViewById(R.id.imageView2);
		imageView.setImageResource(R.drawable.icon_map_view);
		imageView = (ImageView) findViewById(R.id.imageView3);
		imageView.setImageResource(R.drawable.icon_list_view_deselect);
	}

	@Override
	public void onInfoWindowClick(Marker marker) {

		LatLng destAddr = marker.getPosition();
		Double destLat = destAddr.latitude;
		Double destLon = destAddr.longitude;

		Double srcLat = cLocation.latitude;
		Double srcLon = cLocation.longitude;

		// System.out.println("URL = " + Uri.parse(
		// "http://maps.google.com/maps?" +
		// "saddr=" + srcLat + "," + srcLon + "&daddr=" + destLat + "," +
		// destLon));

		final Intent intent = new Intent(Intent.ACTION_VIEW,
		/** Using the web based turn by turn directions url. */

		Uri.parse("http://maps.google.com/maps?" + "saddr=" + srcLat + ","
				+ srcLon + "&daddr=" + destLat + "," + destLon));

		intent.setClassName("com.google.android.apps.maps",
				"com.google.android.maps.MapsActivity");
		startActivity(intent);
	}

	public void getDirections(View view) {
		ImageView view2 = (ImageView) view;
		String contentDescription = (String) view2.getContentDescription();
		String[] str = contentDescription.split("\\$");
		if (str.length == 2) {
			Double destLat = Double.parseDouble(str[0]);
			Double destLon = Double.parseDouble(str[1]);
			final Intent intent = new Intent(Intent.ACTION_VIEW,
			/** Using the web based turn by turn directions url. */

			Uri.parse("http://maps.google.com/maps?" + "saddr=" + latitude
					+ "," + longitude + "&daddr=" + destLat + "," + destLon));

			intent.setClassName("com.google.android.apps.maps",
					"com.google.android.maps.MapsActivity");
			startActivity(intent);
		}

	}

	public static LatLng reportingLatLng;

	@Override
	public void onMapLongClick(LatLng latLng) {
		reportingLatLng = latLng;
		map.clear();
		if (GlobalConstants.ONLY_TOILETS.equals(reportingType)) {
			map.addMarker(new MarkerOptions()
					.position(latLng)
					.title("toilet")
					.icon(BitmapDescriptorFactory
							.fromResource(R.drawable.icons_toilet_marker)));
		} else if (GlobalConstants.ONLY_TRASH.equals(reportingType)) {
			map.addMarker(new MarkerOptions()
					.position(latLng)
					.title("toilet")
					.icon(BitmapDescriptorFactory
							.fromResource(R.drawable.icon_dustbin_marker)));
		} else {
			map.addMarker(new MarkerOptions()
					.position(latLng)
					.title("toilet")
					.icon(BitmapDescriptorFactory
							.fromResource(R.drawable.icon_marker_black)));
		}

		findViewById(R.id.buttonConfirm)

				.setLayoutParams(
						new android.widget.RelativeLayout.LayoutParams(
								android.widget.RelativeLayout.LayoutParams.WRAP_CONTENT,
								android.widget.RelativeLayout.LayoutParams.WRAP_CONTENT));

		findViewById(R.id.buttonConfirm).setLeft(10);
		findViewById(R.id.buttonConfirm).setTop(10);

	}

	public void confirmButton(View view) {
		Intent intent = new Intent(this, AndroidTabMainActivity.class);
		intent.putExtra(GlobalConstants.LAT, reportingLatLng.latitude);
		intent.putExtra(GlobalConstants.LONG, reportingLatLng.longitude);
		intent.putExtra(GlobalConstants.FROM_VIEW, fromView);
		intent.putExtra(GlobalConstants.REPORT_TYPE, reportingType);
		startActivity(intent);

	}

}