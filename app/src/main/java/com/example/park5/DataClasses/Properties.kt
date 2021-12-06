package com.example.park5.DataClasses

import com.google.gson.annotations.SerializedName

data class Properties(
    @SerializedName("FID") val fID: Int,
    @SerializedName("FEATURE_OBJECT_ID") val FEATURE_OBJECT_ID: Int,
    @SerializedName("FEATURE_VERSION_ID") val FEATURE_VERSION_ID: Int,
    @SerializedName("EXTENT_NO") val EXTENT_NO: Int,
    @SerializedName("VALID_FROM") val VALID_FROM: String,
    @SerializedName("VEHICLE") val VEHICLE: String,
    @SerializedName("CITATION") val CITATION: String,
    @SerializedName("STREET_NAME") val STREET_NAME: String,
    @SerializedName("CITY_DISTRICT") val CITY_DISTRICT: String,
    @SerializedName("PARKING_DISTRICT") val PARKING_DISTRICT: String,
    @SerializedName("VF_METER") val VF_METER: Int,
    @SerializedName("VF_PLATS_TYP") val VF_PLATS_TYP: String,
    @SerializedName("ADDRESS") var ADDRESS: String,
    @SerializedName("RDT_URL") val RDT_URL: String,
    @SerializedName("PARKING_RATE") val PARKING_RATE: String
)