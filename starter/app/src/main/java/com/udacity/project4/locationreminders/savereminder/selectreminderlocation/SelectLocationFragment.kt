package com.udacity.project4.locationreminders.savereminder.selectreminderlocation

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Geocoder
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.snackbar.Snackbar


import android.os.Bundle
import android.view.*
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.findNavController
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PointOfInterest
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.databinding.FragmentSelectLocationBinding
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import org.koin.android.ext.android.inject
import java.util.Locale

class SelectLocationFragment : BaseFragment(), OnMapReadyCallback {

    // Use Koin to get the view model of the SaveReminder
    override val _viewModel: SaveReminderViewModel by inject()
    private lateinit var binding: FragmentSelectLocationBinding

    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private var googleMap : GoogleMap? = null
    private var currentPOI: PointOfInterest? = null
    private var marker: Marker? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        val layoutId = R.layout.fragment_select_location
        binding = DataBindingUtil.inflate(inflater, layoutId, container, false)

        binding.viewModel = _viewModel
        binding.lifecycleOwner = this

        setHasOptionsMenu(true)
        setDisplayHomeAsUpEnabled(true)
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireContext())

        // TODO: add the map setup implementation
        var mapFragment = childFragmentManager.findFragmentById(R.id.map_fragment) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // TODO: call this function after the user confirms on the selected location
        binding.saveButton.setOnClickListener {
            onLocationSelected()
        }
        return binding.root
    }
    @SuppressLint("MissingPermission")
    private fun zoonToUserLocation(){
        val request = LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_LOW_POWER
        }
        val builder = LocationSettingsRequest.Builder().addLocationRequest(request)
        val settingClient = LocationServices.getSettingsClient(requireActivity())
        val locationSettingsResponse = settingClient.checkLocationSettings(builder.build())
        locationSettingsResponse.addOnCompleteListener {
            if (it.isSuccessful) {
                fusedLocationProviderClient.lastLocation
                    .addOnSuccessListener {
                        if (it != null) {
                            googleMap?.moveCamera(
                                CameraUpdateFactory.newLatLngZoom(
                                    LatLng(
                                        it.latitude,
                                        it.longitude
                                    ), 15f
                                )
                            )
                        }
                    }
                    .addOnFailureListener { e ->
                        Log.i("Location", "Error: " + e.localizedMessage)
                        Snackbar.make(
                            binding.root,
                            R.string.error_location_permission,
                            Snackbar.LENGTH_SHORT
                        ).setAction("OK") {
                            zoonToUserLocation()
                        }
                            .show()
                    }
            }
        }

    }
    @SuppressLint("MissingPermission")
    private val locationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // Precise location access granted.
            googleMap?.isMyLocationEnabled = true
        } else {
            // No location access granted.
            Snackbar.make(
                requireView(),
                R.string.error_location_permission,
                Snackbar.LENGTH_SHORT
            ).show()
        }

    }
    @SuppressLint("MissingPermission")
    private fun checkLocationPermissions(): Boolean {
        return if (isPermissionGranted()) {
            true
        } else {
            //TODO: Request Location permissions
            locationPermissionLauncher.launch(
                Manifest.permission.ACCESS_FINE_LOCATION)
            false
        }
    }
    private fun isPermissionGranted(): Boolean {
        //TODO: Check if permission is already granted and return (true = granted, false = denied/other)
        return (ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED)

    }

    private fun onLocationSelected() {
        // TODO: When the user confirms on the selected location,
        //  send back the selected location details to the view model
        //  and navigate back to the previous fragment to save the reminder and add the geofence
        currentPOI?.let {
            _viewModel.reminderSelectedLocationStr.value = it.name
            _viewModel.latitude.value = it.latLng.latitude
            _viewModel.longitude.value = it.latLng.longitude
        }
        findNavController().popBackStack()

    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.map_options, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        // TODO: Change the map type based on the user's selection.
        R.id.normal_map -> {
            googleMap?.mapType = GoogleMap.MAP_TYPE_NORMAL
            true
        }
        R.id.hybrid_map -> {
            googleMap?.mapType = GoogleMap.MAP_TYPE_HYBRID
            true
        }
        R.id.satellite_map -> {
            googleMap?.mapType = GoogleMap.MAP_TYPE_SATELLITE
            true
        }
        R.id.terrain_map -> {
            googleMap?.mapType = GoogleMap.MAP_TYPE_TERRAIN
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    override fun onMapReady(p0: GoogleMap) {
        googleMap = p0

        // TODO: zoom to the user location after taking his permission
        if(checkLocationPermissions()) {
            zoonToUserLocation()
        }

        // TODO: add style to the map
        googleMap?.setMapStyle(MapStyleOptions.loadRawResourceStyle(requireContext(), R.raw.map_style))

        // TODO: put a marker to location that the user selected
        setMapClick(googleMap!!)
        setPoiClick(googleMap!!)
    }

    private fun setMapClick(map: GoogleMap) {
        map.setOnMapClickListener { latLng ->
            // A Snippet is Additional text that's displayed below the title.
            var  address = Geocoder(requireContext(), Locale.getDefault()).getFromLocation(latLng.latitude, latLng.longitude, 1)
            val poi =
                address!![0]?.thoroughfare
                    ?.let { PointOfInterest(latLng, null.toString(), it) }

            marker?.remove()
            val mapMarker = map.addMarker(
                MarkerOptions()
                    .position(latLng)
            )
            mapMarker?.showInfoWindow()
            marker = mapMarker
            currentPOI = poi
        }
    }

    private fun setPoiClick(map: GoogleMap) {
        map.setOnPoiClickListener { poi ->
            marker?.remove()
            val poiMarker = map.addMarker(
                MarkerOptions()
                    .position(poi.latLng)
                    .title(poi.name)
            )
            poiMarker?.showInfoWindow()
            currentPOI = poi
            marker = poiMarker
        }
    }
}