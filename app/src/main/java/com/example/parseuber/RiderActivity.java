package com.example.parseuber;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.List;

public class RiderActivity extends FragmentActivity implements OnMapReadyCallback {

        private GoogleMap mMap;

        LocationManager locationManager;
        LocationListener locationListener;

        Button callUberButton;
        Boolean activeRequest= false;

        TextView infoTV;

        Handler handler = new Handler();

        Boolean driverActive = false;
        ArrayList<Marker> markers = new ArrayList<>();

//        public void driverLocPoint(){
//            LatLng driverLocationLatLon = new LatLng(driverLocation.getLatitude(),driverLocation.getLongitude());
//            LatLng riderLocationLatLon = new LatLng(userLocation.getLatitude(),userLocation.getLongitude());
//
//
//
//            markers.add(mMap.addMarker(new MarkerOptions().position(driverLocationLatLon).title("Driver Location")));
//            markers.add(mMap.addMarker(new MarkerOptions().position(riderLocationLatLon).title("User Location")));
//
//            LatLngBounds.Builder builder = new LatLngBounds.Builder();
//            for (Marker marker : markers) {
//                builder.include(marker.getPosition());
//            }
//            LatLngBounds bounds = builder.build();
//
//            int padding = 60; // offset from edges of the map in pixels
//            CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
//
//            mMap.moveCamera(cu);
//
//
//            mMap.animateCamera(cu);
//
//
//        }

        public void checkForUpdate(){

            ParseQuery<ParseObject> query = ParseQuery.getQuery("Request");
            query.whereEqualTo("username",ParseUser.getCurrentUser().getUsername());
            query.whereExists("driverUserName");
            query.findInBackground(new FindCallback<ParseObject>() {
                @Override
                public void done(List<ParseObject> objects, ParseException e) {

                    if(e==null && objects.size() > 0){

                        ParseQuery<ParseUser> query = ParseUser.getQuery();
                        query.whereEqualTo("username", objects.get(0).getString("driverUserName"));
                        query.findInBackground(new FindCallback<ParseUser>() {
                            @Override
                            public void done(List<ParseUser> objects, ParseException e) {

                                if(e==null && objects.size()>0){

                                    driverActive = true;

                                    ParseGeoPoint driverLocation = objects.get(0).getParseGeoPoint("location");


                                    if (ContextCompat.checkSelfPermission(RiderActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                                            ContextCompat.checkSelfPermission(RiderActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                                        Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

                                        if (lastKnownLocation != null){

                                            ParseGeoPoint userLocation = new ParseGeoPoint(lastKnownLocation.getLatitude(),lastKnownLocation.getLongitude());


                                            Double distanceInMiles = userLocation.distanceInMilesTo(driverLocation);

                                            if(distanceInMiles < 0.01){

                                                infoTV.setText("Driver is Here....Welcome");


                                                ParseQuery<ParseObject> query = ParseQuery.getQuery("Request");
                                                query.whereEqualTo("username",ParseUser.getCurrentUser().getUsername());
                                                query.findInBackground(new FindCallback<ParseObject>() {
                                                    @Override
                                                    public void done(List<ParseObject> objects, ParseException e) {
                                                        if(e==null && objects.size()>0){

                                                            for (ParseObject object : objects){

                                                                object.deleteInBackground();
                                                            }

                                                        }
                                                    }
                                                });

                                                handler.postDelayed(new Runnable() {
                                                    @Override
                                                    public void run() {

                                                        infoTV.setText("");
                                                        callUberButton.setVisibility(View.VISIBLE);
                                                        callUberButton.setText("Call Uber");
                                                        driverActive=false;
                                                        activeRequest=false;


                                                    }
                                                },5000);




                                            }else {
                                                Double distanceOneDp = (double) Math.round(distanceInMiles * 10) / 10;


                                                infoTV.setText("Driver is " + distanceOneDp.toString() + " miles away");

                                                LatLng driverLocationLatLon = new LatLng(driverLocation.getLatitude(), driverLocation.getLongitude());
                                                LatLng riderLocationLatLon = new LatLng(userLocation.getLatitude(), userLocation.getLongitude());

                                                mMap.clear();

                                                markers.add(mMap.addMarker(new MarkerOptions().position(driverLocationLatLon).title("Driver Location")));
                                                markers.add(mMap.addMarker(new MarkerOptions().position(riderLocationLatLon).title("User Location")));

                                                LatLngBounds.Builder builder = new LatLngBounds.Builder();
                                                for (Marker marker : markers) {
                                                    builder.include(marker.getPosition());
                                                }
                                                LatLngBounds bounds = builder.build();

                                                int padding = 60; // offset from edges of the map in pixels
                                                CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);

                                                mMap.moveCamera(cu);


                                                mMap.animateCamera(cu);

                                                callUberButton.setVisibility(View.INVISIBLE);


                                                handler.postDelayed(new Runnable() {
                                                    @Override
                                                    public void run() {

                                                        checkForUpdate();

                                                    }
                                                },2000);
                                            }


                                        }



                                    }

                                }
                            }
                        });



                    }


                }
            });

        }

        public void logoutUber(View view){

            ParseUser.logOut();
            Intent intent = new Intent(getApplicationContext(),MainActivity.class);
            startActivity(intent);

        }

        public void callUber(View view){

            Log.i("Button","Click");

            if(activeRequest){
                ParseQuery<ParseObject> query = new ParseQuery<ParseObject>("Request");
                query.whereEqualTo("username",ParseUser.getCurrentUser().getUsername());
                query.findInBackground(new FindCallback<ParseObject>() {
                    @Override
                    public void done(List<ParseObject> objects, ParseException e) {
                        if(e==null){

                            if(objects.size() > 0){

                                for(ParseObject obj : objects){

                                    obj.deleteInBackground();

                                }

                                activeRequest=false;
                                callUberButton.setText("Call Uber");
                            }
                        }
                    }
                });

            }else {

            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
                Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

                if (lastKnownLocation != null) {

                    ParseObject request = new ParseObject("Request");
                    request.put("username", ParseUser.getCurrentUser().getUsername());
                    ParseGeoPoint location = new ParseGeoPoint(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude());
                    request.put("location", location);
                    request.saveInBackground(new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
                            if (e == null) {

                                activeRequest=true;
                                callUberButton.setText("Cancel Uber");

                                checkForUpdate();

                            }
                        }
                    });
                } else {

                    Toast.makeText(getApplicationContext(), "Try Again", Toast.LENGTH_LONG).show();
                    }
                }

            }

        }


        @Override
        public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);

            if (requestCode == 1) {

                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                        ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
                        Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

                        updateLocation(lastKnownLocation);
                    }
                }
            }


        }

        public void updateLocation(Location location) {

            if(driverActive != false) {

                LatLng userLocation = new LatLng(location.getLatitude(), location.getLongitude());

                mMap.clear();
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 16));
                mMap.addMarker(new MarkerOptions().position(userLocation).title("Your Location"));
            }

        }

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_rider);
            // Obtain the SupportMapFragment and get notified when the map is ready to be used.
            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.map);
            mapFragment.getMapAsync(this);


            callUberButton = findViewById(R.id.callUberButton);
            infoTV = findViewById(R.id.infoTV);

            ParseQuery<ParseObject> query = new ParseQuery<ParseObject>("Request");
            query.whereEqualTo("username",ParseUser.getCurrentUser().getUsername());
            query.findInBackground(new FindCallback<ParseObject>() {
                @Override
                public void done(List<ParseObject> objects, ParseException e) {
                    if(e==null){
                        if(objects.size() > 0){
                        activeRequest=true;
                        callUberButton.setText("Requesting Uber");
                            checkForUpdate();

                        }
                    }
                }
            });

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
        @Override
        public void onMapReady(GoogleMap googleMap) {
            mMap = googleMap;


            locationManager = (LocationManager) this.getSystemService(LOCATION_SERVICE);
            locationListener = new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {

                    updateLocation(location);

                }

                @Override
                public void onStatusChanged(String provider, int status, Bundle extras) {

                }

                @Override
                public void onProviderEnabled(String provider) {

                }

                @Override
                public void onProviderDisabled(String provider) {

                }
            };


            if (Build.VERSION.SDK_INT < 23) {
                if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                        checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    Activity#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for Activity#requestPermissions for more details.
                    return;
                }
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);

            }else {

                if(ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION )!= PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_COARSE_LOCATION )!= PackageManager.PERMISSION_GRANTED){

                    ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,Manifest.permission.ACCESS_FINE_LOCATION},1);

                }else {

                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
                    Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

                    if(lastKnownLocation !=null){

                        updateLocation(lastKnownLocation);
                    }

                }

            }
        }
    }
