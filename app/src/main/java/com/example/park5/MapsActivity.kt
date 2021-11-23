package com.example.park5

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
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

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private lateinit var draw:DrawerLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        draw = findViewById<DrawerLayout>(R.id.drawer_layout)
        setSupportActionBar(findViewById(R.id.toolbar))

        val toggle = ActionBarDrawerToggle(this,draw,findViewById(R.id.toolbar),R.string.navigation_drawer_open,R.string.navigation_drawer_close)
        draw.addDrawerListener(toggle)
        toggle.syncState()

        //var drawer = findViewById(R.id.drawer_layout)

        // to read json output
        /*val x:Double = 0.0
        val y:Double = 0.0
        val service = GetObject.retrofitInstance?.create(GetInterface::class.java)
        val call = service?.getPost()
        call?.enqueue(object: Callback<Get> {
            override fun onResponse(call: Call<Get>, response: Response<Get>) {
                val body = response.body()
                body?.features?.forEach {
                    x = it.geometry.coordinates[0][0].toDouble()
                    y = it.geometry.coordinates[0][1].toDouble()
                    Log.d("test_X:",x.toString())
                }
            }

            override fun onFailure(call: Call<Get>, t: Throwable) {
                Toast.makeText(applicationContext,"Error reading JSON", Toast.LENGTH_LONG).show()
            }
        })*/
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
        mMap.addMarker(MarkerOptions().position(sydney).title("Marker in STOCKHOLM"))
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney))
    }
}