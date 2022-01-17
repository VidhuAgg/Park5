package com.example.park5.DataClasses

import com.google.firebase.database.PropertyName
import com.google.gson.annotations.SerializedName

data class Information(
    @PropertyName("Spots") var spots: Int = 0,
    @PropertyName("x") var x: Double = 0.0,
    @PropertyName("y") var y: Double = 0.0,
)

