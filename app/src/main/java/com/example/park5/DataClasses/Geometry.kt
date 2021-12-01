package com.example.retrotry.network

import com.google.android.gms.maps.model.LatLng
import com.google.gson.annotations.SerializedName

data class Geometry (

    @SerializedName("type") val type : String,
    @SerializedName("coordinates") var coordinates : ArrayList<ArrayList<Double>>
)