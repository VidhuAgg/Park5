package com.example.park5

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
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
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.example.park5.databinding.ActivityMapsBinding
import com.example.retrotry.network.Get
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.retrotry.network.Geometry
import com.google.android.material.bottomnavigation.BottomNavigationMenu
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.*

class MapsActivity : AppCompatActivity(), OnMapReadyCallback, NavigationView.OnNavigationItemSelectedListener{

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private lateinit var draw:DrawerLayout
    //var x:Double = 0.0
    //var y:Double = 0.0
    //list for storing latlang of parking spots
    private var places = ArrayList<ArrayList<Double>>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.


        setSupportActionBar(findViewById(R.id.toolbar))
        draw = findViewById<DrawerLayout>(R.id.drawer_layout)

        val navigationView = findViewById<NavigationView>(R.id.nav_view)
        navigationView.setNavigationItemSelectedListener(this)

        val toggle = ActionBarDrawerToggle(this,draw,findViewById(R.id.toolbar),R.string.navigation_drawer_open,R.string.navigation_drawer_close)
        draw.addDrawerListener(toggle)
        toggle.syncState()

        //getting map fragment
        val mapFragment = supportFragmentManager
        .findFragmentById(R.id.map) as? SupportMapFragment
        mapFragment?.getMapAsync(this)

        //to set buttons on navigation bar unchecked when starts
        val bottomnav = findViewById<BottomNavigationView>(R.id.bottom_nav)
        bottomnav.menu.getItem(0).isCheckable=false

        //val bottomnav_right = findViewById<BottomNavigationView>(R.id.bottom_nav_right)
        //bottomnav_right.menu.getItem(0).isCheckable=false

        //image in between two bottom views
        //val image = findViewById<ImageView>(R.id.parkcenter)
        //image.elevation = 10F
        bottomnav.itemIconTintList = null

        BottomNavigationView.OnNavigationItemSelectedListener { item: MenuItem ->
            when (item.itemId) {

                R.id.nearme -> {
                    item.isCheckable=true //here is the magic

                    //notify the listener
                    return@OnNavigationItemSelectedListener true
                }
                R.id.pay ->{
                    item.isCheckable=true

                    //notify the listener
                    return@OnNavigationItemSelectedListener true
                }
                R.id.park ->{
                    //go to forgot user fragment
                    item.isCheckable=true

                    //notify the listener
                    return@OnNavigationItemSelectedListener true
                }
                R.id.savedLoc ->{
                    //go to forgot user fragment
                    item.isCheckable=true

                    //notify the listener
                    return@OnNavigationItemSelectedListener true
                }
                R.id.report ->{
                    //go to forgot user fragment
                    item.isCheckable=true

                    //notify the listener
                    return@OnNavigationItemSelectedListener true
                }

                else -> false
            }


        }

        //press button to find spots and add markers

            var fab: View = findViewById(R.id.parking)
            fab.setOnClickListener { view ->
                var sp = LatLng(73.8567, 18.5204)
                    findPos(){result ->
                        Log.d("lol",result.toString())
                        mMap.addMarker(result?.let { MarkerOptions().position(it).title("Here") })
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(result, 15F))
                        mMap.animateCamera(CameraUpdateFactory.zoomIn());
                        mMap.animateCamera(CameraUpdateFactory.zoomTo(15F), 1000, null);
                        Log.d("letsee",result.toString())
                    }

                Snackbar.make(view, "Here's a Snackbar", Snackbar.LENGTH_LONG)
                    .setAction("Action", null)
                    .show()
        }




        //mapFragment.getMapAsync(this)
        if(savedInstanceState == null){
            //supportFragmentManager.beginTransaction().show(mapFragment).remove(supportFragmentManager
              //  .findFragmentById(R.id.fragment_container) as SupportMapFragment).commit()
            //supportFragmentManager.beginTransaction().replace(R.id.fragment_container,mapFragment).commit()
            //supportFragmentManager.beginTransaction().remove(mapFragment).commit()
            //supportFragmentManager.executePendingTransactions()
            //supportFragmentManager.beginTransaction().replace(R.id.fragment_container,mapFragment).commit()
            navigationView.setCheckedItem(R.id.map)
        }


        //


        //var drawer = findViewById(R.id.drawer_layout)

        // to read json output
        /**/
    }



    fun findPos(callback:(LatLng?)-> Unit){
        val service = GetObject.retrofitInstance?.create(GetInterface::class.java)
        val call = service?.getPost()
        call?.enqueue(object: Callback<Get> {
            override fun onResponse(call: Call<Get>, response: Response<Get>) {
                var body = response.body()
                body?.features?.forEach {
                    places = it.geometry.coordinates
                    var sp = LatLng(places[0][1],places[0][0])
                    callback(sp)
                }
            }
            override fun onFailure(call: Call<Get>, t: Throwable) {
                Toast.makeText(applicationContext,"Error reading JSON", Toast.LENGTH_LONG).show()
            }
        })
    }

    override fun onBackPressed() {
        if(draw.isDrawerOpen(GravityCompat.START)){
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
        // Add a marker in Sydney and move the camera
        val sydney = LatLng(59.3490990, 18.0968296)
        //mMap.addMarker(MarkerOptions().position(sydney).title("Marker in STOCKHOLM"))
        //mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney))
    }
    //function for selecting fragments in navigation drawer
    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        if(item.itemId == R.id.nav_account){
            supportFragmentManager.beginTransaction().replace(R.id.fragment_container,Fragment_Myaccount()).commit()
        } else {
            if(item.itemId == R.id.nav_help)
            {
                supportFragmentManager.beginTransaction().replace(R.id.fragment_container,Fragment_help()).commit()
            } else {
                if(item.itemId == R.id.nav_history){
                    supportFragmentManager.beginTransaction().replace(R.id.fragment_container,Fragment_history()).commit()
                } else {
                    if(item.itemId == R.id.nav_settings){
                        supportFragmentManager.beginTransaction().replace(R.id.fragment_container,Fragment_settings()).commit()
                    } else {
                        if(item.itemId == R.id.nav_support){
                            supportFragmentManager.beginTransaction().replace(R.id.fragment_container,Fragment_support()).commit()
                        }
                    }
                }
            }
        }

        draw.closeDrawer(GravityCompat.START)
        return true
    }
}

