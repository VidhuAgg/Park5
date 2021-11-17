package com.example.retrotry.network

import com.google.gson.annotations.SerializedName

data class Get(
    @SerializedName("type") val type : String,
    @SerializedName("features") val features : List<Features>,
    @SerializedName("totalFeatures") val totalFeatures : Int,
    @SerializedName("numberMatched") val numberMatched : Int,
    @SerializedName("numberReturned") val numberReturned : Int,
    @SerializedName("timeStamp") val timeStamp : String,
    @SerializedName("crs") val crs : Crs

)