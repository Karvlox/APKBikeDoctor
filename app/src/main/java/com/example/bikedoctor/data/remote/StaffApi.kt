package com.example.bikedoctor.data.remote

import com.example.bikedoctor.data.model.Staff
import com.example.bikedoctor.data.model.StaffChangePassword
import com.example.bikedoctor.data.model.StaffLogin
import com.example.bikedoctor.data.model.StaffPost
import com.example.bikedoctor.data.model.StaffResetPassword
import com.example.bikedoctor.data.model.UpdateStaffRequest
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

data class LoginResponse(
    val token: String
)

interface StaffApi {
    @POST("api/Auth/register")
    fun registerStaff(@Body staff: StaffPost): Call<ResponseBody>

    @POST("api/Auth/login")
    fun loginStaff(@Body staff: StaffLogin): Call<LoginResponse>

    @POST("api/Auth/change-password")
    fun changePasswordStaff(@Body staff: StaffChangePassword): Call<StaffChangePassword>

    @POST("api/Auth/reset-password")
    fun resetPasswordStaff(@Body staff: StaffResetPassword): Call<StaffResetPassword>

    @GET("api/Auth/{id}")
    fun getStaffById(@Path("id") id: String): Call<Staff>

    @PUT("api/Auth/update/{id}")
    fun updateStaff(@Path("id") id: String, @Body staff: UpdateStaffRequest): Call<ResponseBody>
}