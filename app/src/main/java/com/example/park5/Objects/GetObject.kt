package com.example.park5.Objects

import android.util.Log
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import com.example.park5.Interface.GetInterface
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import retrofit2.converter.moshi.MoshiConverterFactory
object GetObject {

    private var retrofit: Retrofit? = null
    private val BASE_URL = "https://openparking.stockholm.se"

    val retrofitInstance:Retrofit?
        get() {
            if(retrofit == null){
                retrofit = Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
            }
            return retrofit
        }

}

