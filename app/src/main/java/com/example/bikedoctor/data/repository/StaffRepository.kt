package com.example.bikedoctor.data.repository

import com.example.bikedoctor.data.model.StaffChangePassword
import com.example.bikedoctor.data.model.StaffLogin
import com.example.bikedoctor.data.model.StaffPost
import com.example.bikedoctor.data.model.StaffResetPassword
import com.example.bikedoctor.data.remote.LoginResponse
import com.example.bikedoctor.data.remote.RetrofitStaff
import retrofit2.Call

class StaffRepository {
    fun registerStaff(staff: StaffPost): Call<StaffPost> {
        return RetrofitStaff.staffApi.registerStaff(staff)
    }

    fun loginStaff(staff: StaffLogin): Call<LoginResponse> {
        return RetrofitStaff.staffApi.loginStaff(staff)
    }

    fun changePasswordStaff(staff: StaffChangePassword): Call<StaffChangePassword> {
        return RetrofitStaff.staffApi.changePasswordStaff(staff)
    }

    fun resetPasswordStaff(staff: StaffResetPassword): Call<StaffResetPassword> {
        return RetrofitStaff.staffApi.resetPasswordStaff(staff)
    }

}