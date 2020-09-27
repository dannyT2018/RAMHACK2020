package com.example.ramhack2020

import android.content.Context
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.Location.distanceBetween
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.IOException

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var lastLocation: Location
    private lateinit var testLocation: LatLng
    private lateinit var closestCarMax: LatLng
    private var closestCarMaxDistance =  9999999.99999

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        setContentView(R.layout.activity_maps)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(p0: LocationResult) {
                super.onLocationResult(p0)
                lastLocation = p0.lastLocation
                placeMarkerOnMap(LatLng(lastLocation.latitude, lastLocation.longitude))
                testLocation = LatLng(lastLocation.latitude, lastLocation.longitude)
                findClosestCarMaxToMe(testLocation)
            }
        }
        createLocationRequest()
    }
    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
        private const val REQUEST_CHECK_SETTINGS = 2
    }

    private fun setUpMap() {
        // ensure the user has given permission to use current location feature
        if (ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
            return
        }

        mMap.isMyLocationEnabled = true

        fusedLocationClient.lastLocation.addOnSuccessListener(this) { location ->
            // Got last known location. In some rare situations this can be null.
            if (location != null) {
                lastLocation = location
                val currentLatLng = LatLng(location.latitude, location.longitude)
                testLocation = currentLatLng
                placeMarkerOnMap(currentLatLng)
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 12f))
            }
        }
    }

    private fun placeMarkerOnMap(location: LatLng) {
        val markerOptions = MarkerOptions().position(location)

        val titleStr = getAddress(location)
        markerOptions.title(titleStr)
        mMap.addMarker(markerOptions)
    }

    data class Distributor(val make: String, val model: String, val lat: Double, val lon: Double, val location: String, val color: String, val year: Int, val price: Int) {
    }

    private  fun findClosestCarMaxToMe(currentLatLng: LatLng) {
        try{
            Log.i("CurrentTestLocation", "${testLocation}")
        } catch (e: Exception) {
            Log.i("YEEEEEEEEEEEEEEEP", "FAILLLLLLLLLLLLLLLLLED")
        }

        var closestDistance = 99999.999
        var closestYear: Int
        var closestColor: String
        var pricing = 0
        var transferFee = 0
        val jsonFileString = getJsonDataFromAsset(applicationContext, "LatLonStorageAdjusted.json")
        Log.i("data", jsonFileString)

        val gson = Gson()
        val listPersonType = object : TypeToken<List<Distributor>>() {}.type

        var distributors: List<Distributor> = gson.fromJson(jsonFileString, listPersonType)
        distributors.forEachIndexed {
            // Loops to through each data point to compare the data with current location
                idx, distributor ->
            val locationA = Location("point A")
            locationA.latitude = currentLatLng.latitude
            locationA.longitude = currentLatLng.longitude
            val locationB = Location("point B")
            locationB.latitude = distributor.lat
            locationB.longitude = distributor.lon
            val distance = locationA.distanceTo(locationB).toDouble()
            if (distance < closestDistance) {
                closestCarMaxDistance = distance
                closestCarMax = currentLatLng
            }
        }
    }
    private fun findCarMaxToCarMax(closestCarMax: LatLng) {
        try{
            Log.i("CurrentTestLocation", "${testLocation}")
        } catch (e: Exception) {
            Log.i("YEEEEEEEEEEEEEEEP", "FAILLLLLLLLLLLLLLLLLED")
        }

        var closestDistance = 99999.999
        var closestYear: Int
        var closestColor: String
        var pricing = 0
        var transferFee = 0
        val jsonFileString = getJsonDataFromAsset(applicationContext, "LatLonStorageAdjusted.json")
        Log.i("data", jsonFileString)

        val gson = Gson()
        val listPersonType = object : TypeToken<List<Distributor>>() {}.type

        var distributors: List<Distributor> = gson.fromJson(jsonFileString, listPersonType)
        distributors.forEachIndexed {
            // Loops to through each data point to compare the data with current location
                idx, distributor ->
            val locationA = Location("point A")
            locationA.latitude = closestCarMax.latitude
            locationA.longitude = closestCarMax.longitude
            val locationB = Location("point B")
            locationB.latitude = distributor.lat
            locationB.longitude = distributor.lon
            val distance = locationA.distanceTo(locationB).toDouble()
            // Filter by car is needed HERE
            if (distance < closestDistance) {
                closestDistance = distance
                closestYear = distributor.year
                closestColor = distributor.color
                pricing = distributor.price
            }
        }

        if (closestDistance > 1500) {
            transferFee = 999
        } else if (closestDistance > 1000) {
            transferFee = 800
        } else if (closestDistance > 800) {
            transferFee = 600
        } else if (closestDistance > 400) {
            transferFee = 400
        } else if (closestDistance > 200) {
            transferFee = 200
        } else if (closestDistance > 100) {
            transferFee = 100
        } else {
            transferFee = 0
        }
    }

    private fun getJsonDataFromAsset(context: Context, fileName: String): String? {
        val jsonString: String

        try {
            jsonString = context.assets.open(fileName).bufferedReader().use { it.readText() }
        } catch (ioException: IOException) {
            ioException.printStackTrace()
            return null
        }
        return jsonString
    }

    private fun getAddress(latLng: LatLng): String {
        val geocode = Geocoder(this)
        val addresses: List<Address>?
        val address: Address?
        var addressText = ""

        try {
            addresses = geocode.getFromLocation(latLng.latitude, latLng.longitude, 1)
            if (null != addresses && addresses.isNotEmpty()) {
                address = addresses[0]
                for (i in 0 until address.maxAddressLineIndex) {
                    addressText += if (i == 0) address.getAddressLine(i) else "\n" + address.getAddressLine(
                        i
                    )
                }
            }
        } catch (e: IOException) {
            Log.e("MapActivity", e.localizedMessage)
        }
        return addressText
    }


    private fun startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
            return
        }
        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            null /* Looper */
        )
    }

    private lateinit var locationCallback: LocationCallback
    private lateinit var locationRequest: LocationRequest
    private var locationUpdateState = false

    // Setting rest API to retrieve user's location
    private fun createLocationRequest() {
        locationRequest = LocationRequest()
        locationRequest.interval = 10000
        locationRequest.fastestInterval = 5000
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY

        val builder = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)

        val client = LocationServices.getSettingsClient(this)
        val task = client.checkLocationSettings(builder.build())

        task.addOnSuccessListener {
            locationUpdateState = true
            startLocationUpdates()
        }
        task.addOnFailureListener { e ->
            if (e is ResolvableApiException) {
                // Location settings are not satisfied, but this can be fixed
                // by showing the user a dialog.
                try {
                    // Show the dialog by calling startResolutionForResult(),
                    // and check the result in onActivityResult().
                    e.startResolutionForResult(
                        this@MapsActivity,
                        REQUEST_CHECK_SETTINGS
                    )
                } catch (sendEx: IntentSender.SendIntentException) {
                    // Ignore the error.
                }
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
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.uiSettings.isZoomControlsEnabled = true

        setUpMap()
    }
}
