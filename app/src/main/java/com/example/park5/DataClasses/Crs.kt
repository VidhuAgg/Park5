package com.example.retrotry.network

import com.example.park5.DataClasses.Properties
import com.google.gson.annotations.SerializedName

data class Crs (
    @SerializedName("type") val type : String,
    @SerializedName("properties") val properties : Properties
)