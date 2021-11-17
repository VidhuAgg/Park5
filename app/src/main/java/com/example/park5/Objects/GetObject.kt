package com.example.park5.Objects

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import com.example.park5.Interface.GetInterface
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import retrofit2.converter.moshi.MoshiConverterFactory
object GetObject {

    /*private val moshi = Moshi.Builder().
            add(KotlinJsonAdapterFactory()).
            build()*/

//@TODO doesnt work yet, working on it
    val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl("https://openparking.stockholm.se/LTF-Tolken/v1/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(GetInterface::class.java)
    }
}