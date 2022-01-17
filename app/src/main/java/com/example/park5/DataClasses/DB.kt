package com.example.park5.DataClasses

import com.google.firebase.database.PropertyName
import com.google.gson.annotations.SerializedName

data class DB (
    @PropertyName("features") val spotData:List<Information>
        )
