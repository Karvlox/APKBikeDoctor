package com.example.bikedoctor.data.remote

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitStaff {
    private const val BASE_URL = "https://authenticationservice-production-df3c.up.railway.app/"

    //http://authenticationservice-production-6068.up.railway.app/
    //https://authenticationservice-production-df3c.up.railway.app/

    val staffApi: StaffApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(StaffApi::class.java)
    }
}