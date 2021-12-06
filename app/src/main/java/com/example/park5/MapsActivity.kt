package com.example.park5

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
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
import com.google.android.gms.location.*
import com.google.android.gms.maps.model.*
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.material.bottomnavigation.BottomNavigationMenu
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.*

class MapsActivity : AppCompatActivity(), OnMapReadyCallback,
    NavigationView.OnNavigationItemSelectedListener, GoogleMap.OnMarkerClickListener {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private lateinit var draw: DrawerLayout

    //google's API for location services. Very important
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    //lateinit var locrequest: LocationRequest
    private lateinit var lastLocation: Location

    //list for storing latlang of parking spots
    private var places = ArrayList<ArrayList<Double>>()

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
    }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(findViewById(R.id.toolbar))
        draw = findViewById<DrawerLayout>(R.id.drawer_layout)

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

        //getting map fragment & Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as? SupportMapFragment
        mapFragment?.getMapAsync(this)

        //set properties of locationrequest
        //locrequest.setInterval(1000 * 30) //change 30 & 5 to extract constants
        //locrequest.setFastestInterval(1000 * 5)
        //locrequest.setPriority(102)


        val bottomnav_left = findViewById<BottomNavigationView>(R.id.bottom_nav_left)
        val bottomnav_right = findViewById<BottomNavigationView>(R.id.bottom_nav_right)
        bottomnav_left.menu.getItem(0).isCheckable = false
        bottomnav_right.menu.getItem(0).isCheckable = false


        //for the center button
        // @TODO fix this
        //bottomnav.itemIconTintList = null

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
                        mMap.addMarker(
                            MarkerOptions().position(LatLng(item[1], item[0])).title("here").icon(
                                bitmapDescriptorFromVector(
                                    applicationContext,
                                    R.drawable.ic_parksymbol
                                )
                            )
                        )
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
    }

    private fun setUpMap() {
        if (ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST_CODE)
            return
        }
        // 1
        mMap.isMyLocationEnabled = true

        // 2
        fusedLocationClient.lastLocation.addOnSuccessListener(this) { location ->
            // Got last known location. In some rare situations this can be null.
            // 3
            if (location != null) {
                lastLocation = location
                val currentLatLng = LatLng(location.latitude, location.longitude)
                placeMarkerOnMap(currentLatLng)
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 12f))
            }
        }
    }


    private fun placeMarkerOnMap(location: LatLng) {
        // 1
        val markerOptions = MarkerOptions().position(location)
        // 2
        mMap.addMarker(markerOptions)
    }


    fun findPos(callback: (ArrayList<ArrayList<Double>>?) -> Unit) {
        val service = GetObject.retrofitInstance?.create(GetInterface::class.java)
        val call = service?.getPost()
        call?.enqueue(object : Callback<Get> {
            override fun onResponse(call: Call<Get>, response: Response<Get>) {
                var body = response.body()
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
        mMap.getUiSettings().setZoomControlsEnabled(true)
        mMap.setOnMarkerClickListener(this)

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

