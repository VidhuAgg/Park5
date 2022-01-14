package com.example.park5

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.location.Geocoder
import android.location.Location

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.drawerlayout.widget.DrawerLayout
import com.example.park5.Interface.GetInterface
import com.example.park5.Objects.GetObject

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.example.park5.databinding.ActivityMapsBinding
import com.example.retrotry.network.Get
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.maps.model.*
import kotlinx.coroutines.*
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView
import java.util.*
import kotlin.collections.ArrayList
import android.app.ProgressDialog
import android.widget.ProgressBar


class MapsActivity : AppCompatActivity(), OnMapReadyCallback,
    NavigationView.OnNavigationItemSelectedListener, GoogleMap.OnMarkerClickListener {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private lateinit var draw: DrawerLayout
    //private val mapView: View? = null
    //private val TAG: String = MapsActivity::class.java.simpleName
    //private val GOOGLEMAP_COMPASS = "GoogleMapCompass"
    private var currentLatLng:LatLng = LatLng(59.4023,17.9457)

    //google's API for location services. Very important
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    //lateinit var locrequest: LocationRequest
    private lateinit var lastLocation: Location

    //list for storing latlang of parking spots
    private var places = ArrayList<ArrayList<Double>>()

    //for location updates
    private lateinit var locationCallback: LocationCallback
    private val locationRequest = LocationRequest()
    private var locationUpdateState = false

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
        private const val REQUEST_CHECK_SETTINGS = 2
        private const val PARKING_API_KEY = "693167d9-7ce5-437a-90fd-030343a3bacf"
    }

    private fun findPos(callback: (ArrayList<ArrayList<Double>>?) -> Unit) {
        val service = GetObject.retrofitInstance?.create(GetInterface::class.java)
        val call = service?.getPost(100,59.4023,17.9457,"json",
            PARKING_API_KEY)
        call?.enqueue(object : Callback<Get> {
            override fun onResponse(call: Call<Get>, response: Response<Get>) {
                var body = response.body()
                body?.features?.forEach {
                    places = it.geometry.coordinates
                    callback(places)
                }
            }
            override fun onFailure(call: Call<Get>, t: Throwable) {
                Toast.makeText(applicationContext, "Error reading JSON", Toast.LENGTH_LONG).show()
            }
        })
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //@TODO(Might remove this)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(findViewById(R.id.toolbar))
        draw = findViewById<DrawerLayout>(R.id.drawer_layout)

        supportActionBar?.setDisplayShowTitleEnabled(false)

        val navigationView = findViewById<NavigationView>(R.id.nav_view)
        navigationView.setNavigationItemSelectedListener(this)

        val toggle = ActionBarDrawerToggle(
            this,
            draw,
            findViewById(R.id.toolbar),
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        draw.addDrawerListener(toggle)
        toggle.syncState()

        //clicking on park5 logo on top goes back to main screen, basically creates a new activity
        val park5logo = findViewById<ImageView>(R.id.logocenter)
        park5logo.setOnClickListener {
            val intent = Intent(this, MapsActivity::class.java)
            startActivity(intent)
        }

        //getting map fragment & Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as? SupportMapFragment
        mapFragment?.getMapAsync(this)

        val bottom = findViewById<BottomNavigationView>(R.id.bottom_nav)
        bottom.itemIconTintList = null
        bottom.isHapticFeedbackEnabled = true
        bottom.menu.getItem(2).isChecked = false
        bottom.setOnNavigationItemSelectedListener(bottomNavItemSelected)

        //for the center button
        // @TODO fix this
        val front = findViewById<ImageView>(R.id.tofront)
        front.setOnClickListener {
            // @TODO add scan fragment
            Toast.makeText(applicationContext, "Parking", Toast.LENGTH_LONG).show()
        }

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(p0: LocationResult) {
                super.onLocationResult(p0)
                lastLocation = p0.lastLocation
            }
        }

        if (savedInstanceState == null) {
            //supportFragmentManager.beginTransaction().show(mapFragment).remove(supportFragmentManager
            //  .findFragmentById(R.id.fragment_container) as SupportMapFragment).commit()
            //supportFragmentManager.beginTransaction().replace(R.id.fragment_container,mapFragment).commit()
            //supportFragmentManager.beginTransaction().remove(mapFragment).commit()
            //supportFragmentManager.executePendingTransactions()
            //supportFragmentManager.beginTransaction().replace(R.id.fragment_container,mapFragment).commit()
            //navigationView.setCheckedItem(R.id.map)
        }
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
    }// End of OnCreate

    private fun startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE)
            return
        }
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null /* Looper */)
    }

    private fun createLocationRequest() {
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
                    e.startResolutionForResult(this@MapsActivity,
                        REQUEST_CHECK_SETTINGS)
                } catch (sendEx: IntentSender.SendIntentException) {
                    // Ignore the error.
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CHECK_SETTINGS) {
            if (resultCode == Activity.RESULT_OK) {
                locationUpdateState = true
                startLocationUpdates()
            }
        }
    }
    private val bottomNavItemSelected = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.nearme -> {
                //item.isCheckable = true //here is the magic
                findPos { result ->
                    if (result != null) {
                        for (item in result) {
                            placeMarkerOnMap(LatLng(item[1], item[0]))
                        }
                    }
                }
                return@OnNavigationItemSelectedListener true
            }
            R.id.pay -> {
                //item.isCheckable = true
                replaceFragment(Fragment_pay())
                return@OnNavigationItemSelectedListener true
            }
            R.id.park->{
                //item.isCheckable = true
                return@OnNavigationItemSelectedListener true
            }
            R.id.savedLoc -> {
                replaceFragment(Fragment_SavedLoc())
                return@OnNavigationItemSelectedListener true
            }
            R.id.report -> {
                //item.isCheckable = true
                replaceFragment(Fragment_ReportError())
                return@OnNavigationItemSelectedListener true
            }
        }
        false
    }

    override fun onPause() {
        super.onPause()
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    public override fun onResume() {
        super.onResume()
        if (!locationUpdateState) {
            startLocationUpdates()
        }
    }

    private fun setUpMap() {
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST_CODE)
            return
        }

        mMap.isMyLocationEnabled = true

        fusedLocationClient.lastLocation.addOnSuccessListener(this) { location ->
            // Got last known location. In some rare situations this can be null.
            if (location != null) {
                lastLocation = location
                currentLatLng = LatLng(location.latitude, location.longitude)
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 12f))
            }
        }
    }

    private fun placeMarkerOnMap(location: LatLng) {
        val markerOptions = MarkerOptions().position(location).icon(
            bitmapDescriptorFromVector(
                applicationContext,
                R.drawable.ic_parksymbol
            ))
        //@TODO("Find better values for anchor, responsible for weird marker positioning in different zoom levels")
        //markerOptions.anchor(0.3F,0.0F)
        // markerOptions.anchor(0.0F, 0.0f)
        val geocoder = Geocoder(this, Locale.getDefault())
        val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
        markerOptions.title("Spots:3").snippet(addresses[0].getAddressLine(0).toString())
        mMap.addMarker(markerOptions)
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 15F))
    }

    private fun bitmapDescriptorFromVector(context: Context, vectorResId: Int): BitmapDescriptor? {
        return ContextCompat.getDrawable(context, vectorResId)?.run {
            setBounds(0, 0, intrinsicWidth, intrinsicHeight)
            val bitmap =
                Bitmap.createBitmap(intrinsicWidth, intrinsicHeight, Bitmap.Config.ARGB_8888)
            draw(Canvas(bitmap))
            BitmapDescriptorFactory.fromBitmap(bitmap)
        }
    }

    override fun onBackPressed() {
        if (draw.isDrawerOpen(GravityCompat.START)) {
            draw.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
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
        mMap.uiSettings.isCompassEnabled = true
        mMap.uiSettings.isMyLocationButtonEnabled = true
        mMap.uiSettings.isMapToolbarEnabled = true
        mMap.uiSettings.isZoomControlsEnabled = true
        mMap.setOnMarkerClickListener(this)
        mMap.setPadding(0,250,0,180)
        setUpMap()
    }

    //function for selecting fragments in navigation drawer
    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.nav_account) {
            replaceFragment(Fragment_Myaccount())
        } else {
            if (item.itemId == R.id.nav_help) {
                replaceFragment(Fragment_help())
            } else {
                if (item.itemId == R.id.nav_history) {
                    replaceFragment(Fragment_history())
                } else {
                    if (item.itemId == R.id.nav_settings) {
                        replaceFragment(Fragment_settings())
                    } else {
                        if (item.itemId == R.id.nav_support) {
                            replaceFragment(Fragment_support())
                        }
                    }
                }
            }
        }
        draw.closeDrawer(GravityCompat.START)
        return true
    }

    private fun replaceFragment(fragment: Fragment){
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment).commit()
    }

    override fun onMarkerClick(p0: Marker?) = false
}


