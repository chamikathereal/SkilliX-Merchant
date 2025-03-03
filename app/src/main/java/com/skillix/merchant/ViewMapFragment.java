package com.skillix.merchant;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;


public class ViewMapFragment extends Fragment {

    private double latitude;
    private double longitude;
    private String mentorFullName;
    private Marker marker;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_view_map, container, false);

        SupportMapFragment supportMapFragment = new SupportMapFragment();
        FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.viewMapFrameLayout, supportMapFragment);
        fragmentTransaction.commit();

        if (getArguments() != null) {
            latitude = getArguments().getDouble("latitude", 0.0);
            longitude = getArguments().getDouble("longitude", 0.0);
            mentorFullName = getArguments().getString("mentorFullName");

            Log.i("SkilliXLog", "Latitude: " + latitude);
            Log.i("SkilliXLog", "Longitude: " + longitude);

            if (latitude != 0.0 && longitude != 0.0) {
                Log.i("SkilliXLog", "Latitude: " + latitude);
                Log.i("SkilliXLog", "Longitude: " + longitude);
            } else {
                Log.i("SkilliXLog", "Latitude or Longitude is invalid or missing!");
            }
        }

        supportMapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(@NonNull GoogleMap googleMap) {
                Log.i("SkilliXLog", "Map Ready");


                googleMap.getUiSettings().setCompassEnabled(true);
                googleMap.getUiSettings().setZoomControlsEnabled(true);

                LatLng latLng = new LatLng(latitude, longitude);

                googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));

                marker = googleMap.addMarker(new MarkerOptions()
                        .position(latLng)
                        .title(mentorFullName));

            }
        });

        return view;
    }

}