package com.skillix.merchant;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.skillix.merchant.utils.ToastUtils;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class GoogleMapFragment extends Fragment {

    private FusedLocationProviderClient fusedLocationProviderClient;
    private GoogleMap googleMapInstance;
    private Marker userMarker;
    private double selectedLatitude;
    private double selectedLongitude;
    private String selectedAddress;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_google_map, container, false);

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireActivity());

        SupportMapFragment supportMapFragment = new SupportMapFragment();
        FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(R.id.mapFrameLayout, supportMapFragment);
        fragmentTransaction.commit();

        supportMapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(@NonNull GoogleMap googleMap) {
                Log.i("SkilliXLog", "Map Ready");

                googleMap.getUiSettings().setCompassEnabled(true);
                googleMap.getUiSettings().setZoomControlsEnabled(true);
                googleMapInstance = googleMap;

                if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    googleMap.setMyLocationEnabled(true);
                } else {
                    Log.i("SkilliXLog", "ACCESS_FINE_LOCATION PERMISSION DENIED!");
                    ToastUtils.showWarningToast(getActivity(), "ACCESS_FINE_LOCATION PERMISSION DENIED!");
                    requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 200);
                }

                // Handle map touch event
                googleMapInstance.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                    @Override
                    public void onMapClick(LatLng latLng) {
                        // Remove previous marker if exists
                        if (userMarker != null) {
                            userMarker.remove();
                        }

                        // Add new marker at tapped position
                        userMarker = googleMapInstance.addMarker(new MarkerOptions()
                                .position(latLng)
                                .title("Selected Location"));

                        selectedLatitude = latLng.latitude;
                        selectedLongitude = latLng.longitude;

                        // Optionally, use Geocoder to fetch the address
                        getAddress(selectedLatitude, selectedLongitude);
                    }
                });
            }
        });

        // Handle "OK" button click
        Button okButton = view.findViewById(R.id.okButton);
        okButton.setOnClickListener(v -> {
            // Send the location data back to the calling fragment/activity
            if (selectedLatitude != 0 && selectedLongitude != 0) {
                Bundle result = new Bundle();
                result.putDouble("latitude", selectedLatitude);
                result.putDouble("longitude", selectedLongitude);
                result.putString("address", selectedAddress);
                getParentFragmentManager().setFragmentResult("locationData", result);

                // Go back to the previous screen
                requireActivity().getSupportFragmentManager().popBackStack();
            } else {
                Toast.makeText(getContext(), "Please select a location", Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }

    private void getAddress(double latitude, double longitude) {
        Geocoder geocoder = new Geocoder(requireContext(), Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                selectedAddress = address.getAddressLine(0);
            } else {
                Log.i("SkilliXLog", "No address found.");
                ToastUtils.showErrorToast(getActivity(), "Address not found. Try again.");
            }
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("SkilliXLog", "Error getting address: " + e.getMessage());
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 200 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Log.i("SkilliXLog", "ACCESS_FINE_LOCATION PERMISSION GRANTED!");
        } else {
            ToastUtils.showErrorToast(getActivity(), "Permission denied, location features may not work.");
        }
    }
}

