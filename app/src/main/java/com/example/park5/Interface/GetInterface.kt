package com.example.park5.Interface

import com.example.retrotry.network.Get
import retrofit2.http.GET
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Response

interface GetInterface {
    //@TODO need to breakdown request
    @GET("pbuss/within?radius=25&lat=59.32784&lng=18.05306&outputFormat=json&apiKey=693167d9-7ce5-437a-90fd-030343a3bacf")
    fun getPost(): Call<Get>

}