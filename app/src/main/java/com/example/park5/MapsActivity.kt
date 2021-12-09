package com.example.park5

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Build

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import android.widget.Toolbar
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
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.retrotry.network.Geometry
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.maps.model.*
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.material.bottomnavigation.BottomNavigationMenu
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.*
import java.io.IOException
import android.widget.RelativeLayout
import androidx.fragment.app.FragmentActivity
import java.lang.Exception

class MapsActivity : AppCompatActivity(), OnMapReadyCallback,
    NavigationView.OnNavigationItemSelectedListener, GoogleMap.OnMarkerClickListener {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private lateinit var draw: DrawerLayout
    private val mapView: View? = null
    private val TAG: String = MapsActivity::class.java.simpleName
    private val GOOGLEMAP_COMPASS = "GoogleMapCompass"
    lateinit var currentLatLng:LatLng

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
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(findViewById(R.id.toolbar))
        draw = findViewById<DrawerLayout>(R.id.drawer_layout)

        //supportActionBar?.setIcon(R.drawable.logo)
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
        val iv = findViewById<ImageView>(R.id.logocenter)
        iv.setOnClickListener(View.OnClickListener {
            val intent = Intent(this,MapsActivity::class.java)
            startActivity(intent)
        })



        //getting map fragment & Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as? SupportMapFragment
        mapFragment?.getMapAsync(this)


        val bottomnav = findViewById<BottomNavigationView>(R.id.bottom_nav)
        //val bottomnav_right = findViewById<BottomNavigationView>(R.id.bottom_nav_right)
        //bottomnav.menu.getItem(0).isCheckable = false
        //bottomnav_right.menu.getItem(0).isCheckable = false


        //for the center button
        // @TODO fix this
        bottomnav.itemIconTintList = null
        val front = findViewById<ImageView>(R.id.tofront)
        front.bringToFront()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(p0: LocationResult) {
                super.onLocationResult(p0)
                lastLocation = p0.lastLocation
            }
        }

        //bottomnav seletor
        BottomNavigationView.OnNavigationItemSelectedListener { item: MenuItem ->
            when (item.itemId) {

                R.id.nearme -> {
                    item.isCheckable = true //here is the magic

                    //notify the listener
                    return@OnNavigationItemSelectedListener true
                }
                R.id.pay -> {
                    item.isCheckable = true

                    //notify the listener
                    return@OnNavigationItemSelectedListener true
                }
                R.id.park -> {
                    //go to forgot user fragment
                    item.isCheckable = true

                    //notify the listener
                    return@OnNavigationItemSelectedListener true
                }
                R.id.savedLoc -> {
                    //go to forgot user fragment
                    item.isCheckable = true

                    //notify the listener
                    return@OnNavigationItemSelectedListener true
                }
                R.id.report -> {
                    //go to forgot user fragment
                    item.isCheckable = true

                    //notify the listener
                    return@OnNavigationItemSelectedListener true
                }

                else -> false
            }

        }

        //press button to find spots and add markers
        var fab: View = findViewById(R.id.parking)
        fab.setOnClickListener { view ->
            findPos() { result ->
                if (result != null) {
                    for (item in result) {
                        placeMarkerOnMap(LatLng(item[1], item[0]))
                    }
                }
                //mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(result, 15F))
                //mMap.animateCamera(CameraUpdateFactory.zoomIn());
                //mMap.animateCamera(CameraUpdateFactory.zoomTo(15F), 1000, null);
            }

            //@TODO("temporary to see if button is being pressed")
            Snackbar.make(view, "Finding parking spots", Snackbar.LENGTH_LONG)
                .setAction("Action", null)
                .show()
        }

        if (savedInstanceState == null) {
            //supportFragmentManager.beginTransaction().show(mapFragment).remove(supportFragmentManager
            //  .findFragmentById(R.id.fragment_container) as SupportMapFragment).commit()
            //supportFragmentManager.beginTransaction().replace(R.id.fragment_container,mapFragment).commit()
            //supportFragmentManager.beginTransaction().remove(mapFragment).commit()
            //supportFragmentManager.executePendingTransactions()
            //supportFragmentManager.beginTransaction().replace(R.id.fragment_container,mapFragment).commit()
            navigationView.setCheckedItem(R.id.map)
        }
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
    }// End of OnCreate

    private fun startLocationUpdates() {
        //1
        if (ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE)
            return
        }
        //2
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null /* Looper */)
    }

    private fun createLocationRequest() {
        // 2
        locationRequest.interval = 10000
        // 3
        locationRequest.fastestInterval = 5000
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY

        val builder = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)

        // 4
        val client = LocationServices.getSettingsClient(this)
        val task = client.checkLocationSettings(builder.build())

        // 5
        task.addOnSuccessListener {
            locationUpdateState = true
            startLocationUpdates()
        }
        task.addOnFailureListener { e ->
            // 6
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

    // 1
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CHECK_SETTINGS) {
            if (resultCode == Activity.RESULT_OK) {
                locationUpdateState = true
                startLocationUpdates()
            }
        }
    }

    // 2
    override fun onPause() {
        super.onPause()
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    // 3
    public override fun onResume() {
        super.onResume()
        if (!locationUpdateState) {
            startLocationUpdates()
        }
    }


    private fun setUpMap() {
        if (ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST_CODE)
            return
        }

        mMap.isMyLocationEnabled = true

        fusedLocationClient.lastLocation.addOnSuccessListener(this) { location ->
            // Got last known location. In some rare situations this can be null.
            if (location != null) {
                lastLocation = location
                currentLatLng = LatLng(location.latitude, location.longitude)
                //placeMarkerOnMap(currentLatLng)
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 12f))
            }
        }
    }
    private fun getAddress(latLng: LatLng): String {
        // 1
        val geocoder = Geocoder(this)
        val addresses: List<Address>?
        val address: Address?
        var addressText = ""

        try {
            // 2
            addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)
            // 3
            if (null != addresses && !addresses.isEmpty()) {
                address = addresses[0]
                for (i in 0 until address.maxAddressLineIndex) {
                    addressText += if (i == 0) address.getAddressLine(i) else "\n" + address.getAddressLine(i)
                }
            }
        } catch (e: IOException) {
            Log.e("MapsActivity", e.localizedMessage)
        }

        return addressText
    }


    private fun placeMarkerOnMap(location: LatLng) {
        var markerOptions = MarkerOptions().position(location).icon(
            bitmapDescriptorFromVector(
                applicationContext,
                R.drawable.ic_parksymbol
            ))
        //@TODO("Find better values for anchor, responsible for weird marker positioning in different zoom levels")
        //markerOptions.anchor(0.3F,0.0F)
        // markerOptions.anchor(0.0F, 0.0f)
        val titleStr = getAddress(location)  // add these two lines
        markerOptions.title("here")
        mMap.addMarker(markerOptions)

    }

    private fun findPos(callback: (ArrayList<ArrayList<Double>>?) -> Unit) {
        val service = GetObject.retrofitInstance?.create(GetInterface::class.java)
        val call = service?.getPost(100,currentLatLng.latitude,currentLatLng.longitude,"json","693167d9-7ce5-437a-90fd-030343a3bacf")
        call?.enqueue(object : Callback<Get> {
            override fun onResponse(call: Call<Get>, response: Response<Get>) {
                var body = response.body()
                Log.d("lat",currentLatLng.latitude.toString())
                Log.d("lon",currentLatLng.longitude.toString())
                Log.d("inside Reponse",body.toString())
                body?.features?.forEach {
                    places = it.geometry.coordinates
                    Log.d("prop!", it.properties.ADDRESS)
                    callback(places)
                }
            }

            override fun onFailure(call: Call<Get>, t: Throwable) {
                Toast.makeText(applicationContext, "Error reading JSON", Toast.LENGTH_LONG).show()
            }
        })
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
        mMap.setPadding(0,160,0,200);
        setUpMap()
    }

    //function for selecting fragments in navigation drawer
    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.nav_account) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, Fragment_Myaccount()).commit()
        } else {
            if (item.itemId == R.id.nav_help) {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, Fragment_help()).commit()
            } else {
                if (item.itemId == R.id.nav_history) {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, Fragment_history()).commit()
                } else {
                    if (item.itemId == R.id.nav_settings) {
                        supportFragmentManager.beginTransaction()
                            .replace(R.id.fragment_container, Fragment_settings()).commit()
                    } else {
                        if (item.itemId == R.id.nav_support) {
                            supportFragmentManager.beginTransaction()
                                .replace(R.id.fragment_container, Fragment_support()).commit()
                        }
                    }
                }
            }
        }

        draw.closeDrawer(GravityCompat.START)
        return true
    }

    override fun onMarkerClick(p0: Marker?) = false
}


