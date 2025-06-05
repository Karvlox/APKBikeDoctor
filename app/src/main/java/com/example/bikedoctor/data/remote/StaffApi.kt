package com.example.bikedoctor.data.remote

import com.example.bikedoctor.data.model.StaffChangePassword
import com.example.bikedoctor.data.model.StaffLogin
import com.example.bikedoctor.data.model.StaffPost
import com.example.bikedoctor.data.model.StaffResetPassword
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

data class LoginResponse(
    val token: String
)

interface StaffApi {
    @POST("api/Auth/register")
    fun registerStaff(@Body staff: StaffPost): Call<StaffPost>

    @POST("api/Auth/login")
    fun loginStaff(@Body staff: StaffLogin): Call<LoginResponse>

    @POST("api/Auth/change-password")
    fun changePasswordStaff(@Body staff: StaffChangePassword): Call<StaffChangePassword>

    @POST("api/Auth/reset-password")
    fun resetPasswordStaff(@Body staff: StaffResetPassword): Call<StaffResetPassword>
}