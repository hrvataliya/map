package com.example.hardik.myapplication;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final int REQUEST_ID_MULTIPLE_PERMISSIONS = 1;
    private static final int REQUEST_LOCATION = 1;
    public static GoogleMap mMap;
    private GPSTracker gps;
    public static Double lat, lng;
    boolean fromGps_alert = false;
    List<Address> addresses = null;
    String addressText = "";

    EditText enteraddress;
    Button add;

    Double latitude,longitude;
    public static Double pickuplatitude, pickuplongitude,destinationlatitude,destinationlongitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        checkAndRequestPermissions();

        enteraddress = (EditText) findViewById(R.id.address);
        add = (Button) findViewById(R.id.add);

        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String pickupaddress = enteraddress.getText().toString();

                if(pickupaddress.length()!=0) {
                    GeocodingLocation pickuplocationAddress = new GeocodingLocation();
                    pickuplocationAddress.getAddressFromLocation(pickupaddress, MainActivity.this, new GeocoderHandler());
                }
                else {
                    enteraddress.setError("Enter address");
                }

                // Polylines are useful for marking paths and routes on the map.
                mMap.addPolyline(new PolylineOptions().geodesic(true)
                        .add(new LatLng(lat, lng))
                        .add(new LatLng(pickuplatitude, pickuplongitude))

                );

            }
        });

    }

    private void checkAndRequestPermissions() {
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) ==
                        PackageManager.PERMISSION_GRANTED) {

        } else {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_LOCATION);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_LOCATION: {

                Log.e("TAG", "onRequestPermissionsResult");
                // If request is cancelled, the result arrays are empty.
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    //getLocation();
                    gps = new GPSTracker(MainActivity.this);
                    if (gps.canGetLocation()) {
                        lat = gps.getLatitude();
                        lng = gps.getLongitude();
                        if (lat != 0.0 && lng != 0.0) {
                            try {
                                setUpMap();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                } else {
                    checkAndRequestPermissions();
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app

        }
    }


    private void setUpMapIfNeeded() throws IOException {
       /* MapFragment mapFragment = (MapFragment) getFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(MainActivity.this);*/

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(MainActivity.this);

    }

    @Override
    public void onMapReady(GoogleMap map) {
        mMap = map;
        try {
            setUpMap();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setUpMap() throws IOException {
        //gps = new GPSTracker(this);
        if (gps.canGetLocation()) {
            lat = gps.getLatitude();
            lng = gps.getLongitude();
            Log.e("TAG",String.valueOf(lat)  +  String.valueOf(lng));

            mMap.clear();

            LatLng sydney = new LatLng(lat, lng);

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            mMap.setMyLocationEnabled(true);

            /*MapStyleOptions style = MapStyleOptions.loadRawResourceStyle(
                    this, R.raw.style_json);
            map.setMapStyle(style);*/

            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(sydney, 16));

            mMap.addMarker(new MarkerOptions()
                    .title(addressText)
                    .snippet("The most populous city in India.")
                    /*.icon(BitmapDescriptorFactory.fromResource(R.drawable.house_flag))
                    .anchor(0.0f, 1.0f) // Anchors the marker on the bottom left*/
                    /*.flat(true)
                    .rotation(245)*/
                    .position(sydney));

            /*CameraPosition cameraPosition = CameraPosition.builder()
                    .target(sydney)
                    .zoom(13)
                    .bearing(90)
                    .build();

            // Animate the change in camera view over 2 seconds
            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition),
                    2000, null);*/

            // Polylines are useful for marking paths and routes on the map.
           /* mMap.addPolyline(new PolylineOptions().geodesic(true)
                    .add(new LatLng(-33.866, 151.195))  // Sydney
                    .add(new LatLng(-18.142, 178.431))  // Fiji
                    .add(new LatLng(21.291, -157.821))  // Hawaii
                    .add(new LatLng(37.423, -122.091))  // Mountain View
            );*/

            try {
                Geocoder geocoder = new Geocoder(MainActivity.this);
                addresses = geocoder.getFromLocation(lat, lng, 1);
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (addresses != null && addresses.size() > 0) {
                Address address = addresses.get(0);

                addressText = String.format("%s, %s, %s",
                        address.getMaxAddressLineIndex() > 0 ? address.getAddressLine(0) : "",
                        address.getLocality(),
                        address.getCountryName());
            }

        }else {
            fromGps_alert = true;
            gps.showSettingsAlert();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        MainActivity.this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        try {
            if(fromGps_alert==true) {
                gps = new GPSTracker(MainActivity.this);
                if (gps.canGetLocation()) {
                    lat = gps.getLatitude();
                    lng = gps.getLongitude();
                    fromGps_alert=false;
                    setUpMap();
                } else {
                    gps.showSettingsAlert();
                }
            }
            else{
                gps = new GPSTracker(MainActivity.this);
                try {
                    setUpMapIfNeeded();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private class GeocoderHandler extends Handler {
        @Override
        public void handleMessage(Message message) {
            String locationAddress;
            switch (message.what) {
                case 1:
                    Bundle bundle = message.getData();
                    locationAddress = bundle.getString("address");
                    break;
                default:
                    locationAddress = null;
            }

        }
    }

    private class GeocodingLocation {

        private static final String TAG = "GeocodingLocation";

        public  void getAddressFromLocation(final String pickuplocationAddress,
                                            final Context context, final Handler handler) {

            Geocoder geocoder = new Geocoder(context, Locale.getDefault());
            String result = null;
            try {
                List<Address> addressList = geocoder.getFromLocationName(pickuplocationAddress, 1);
                if (addressList != null && addressList.size() > 0) {
                    Address address = addressList.get(0);
                    pickuplatitude=address.getLatitude();
                    pickuplongitude=address.getLongitude();
                    Log.e("pickup lat lng",pickuplatitude + " " + pickuplongitude);
                    if(pickuplatitude!=null && pickuplongitude!=null){
                        enteraddress.setText(pickuplocationAddress);
                        //destination.setText(locationAddress);
                    }
                }
            } catch (IOException e) {
                Log.e(TAG, "Unable to connect to Geocoder", e);
            }

        }
    }
}
