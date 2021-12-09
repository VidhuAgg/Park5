package com.example.park5.Interface

import com.example.retrotry.network.Get
import retrofit2.http.GET
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Query

const val parking_api_key = "693167d9-7ce5-437a-90fd-030343a3bacf"

interface GetInterface {
    //@TODO need to breakdown request
    //within?radius=100&lat=59.32784&lng=18.05306&outputFormat=json&apiKey=693167d9-7ce5-437a-90fd-030343a3bacf
    @GET("/LTF-Tolken/v1/ptillaten/within")
    fun getPost(
        @Query("radius") rad:Int,
        @Query("lat") latitude:Double,
        @Query("lng") longitude:Double,
        @Query("outputFormat") form:String,
        @Query("apiKey") key:String
    ): Call<Get>

}