package com.example.grab;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class CustomerMapActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private LocationManager mLocationManager;
    private LocationListener mLocationListener;
    private Location mLastLocation;
    private Button mlogout, mRequest, mThongtin;
    private EditText mSearchBox;
    private LatLng VitriKhach, goalPosition;
    private boolean requestBol = false, chooseGoal = false;
    private Marker khachMarker, goalMarker;
    private String driverName, driverSdt, khoangcach, giatien, goalAdress;
    AlertDialog.Builder builder, builder2;
    AlertDialog alertDialog, alertDialog2;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        builder = new AlertDialog.Builder(CustomerMapActivity.this);
        builder2 = new AlertDialog.Builder(CustomerMapActivity.this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_map);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        final SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        mLocationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                mLastLocation = location;
//                LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
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

        mRequest = (Button) findViewById(R.id.request);
        mlogout = (Button) findViewById(R.id.logout);
        mThongtin = (Button)findViewById(R.id.allInfor);
        mSearchBox = (EditText)findViewById(R.id.searchBox);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling

            return;
        }
        mLastLocation = mLocationManager.getLastKnownLocation("gps");

        mSearchBox.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if(actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_ACTION_DONE || event.getAction() == KeyEvent.ACTION_DOWN || event.getAction() == KeyEvent.KEYCODE_ENTER ){
                    String searchStr = mSearchBox.getText().toString();
                    Geocoder geoCoder = new Geocoder(CustomerMapActivity.this);
                    List<Address> list = new ArrayList<>();
                    try {
                        list = geoCoder.getFromLocationName(searchStr,1);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    if(list.size() > 0){
                        Address address = list.get(0);
                        Log.d("address", "onEditorAction: " + address.toString());
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(address.getLatitude(), address.getLongitude()),14));
                    }
                }
                return false;
            }
        });
        mlogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(CustomerMapActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
                return;
            }
        });

        mThongtin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog2.show();
            }
        });

        mRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(chooseGoal){
                    if(requestBol){
                        mThongtin.setVisibility(View.INVISIBLE);
                        geoQuery.removeAllListeners();
                        if(driverLocationRefListioner != null){
                            driverLocationRef.removeEventListener(driverLocationRefListioner);
                        }

                        if(driverID != null){
                            DatabaseReference driverRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(driverID).child("CustomerRideId");
                            driverRef.removeValue();
                            driverID = null;

                        }
                        requestBol = false;
                        driverFound = false;
                        bankinh = 1;
                        String userID = FirebaseAuth.getInstance().getCurrentUser().getUid();
                        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("CustomerRequest").child(userID);
//                        GeoFire geoFire = new GeoFire(ref);
//                        geoFire.removeLocation(userID);
                        ref.removeValue();
                        if(khachMarker != null){
                            khachMarker.remove();
                        }

                        mRequest.setText("Goi xe");
                    }else {
                        requestBol = true;
                        VitriKhach = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
                        khachMarker = mMap.addMarker(new MarkerOptions().position(VitriKhach).title("Ban o day"));

                        String userID = FirebaseAuth.getInstance().getCurrentUser().getUid();

                        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("CustomerRequest").child(userID);
                        GeoFire geoFire = new GeoFire(ref);
                        ref.child("Distance").setValue(khoangcach);
                        ref.child("GiaTien").setValue(giatien);
                        ref.child("GoalAdress").setValue(getGoalAddress());
                        geoFire.setLocation("GoalLocation", new GeoLocation(goalPosition.latitude, goalPosition.longitude));
                        geoFire.setLocation("CustomerLocation", new GeoLocation(mLastLocation.getLatitude(), mLastLocation.getLongitude()));


                        mRequest.setText("Dang tim xe om....");
                        getClosestDriver();

                    }
                }else{
                    AlertDialog.Builder builder = new AlertDialog.Builder(CustomerMapActivity.this);
                    builder.setTitle("Mời bạn chọn điểm đến");
                    builder.setPositiveButton("Ô kê", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });
                    AlertDialog alertDialog = builder.create();
                    alertDialog.show();
                }

            }
        });
    }


    private int bankinh;
    private Boolean driverFound = false;
    private String driverID;

    GeoQuery geoQuery;
    private void getClosestDriver(){
        DatabaseReference driverLocation = FirebaseDatabase.getInstance().getReference().child("DriversAvailable");
        GeoFire geoFire = new GeoFire(driverLocation);
        geoQuery = geoFire.queryAtLocation(new GeoLocation(VitriKhach.latitude, VitriKhach.longitude), bankinh);
        geoQuery.removeAllListeners();
        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                if(!driverFound && requestBol){
                    driverFound = true;
                    driverID = key;


                    DatabaseReference driverRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(driverID);
                    String customerID = FirebaseAuth.getInstance().getCurrentUser().getUid();
                    HashMap map = new HashMap();
                    map.put("CustomerRideId", customerID);
                    driverRef.updateChildren(map);
                    getDriverLocation();
                    mRequest.setText("Dang lay vi tri cua xe om");

                }
            }

            @Override
            public void onKeyExited(String key) {

            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {

            }

            @Override
            public void onGeoQueryReady() {
                if(!driverFound){
                    bankinh++;
                    getClosestDriver();
                }
            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });
    }

    private Marker mDriverMaker;
    private DatabaseReference driverLocationRef;
    private ValueEventListener driverLocationRefListioner;
    private void getDriverLocation(){
        driverLocationRef = FirebaseDatabase.getInstance().getReference().child("DriversWorking").child(driverID).child("l");
        driverLocationRefListioner = driverLocationRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if(dataSnapshot.exists() && requestBol){
                        List<Object> map = (List<Object>) dataSnapshot.getValue();
                        double locationLat = 0;
                        double locationLng = 0;
                        mRequest.setText("Da tim thay xe");
                    if(map.get(0) != null){
                        locationLat = Double.parseDouble(map.get(0).toString());
                    }
                    if(map.get(1) != null){
                        locationLng = Double.parseDouble(map.get(1).toString());
                    }
                    LatLng driverLatLng = new LatLng(locationLat, locationLng);
                    if(mDriverMaker != null){
                        mDriverMaker.remove();
                    }
                    Location loc1 = new Location("");
                    loc1.setLatitude(VitriKhach.latitude);
                    loc1.setLongitude(VitriKhach.longitude);

                    Location loc2 = new Location("");
                    loc2.setLatitude(driverLatLng.latitude);
                    loc2.setLongitude(driverLatLng.longitude);

                    float distance = loc1.distanceTo(loc2);
                    if(distance<100){
                        mRequest.setText("Xe om da den");
                        mDriverMaker = mMap.addMarker(new MarkerOptions().position(driverLatLng).title("Xe om cua ban").icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_motobike_foreground)));
                    }else {
                        mRequest.setText("Da tim thay xe: "+String.valueOf(distance));
                        mDriverMaker = mMap.addMarker(new MarkerOptions().position(driverLatLng).title("Xe om cua ban").icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_motobike_foreground)));
                    }
                    getDriverInfor(driverID);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void getDriverInfor(String driverID){
        DatabaseReference driverInfor = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(driverID);
        driverInfor.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                HashMap infor = (HashMap) dataSnapshot.getValue();
                driverName = infor.get("Name").toString();
                driverSdt = infor.get("Sdt").toString();
                getAllInfor();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,
                                Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.INTERNET}
                        , 10);
            }
            return;
        }
//        buildGoogleApiClient();
        mMap.setMyLocationEnabled(true);
        mLocationManager.requestLocationUpdates("gps", 5000, 0, mLocationListener);

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                if(goalMarker != null){
                    goalMarker.remove();
                }
                chooseGoal = true;
                goalPosition = latLng;
                goalMarker = mMap.addMarker(new MarkerOptions().position(latLng).title("Diem den"));
                displayGoalInfor();
            }
        });
    }

    private void displayGoalInfor(){
        VitriKhach = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
        Location loc1 = new Location(""), loc2 = new Location("");
        loc1.setLatitude(VitriKhach.latitude);
        loc1.setLongitude(VitriKhach.longitude);
        loc2.setLatitude(goalPosition.latitude);
        loc2.setLongitude(goalPosition.longitude);
        int distance = (int) loc1.distanceTo(loc2);
        khoangcach = Integer.toString(distance)+"m";
        giatien = Integer.toString(distance*15)+" VND";
        goalAdress = getGoalAddress();
        builder.setTitle("Thông tin chi tiết");
        builder.setMessage("Muốn đến: "+goalAdress+"\n\nKhoảng cách: "+khoangcach+"\n\nGiá tiền: "+giatien+" VND");
        builder.setNegativeButton("Xong", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        alertDialog = builder.create();
        alertDialog.show();
    }
    private void getAllInfor(){
        builder2.setTitle("Thông tin chi tiết");
        builder2.setMessage("Xe om: " + driverName + "\n\nSDT: " + driverSdt + "\n\nMuốn đến: "+goalAdress+"\n\nKhoảng cách: "+khoangcach+"\n\nGiá tiền: "+giatien+" VND");
        builder2.setNegativeButton("Xong", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder2.setPositiveButton("Goi", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent callDriver = new Intent(Intent.ACTION_CALL);
                callDriver.setData(Uri.parse("tel:"+driverSdt));
                startActivity(callDriver);
            }
        });
        alertDialog2 = builder2.create();
        mThongtin.setVisibility(View.VISIBLE);
    }
    private String getGoalAddress(){
        Locale locale = new Locale("vi_VN");
        Locale.setDefault(locale);
        Geocoder geocode = new Geocoder(CustomerMapActivity.this, locale.getDefault());
        List<Address> addresses = null;
        String address = " ";
        try {
            addresses = geocode.getFromLocation(goalPosition.latitude, goalPosition.longitude,1);
            address = addresses.get(0).getAddressLine(0);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return address;
    }

    @Override
    protected void onStop() {
        super.onStop();
    }
}
