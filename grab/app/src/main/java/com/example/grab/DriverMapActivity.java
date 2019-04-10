package com.example.grab;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Service;
import android.bluetooth.BluetoothClass;
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
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.text.Layout;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewDebug;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.security.Provider;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DriverMapActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private LocationManager mLocationManager;
    private LocationListener mLocationListener;
    private Location mLastLocation;
    private Button mlogout, mThongtin;
    private EditText mSearchBox;
    private String customerID = "";
    private boolean isLogout = false, thanhtoan = false;
    private String goalAdress, tenKhach = "", sdtKhach = "";
    AlertDialog.Builder builder, builder2;
    AlertDialog alertDialog, alertDialog2;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_map);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        builder = new AlertDialog.Builder(DriverMapActivity.this);
        builder2 = new AlertDialog.Builder(DriverMapActivity.this);
        Toast.makeText(this, "Dang nhap thanh cong", Toast.LENGTH_LONG).show();
        mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            return;
        }
//        mLastLocation = mLocationManager.getLastKnownLocation("gps");
//        String userID = FirebaseAuth.getInstance().getCurrentUser().getUid();
//        DatabaseReference refAvailable = FirebaseDatabase.getInstance().getReference().child("DriversAvailable");
//        GeoFire geoFireAvailable = new GeoFire(refAvailable);
//        geoFireAvailable.setLocation(userID, new GeoLocation(mLastLocation.getLatitude(), mLastLocation.getLongitude()));

        mLocationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
//                mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
//                mMap.animateCamera(CameraUpdateFactory.zoomTo(15));

                String userID = FirebaseAuth.getInstance().getCurrentUser().getUid();
                DatabaseReference refAvailable = FirebaseDatabase.getInstance().getReference().child("DriversAvailable");
                DatabaseReference refWorking = FirebaseDatabase.getInstance().getReference().child("DriversWorking");
                GeoFire geoFireAvailable = new GeoFire(refAvailable);
                GeoFire geoFireWorking = new GeoFire(refWorking);

                switch (customerID) {
                    case "":
                        geoFireWorking.removeLocation(userID);
                        geoFireAvailable.setLocation(userID, new GeoLocation(location.getLatitude(), location.getLongitude()));
                        break;

                    default:
                        geoFireAvailable.removeLocation(userID);
                        geoFireWorking.setLocation(userID, new GeoLocation(location.getLatitude(), location.getLongitude()));
                        break;
                }

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

        getAssignedCustomer();

        mlogout = (Button) findViewById(R.id.logout);
        mThongtin = (Button) findViewById(R.id.thongtindatxe);
        mSearchBox = (EditText) findViewById(R.id.searchBox);

        mSearchBox.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_ACTION_DONE || event.getAction() == KeyEvent.ACTION_DOWN || event.getAction() == KeyEvent.KEYCODE_ENTER) {
                    String searchStr = mSearchBox.getText().toString();
                    Geocoder geoCoder = new Geocoder(DriverMapActivity.this);
                    List<Address> list = new ArrayList<>();
                    try {
                        list = geoCoder.getFromLocationName(searchStr, 1);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    if (list.size() > 0) {
                        Address address = list.get(0);
                        Log.d("address", "onEditorAction: " + address.toString());
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(address.getLatitude(), address.getLongitude()), 14));
                    }
                }
                return false;
            }
        });
        mlogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isLogout = true;
                disconnectDriver();
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(DriverMapActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
                return;
            }
        });
        mThongtin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.show();
            }
        });
    }

    private void getAssignedCustomer() {
        String driverID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference assignedCustomerRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(driverID).child("CustomerRideId");
        assignedCustomerRef.setValue("");
        assignedCustomerRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    customerID = dataSnapshot.getValue().toString();
                    getAssignedCustomerPickupLocation();
                } else {
                    customerID = "";
                    if (khachMarker != null) {
                        khachMarker.remove();
                    }
                    if (assignedCustomerPickupLocationRefListener != null) {
                        assignedCustomerPickupLocationRef.removeEventListener(assignedCustomerPickupLocationRefListener);
                    }
                    builder2.setTitle("Thong bao");
                    builder2.setMessage("Khach hang da huy chuyen");
                    builder2.setNegativeButton("Thoat", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });
                    alertDialog2 = builder2.create();
                    if (!thanhtoan) alertDialog2.show();
                    else {
                        AlertDialog.Builder hoanTatBuilder = new AlertDialog.Builder(DriverMapActivity.this);
                        hoanTatBuilder.setTitle("Thong bao");
                        hoanTatBuilder.setMessage("Chuyen di da hoan thanh");
                        hoanTatBuilder.setPositiveButton("Xong", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });
                        AlertDialog hoantat = hoanTatBuilder.create();
                        hoantat.show();
                        thanhtoan = false;
                    }
                    mThongtin.setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    Marker khachMarker;
    DatabaseReference assignedCustomerPickupLocationRef;
    ValueEventListener assignedCustomerPickupLocationRefListener;

    private void getAssignedCustomerPickupLocation() {
        assignedCustomerPickupLocationRef = FirebaseDatabase.getInstance().getReference().child("CustomerRequest").child(customerID).child("CustomerLocation").child("l");
        assignedCustomerPickupLocationRefListener = assignedCustomerPickupLocationRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists() && !customerID.equals("")) {
                    List<Object> map = (List<Object>) dataSnapshot.getValue();
                    double locationLat = 0;
                    double locationLng = 0;
                    if (map.get(0) != null) {
                        locationLat = Double.parseDouble(map.get(0).toString());
                    }
                    if (map.get(1) != null) {
                        locationLng = Double.parseDouble(map.get(1).toString());
                    }
                    LatLng driverLatLng = new LatLng(locationLat, locationLng);
                    getCustomerInfor();
                    khachMarker = mMap.addMarker(new MarkerOptions().position(driverLatLng).title("Khach o day"));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void getCustomerInfor() {
        DatabaseReference customerInforRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Customers").child(customerID);
        customerInforRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                HashMap infor = (HashMap) dataSnapshot.getValue();
                tenKhach = infor.get("Name").toString();
                sdtKhach = infor.get("Sdt").toString();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        getGoalInfor();
    }

    private void getGoalInfor() {
        DatabaseReference goalPositionRef = FirebaseDatabase.getInstance().getReference().child("CustomerRequest").child(customerID).child("GoalLocation").child("l");
        goalPositionRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    List<Object> infor = (List<Object>) dataSnapshot.getValue();
                    Double locationLat = Double.parseDouble(infor.get(0).toString());
                    Double locationLng = Double.parseDouble(infor.get(1).toString());
                    LatLng goalLatLng = new LatLng(locationLat, locationLng);
                    khachMarker = mMap.addMarker(new MarkerOptions().position(goalLatLng).title("Day la diem den"));
                } else {

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        final DatabaseReference goalInforRef = FirebaseDatabase.getInstance().getReference().child("CustomerRequest").child(customerID);
        goalInforRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    HashMap infor = (HashMap) dataSnapshot.getValue();
                    Log.d(Integer.toString(infor.size()), "onDataChange: " + Integer.toString(infor.size()));

                    if (infor.size() == 5) {
                        int i = 1;
                        String khoangcach = infor.get("Distance").toString();
                        goalAdress = infor.get("GoalAdress").toString();
                        String giatien = infor.get("GiaTien").toString();
                        builder.setTitle("Đã bắt được khách");
                        builder.setMessage("Ten khach: " + tenKhach + "\nSdt: " + sdtKhach + "\nKhoangcach: " + khoangcach + "\nGia tien: " + giatien + "\nMuốn đến: " + goalAdress);
                        builder.setNegativeButton("Thoat", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });
                        builder.setPositiveButton("Goi", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent call = new Intent(Intent.ACTION_CALL);
                                call.setData(Uri.parse("tel:" + sdtKhach));
                                startActivity(call);
                            }
                        });
                        builder.setNeutralButton("Xac nhan Thanh toan", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                AlertDialog.Builder xacnhan = new AlertDialog.Builder(DriverMapActivity.this);
                                xacnhan.setTitle("Bạn chắc chắn đã nhận tiền đầy đủ?");
                                xacnhan.setPositiveButton("Xác nhận", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        thanhtoan = true;
                                        String driverID = FirebaseAuth.getInstance().getCurrentUser().getUid();
                                        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(driverID).child("CustomerRideId");
                                        ref.removeValue();

                                        if (ActivityCompat.checkSelfPermission(DriverMapActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(DriverMapActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                                            // TODO: Consider calling
                                            return;
                                        }
                                        mLastLocation = mLocationManager.getLastKnownLocation("gps");
                                        String userID = FirebaseAuth.getInstance().getCurrentUser().getUid();
                                        DatabaseReference refAvailable = FirebaseDatabase.getInstance().getReference().child("DriversAvailable");
                                        GeoFire geoFireAvailable = new GeoFire(refAvailable);
                                        geoFireAvailable.setLocation(userID, new GeoLocation(mLastLocation.getLatitude(), mLastLocation.getLongitude()));

                                        dialog.cancel();
                                        alertDialog.cancel();
                                    }
                                });
                                xacnhan.setNegativeButton("Huy", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.cancel();
                                    }
                                });
                                AlertDialog xacnhanTT = xacnhan.create();
                                xacnhanTT.show();
                            }
                        });
                        alertDialog = builder.create();

                        alertDialog.show();
                        mThongtin.setVisibility(View.VISIBLE);
                        goalInforRef.removeValue();
                    }
                }
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
        mMap.setMyLocationEnabled(true);
        mLocationManager.requestLocationUpdates("gps", 5000, 0, mLocationListener);

    }

    private void disconnectDriver(){
        mLocationManager.removeUpdates(mLocationListener);
        String userID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference refAvailable = FirebaseDatabase.getInstance().getReference().child("DriversAvailable");
        GeoFire geoFireAvailable = new GeoFire(refAvailable);
        geoFireAvailable.removeLocation(userID);

        DatabaseReference refWorking = FirebaseDatabase.getInstance().getReference().child("DriversWorking");

        GeoFire geoFireWorking = new GeoFire(refWorking);
        geoFireWorking.removeLocation(userID);

        DatabaseReference customerRideId = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(userID).child("CustomerRideId");
        customerRideId.removeValue();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(!isLogout){
//            disconnectDriver();
            isLogout = true;
        }
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        if(!isLogout){
            disconnectDriver();
        }
    }
}
