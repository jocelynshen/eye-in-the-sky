package com.codepath.apps.restclienttemplate;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.codepath.apps.restclienttemplate.Objects.Tornado;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseGeoPoint;
import com.parse.SaveCallback;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import cz.msebera.android.httpclient.Header;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private boolean mJumpToCurrentLocation = false;
    private Location mLocation;
    private SupportMapFragment mapFragment;
    private EditText mSearchText;
    TextView selected;
    private File mPhotoFile;
    ParseFile mParseFile;
    ImageView mImage;
    private AlertDialog dialog;
    ProgressBar mProgressBar;
    private Location myLocation;
    private CustomInfoWindowAdapter mInfoAdapter;


    // Tweet variables
    TwitterClient client;
    WeatherClient weather;
    TextView characterCount; // tweet character count
    EditText mEditText; // tweet body during composition
    private String username; // your username
    private String profileUrl; // your profile image url

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        mSearchText = findViewById(R.id.searchText);
        client = TwitterApp.getRestClient(this);
        mProgressBar = findViewById(R.id.pbLoading);
        mInfoAdapter = new CustomInfoWindowAdapter(this);
        fetchUserInfo();

    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                mJumpToCurrentLocation = true;
                LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
                Criteria criteria = new Criteria();
                if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    mLocation = locationManager.getLastKnownLocation(locationManager.getBestProvider(criteria, false));
                }
                setupMapUserLocation();
            }
        }
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setInfoWindowAdapter(mInfoAdapter);
        mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.style_json_retro));
        mMap.getUiSettings().setMapToolbarEnabled(true);

        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                marker.showInfoWindow();
                return false;
            }
        });
        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(final LatLng latLng) {
                final AlertDialog.Builder mBuilder = new AlertDialog.Builder(MapActivity.this);
                View mView = getLayoutInflater().inflate(R.layout.item_compose, null);
                mBuilder.setView(mView);
                dialog = mBuilder.create();
                ImageView ivProfileImage = mView.findViewById(R.id.yourProfile);
                TextView tvUserName = mView.findViewById(R.id.yourUsername);
                tvUserName.setText(username); // set your username
                Glide.with(MapActivity.this).load(profileUrl).apply(RequestOptions.circleCropTransform()).into(ivProfileImage); // set your profile image
                characterCount = mView.findViewById(R.id.counter); // set character count
                mEditText = mView.findViewById(R.id.etTweetBody);
                Button mSave = mView.findViewById(R.id.tweet_button);

                ImageView croc = mView.findViewById(R.id.croc);
                ImageView power = mView.findViewById(R.id.power);
                ImageView roadblock = mView.findViewById(R.id.roadblock);
                ImageView traffic = mView.findViewById(R.id.traffic);
                ImageView fire = mView.findViewById(R.id.fire);
                ImageView flood = mView.findViewById(R.id.flood);

                mImage = mView.findViewById(R.id.image);

                ImageView ivCamera = mView.findViewById(R.id.ivCamera);
                ivCamera.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        mPhotoFile = getPhotoFileUri(MapActivity.this, "photo.jpg");
                        System.out.println(mPhotoFile);

                        Uri fileProvider = FileProvider.getUriForFile(MapActivity.this, "com.example.fileprovider", mPhotoFile);
                        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileProvider);
                        if (intent.resolveActivity(getPackageManager()) != null) {
                            startActivityForResult(intent, 1034);
                        }
                    }
                });

                selected = mView.findViewById(R.id.selected);

                croc.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        selected.setText("Wildlife");
                    }
                });

                power.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        selected.setText("Power outage");
                    }
                });

                roadblock.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        selected.setText("Roadblock");
                    }
                });

                traffic.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        selected.setText("Traffic");
                    }
                });

                fire.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        selected.setText("Fire");
                    }
                });

                flood.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        selected.setText("Flood");
                    }
                });

                System.out.println(selected.getText().toString());

                mSave.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (selected.getText().toString().length() > 0) {
                            String description = mEditText.getText().toString();
                            String image = null;
                            if (mParseFile != null) {
                                image = mParseFile.getUrl();
                                System.out.println("image not null");
                            }
//                            createMarker(latLng, selected.getText().toString(), image);
                            createMarker(MapActivity.this, description, mParseFile, latLng, selected.getText().toString());
                            dialog.dismiss();
                        }
                    }
                });

                mEditText.addTextChangedListener(mTextEditorWatcher);
                dialog.show();            }
        });
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                System.out.println(latLng);
            }
        });
        mJumpToCurrentLocation = true;
        setupMapUserLocation();
        View locationButton = ((View) this.findViewById(Integer.parseInt("1")).getParent()).findViewById(Integer.parseInt("2"));
        RelativeLayout.LayoutParams rlpMyLocation = (RelativeLayout.LayoutParams) locationButton.getLayoutParams();
        rlpMyLocation.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0);
        rlpMyLocation.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
        rlpMyLocation.setMargins(0, 200, 180, 0);
        myLocation = mMap.getMyLocation();
        handleSearch();
        loadMarkers();
        getDataPoints();

    }

    /**
     * Requests permission for location and handles finding user location
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    private void setupMapUserLocation() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            return;
        }
        mMap.setMyLocationEnabled(true);
        if (mJumpToCurrentLocation && mLocation != null) {
            mJumpToCurrentLocation = false;
            MapUtil.centreMapOnLocation(mMap, mLocation);
        }

        mMap.setOnMyLocationChangeListener(new GoogleMap.OnMyLocationChangeListener() {
            @Override
            public void onMyLocationChange(Location arg0) {
                Location temp = new Location(LocationManager.GPS_PROVIDER);
                temp.setLatitude(arg0.getLatitude());
                temp.setLongitude(arg0.getLongitude());
                if (mJumpToCurrentLocation) {
                    mJumpToCurrentLocation = false;
                    MapUtil.centreMapOnLocation(mMap, temp);
                }
            }
        });
    }

    /**
     * Handles searching for location
     */
    private void handleSearch() {
        mSearchText.setOnEditorActionListener(new EditText.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    Intent i = new Intent(MapActivity.this, TimelineActivity.class);
                    i.putExtra("query", mSearchText.getText().toString());
                    Location location = mMap.getMyLocation();
                    System.out.println(location);
                    i.putExtra("location", location);
                    startActivityForResult(i, 30);
                }
                InputMethodManager inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                return false;
            }
        });
    }


    public void onComposeAction(View v){
        /*
        Handles all actions related to composing tweet
         */
        final AlertDialog.Builder mBuilder = new AlertDialog.Builder(MapActivity.this);
        View mView = getLayoutInflater().inflate(R.layout.item_compose, null);
        mBuilder.setView(mView);
        dialog = mBuilder.create();
        ImageView ivProfileImage = mView.findViewById(R.id.yourProfile);
        TextView tvUserName = mView.findViewById(R.id.yourUsername);
        tvUserName.setText(username); // set your username
        Glide.with(this).load(profileUrl).apply(RequestOptions.circleCropTransform()).into(ivProfileImage); // set your profile image
        characterCount = mView.findViewById(R.id.counter); // set character count
        mEditText = mView.findViewById(R.id.etTweetBody);
        Button mSave = mView.findViewById(R.id.tweet_button);

        ImageView croc = mView.findViewById(R.id.croc);
        ImageView power = mView.findViewById(R.id.power);
        ImageView roadblock = mView.findViewById(R.id.roadblock);
        ImageView traffic = mView.findViewById(R.id.traffic);
        ImageView fire = mView.findViewById(R.id.fire);
        ImageView flood = mView.findViewById(R.id.flood);

        mImage = mView.findViewById(R.id.image);

        ImageView ivCamera = mView.findViewById(R.id.ivCamera);
        ivCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                mPhotoFile = getPhotoFileUri(MapActivity.this, "photo.jpg");
                System.out.println(mPhotoFile);

                Uri fileProvider = FileProvider.getUriForFile(MapActivity.this, "com.example.fileprovider", mPhotoFile);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, fileProvider);
                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivityForResult(intent, 1034);
                }
            }
        });

        selected = mView.findViewById(R.id.selected);

        croc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selected.setText("Wildlife");
            }
        });

        power.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selected.setText("Power outage");
            }
        });

        roadblock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selected.setText("Roadblock");
            }
        });

        traffic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selected.setText("Traffic");
            }
        });

        fire.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selected.setText("Fire");
            }
        });

        flood.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selected.setText("Flood");
            }
        });

        System.out.println(selected.getText().toString());

        mSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (selected.getText().toString().length() > 0) {
                    String description = mEditText.getText().toString();
                    Location location = mMap.getMyLocation();
                    LatLng currLocation = new LatLng(location.getLatitude(), location.getLongitude());
                    String image = null;
                    if (mParseFile != null) {
                        image = mParseFile.getUrl();
                    }
                    createMarker(currLocation, selected.getText().toString(), image);
                    createMarker(MapActivity.this, description, mParseFile, currLocation, selected.getText().toString());
                    dialog.dismiss();
                }
            }
        });

        mEditText.addTextChangedListener(mTextEditorWatcher);
        dialog.show();
    }

    public static File getPhotoFileUri(Context context, String fileName) {
        File mediaStorageDir = new File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), String.valueOf(R.string.app_name));

        if (!mediaStorageDir.exists() && !mediaStorageDir.mkdirs()) {
            Log.d(String.valueOf(R.string.app_name), "failed to create directory");
        }

        File file = new File(mediaStorageDir.getPath() + File.separator + fileName);
        return file;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, final Intent data) {
        if (requestCode == 1034) {
            if (resultCode == this.RESULT_OK) {
                Bitmap takenImage = BitmapFactory.decodeFile(mPhotoFile.getAbsolutePath());
                mImage.setVisibility(View.VISIBLE);
                mImage.setImageBitmap(takenImage);
                File photoFile = getPhotoFileUri(this, "photo.jpg");
                mParseFile = new ParseFile(photoFile);
            } else {
                mParseFile = null;
                Toast.makeText(MapActivity.this, "Picture was not taken", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public com.codepath.apps.restclienttemplate.models.Marker createMarker(final Context context, String description, final ParseFile imageFile, final LatLng point, final String type) {
        final com.codepath.apps.restclienttemplate.models.Marker newPost = new com.codepath.apps.restclienttemplate.models.Marker();
        newPost.setDescription(description);
        if (imageFile == null) {
            newPost.remove("image");
        } else {
            newPost.setImage(imageFile);
        }
        newPost.setLocation(new ParseGeoPoint(point.latitude, point.longitude));
        newPost.setType(type);
        newPost.saveInBackground(new SaveCallback() {
            @Override
            public void done(com.parse.ParseException e) {
                if (e == null) {
                    createMarker(new LatLng(point.latitude, point.longitude), type, imageFile.getUrl());
                    Toast.makeText(context, context.getResources().getString(R.string.post_message), Toast.LENGTH_SHORT).show();
                } else {
                    e.printStackTrace();
                }
            }
        });
        return newPost;
    }

    public void fetchUserInfo(){
        /*
        Uses TwitterClient to grab your screen name and profile image url
         */
        client.getProfile(new JsonHttpResponseHandler(){
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    username = response.getString("screen_name");
                    profileUrl = response.getString("profile_image_url");
                }
                catch (JSONException e){
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                super.onFailure(statusCode, headers, throwable, errorResponse);
            }
        });
    }

    ArrayList<Tornado> tornadoList;

    public void getDataPoints() {
        mProgressBar.setVisibility(ProgressBar.VISIBLE);
        tornadoList = new ArrayList<Tornado>();
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String url = "https://www.ncdc.noaa.gov/swdiws/json/nx3tvs/201902020000:201902022359";
                    URL obj = new URL(url);
                    HttpURLConnection con = (HttpURLConnection) obj.openConnection();
                    int responseCode = con.getResponseCode();
                    System.out.println("\nSending 'GET' request to URL : " + url);
                    System.out.println("Response Code : " + responseCode);
                    BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                    String inputLine;
                    StringBuffer response = new StringBuffer();
                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    } in .close();

                    String responseString = response.toString();
                    int first = responseString.indexOf("[");
                    int last = responseString.lastIndexOf("]");
                    String responseJSONString = '{' + responseString.substring(224,last+1) + '}';
                    String fixedString = responseJSONString.replaceAll("\"","\\\"");

                    String testJSONString = "{\"result\": [{\"CELL_TYPE\":\"TVS\"}]}";
                    final Gson gson = new Gson();
                    JsonParser parser = new JsonParser();
                    JsonObject jsonObject = parser.parse(fixedString).getAsJsonObject();
                    JsonElement jsonObjects = jsonObject.get("result");
                    final JsonArray jsonArray = jsonObjects.getAsJsonArray();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            for (int i=0; i < jsonArray.size(); i++) {
                                Tornado newTornado = gson.fromJson(jsonArray.get(i), Tornado.class);
                                newTornado.fixCoordinates();
                                tornadoList.add(newTornado);
                                createMarkerTornado(new LatLng(newTornado.latitude, newTornado.longitude), "Tornado");
                            }
                            mProgressBar.setVisibility(ProgressBar.INVISIBLE);
                        }
                    });

                } catch(Exception e) {
                    System.out.println(e);
                }
            }
        });
        thread.start();
    };

    private final TextWatcher mTextEditorWatcher = new TextWatcher() {
        /*
        Handles counting characters
         */
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        public void onTextChanged(CharSequence s, int start, int before, int count) {
            characterCount.setText(String.valueOf(s.length()));
        }

        public void afterTextChanged(Editable s) {
        }
    };

    private BitmapDescriptor getMarkers(String type) {
        Drawable descriptor;
        if (type.equals("Wildlife")) {
            descriptor = getResources().getDrawable(R.drawable.croc);
        } else if (type.equals("Fire")) {
            descriptor = getResources().getDrawable(R.drawable.fire);
        } else if (type.equals("firetruck")) {
            descriptor = getResources().getDrawable(R.drawable.firetruck);
        } else if (type.equals("Flood")) {
            descriptor = getResources().getDrawable(R.drawable.flood);
        } else if (type.equals("Hospital")) {
            descriptor = getResources().getDrawable(R.drawable.hospital);
        } else if (type.equals("Roadblock")) {
            descriptor = getResources().getDrawable(R.drawable.roadblock);
        } else if (type.equals("Tornado")) {
            descriptor = getResources().getDrawable(R.drawable.tornado);
        }else if (type.equals("Power outage")) {
            descriptor = getResources().getDrawable(R.drawable.powerline);
        }
        else {
            descriptor = getResources().getDrawable(R.drawable.traffic);
        }
        BitmapDescriptor markerIcon = getMarkerIconFromDrawable(descriptor);

        return markerIcon;
    }

    private BitmapDescriptor getMarkerIconFromDrawable(Drawable drawable) {
        Canvas canvas = new Canvas();
        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        canvas.setBitmap(bitmap);
        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
        drawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    public Marker createMarker(LatLng position, String type, String imageUrl) {
        Marker marker;
        if (imageUrl != null) {
            marker = mMap.addMarker(new MarkerOptions()
                    .position(position)
                    .title(getAddress(this, position))
                    .snippet(imageUrl)
                    .icon(getMarkers(type)));
        } else {
            marker = mMap.addMarker(new MarkerOptions()
                    .position(position)
                    .title(getAddress(this, position))
                    .snippet("")
                    .icon(getMarkers(type)));
        }
        return marker;
    }

    public Marker createMarkerTornado(LatLng position, String type) {
        Marker marker = mMap.addMarker(new MarkerOptions()
                .position(position)
                .title(getAddress(this, position))
                .snippet(null)
                .icon(getMarkers(type)));
        return marker;
    }


    /**
     * Helper function to get address from point
     *
     * @param context
     * @param point   with latitude and longitude
     * @return address
     */
    public static String getAddress(Context context, LatLng point) {
        try {
            Geocoder geo = new Geocoder(context, Locale.getDefault());
            List<Address> addresses = geo.getFromLocation(point.latitude, point.longitude, 1);
            if (addresses.isEmpty()) {
                return "Waiting for location...";
            } else {
                if (addresses.size() > 0) {
                    String address = (addresses.get(0).getFeatureName() + ", " + addresses.get(0).getLocality() + ", " + addresses.get(0).getAdminArea() + ", " + addresses.get(0).getCountryName());
                    address = address.replaceAll(" null,", "");
                    address = address.replaceAll(", null", "");
                    return address;
                }
            }
        } catch (Exception e) {
            e.printStackTrace(); // getFromLocation() may sometimes fail
            return null;
        }
        return null;
    }

    private ArrayList<com.codepath.apps.restclienttemplate.models.Marker> markers;

    public void loadMarkers() {
        markers = new ArrayList<>();
//        mProgressBar.setVisibility(ProgressBar.VISIBLE);
        final com.codepath.apps.restclienttemplate.models.Marker.Query postQuery = new com.codepath.apps.restclienttemplate.models.Marker.Query();
//        postQuery.withinPoint(new ParseGeoPoint(myLocation.getLatitude(), myLocation.getLongitude()), 50);
        postQuery.findInBackground(new FindCallback<com.codepath.apps.restclienttemplate.models.Marker>() {
            @Override
            public void done(List<com.codepath.apps.restclienttemplate.models.Marker> objects, ParseException e) {
                if (e == null) {
                    for (int i = 0; i < objects.size(); i++) {
                        com.codepath.apps.restclienttemplate.models.Marker md = objects.get(i);
                        markers.add(md);
                    }
                    for (com.codepath.apps.restclienttemplate.models.Marker markerDetails : markers) {
                        ParseGeoPoint gp = markerDetails.getLocation();
                        LatLng location = new LatLng(gp.getLatitude(), gp.getLongitude());
                        String image = null;
                        if (markerDetails.getImage() != null) {
                            image = markerDetails.getImage().getUrl();
                        }
                        Marker m = createMarker(location, markerDetails.getType(), image);
                    }
//                    mProgressBar.setVisibility(ProgressBar.INVISIBLE);
                } else {
//                    mProgressBar.setVisibility(ProgressBar.INVISIBLE);
                }
            }
        });
    }

}
