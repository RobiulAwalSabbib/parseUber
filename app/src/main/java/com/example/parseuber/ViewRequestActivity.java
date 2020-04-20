package com.example.parseuber;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

public class ViewRequestActivity extends AppCompatActivity {

    ListView requestListView;
    ArrayList<String> requests = new ArrayList<String>();
    ArrayAdapter requestAdapter;
    ArrayList<Double> requestLattitude = new ArrayList<Double>();
    ArrayList<Double> requestLongitude = new ArrayList<Double>();
    ArrayList<String> usernames = new ArrayList<String>();

    LocationManager locationManager;
    LocationListener locationListener;




    public void updateListView(Location location){


        if(location != null) {

            ParseQuery<ParseObject> query = ParseQuery.getQuery("Request");
            final ParseGeoPoint getPointLocation = new ParseGeoPoint(location.getLatitude(),location.getLongitude());
            query.whereNear("location",getPointLocation);
            query.whereDoesNotExist("driverUserName");
            //query.whereExists("driverUserName");

            query.setLimit(10);
            query.findInBackground(new FindCallback<ParseObject>() {
                @Override
                public void done(List<ParseObject> objects, ParseException e) {

                    if(e==null){

                        requests.clear();
                        requestLattitude.clear();
                        requestLongitude.clear();
                        usernames.clear();

                        if(objects.size() > 0){

                            for(ParseObject object : objects){

                                ParseGeoPoint requestLocation=(ParseGeoPoint) object.getParseGeoPoint("location");

                                if(requestLocation != null) {
                                    Double distanceInMiles = getPointLocation.distanceInMilesTo(requestLocation);
                                    Double distanceOneDp = (double) Math.round(distanceInMiles * 10) / 10;

                                    requests.add(distanceOneDp.toString() + " Miles");

                                    requestLattitude.add(requestLocation.getLatitude());
                                    requestLongitude.add(requestLocation.getLongitude());
                                    usernames.add(object.getString("username"));
                                }
                            }

                        }else {
                            requests.add("No Active Request nearby");
                            Toast.makeText(getApplicationContext(),"Location not in ArrayList..",Toast.LENGTH_LONG).show();
                        }


                        requestAdapter.notifyDataSetChanged();
                    }

                }
            });


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

                    updateListView(lastKnownLocation);
                }
            }
        }


    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_request);


        setTitle("Nearby Request");

        requestListView = findViewById(R.id.requestListView);

        requestAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, requests);

        requests.clear();

        requestListView.setAdapter(requestAdapter);

        requestListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                if(ContextCompat.checkSelfPermission(ViewRequestActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                        ContextCompat.checkSelfPermission(ViewRequestActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                    Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

                    if(requestLattitude.size() > position && requestLongitude.size() > position && usernames.size() > position &&  lastKnownLocation != null) {

                        Intent intent = new Intent(ViewRequestActivity.this, DriverLocationActivity.class);
                        intent.putExtra("requestLattitude", requestLattitude.get(position));
                        intent.putExtra("requestLongitude", requestLongitude.get(position));
                        intent.putExtra("driverLattitude", lastKnownLocation.getLatitude());
                        intent.putExtra("driverLongitude", lastKnownLocation.getLongitude());
                        intent.putExtra("username",usernames.get(position));

                        startActivity(intent);
                    }
                }
            }
        });

        locationManager = (LocationManager) this.getSystemService(LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {

                updateListView(location);

                ParseUser.getCurrentUser().put("location",new ParseGeoPoint(location.getLatitude(),location.getLongitude()));
                ParseUser.getCurrentUser().saveInBackground();

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

                    updateListView(lastKnownLocation);
                }

            }

        }


    }
}
