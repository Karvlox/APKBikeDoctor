package com.example.bikedoctor.data.remote

import com.example.bikedoctor.data.model.Client
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface ClientApi {
    @POST("api/Client")
    fun registerClient(@Body client: Client): Call<Client>

    @GET("api/Client")
    fun getClients(): Call<List<Client>>

    @GET("api/Client/{ci}")
    fun getClientById(@Path("ci") ci: Int): Call<Client>
}