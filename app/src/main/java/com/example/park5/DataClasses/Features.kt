package com.example.retrotry.network

import com.example.park5.DataClasses.Properties
import com.google.gson.annotations.SerializedName

data class Features (
    @SerializedName("type") val type : String,
    @SerializedName("id") val id : String,
    @SerializedName("geometry") var geometry : Geometry,
    @SerializedName("geometry_name") val geometry_name : String,
    @SerializedName("properties") val properties : Properties
        )