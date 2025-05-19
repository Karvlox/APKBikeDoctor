package com.example.bikedoctor.data.remote

import com.example.bikedoctor.data.model.CostApproval
import com.example.bikedoctor.data.model.QualityControl
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private const val BASE_URL = "https://bikedoctor-production.up.railway.app/"

    val clientApi: ClientApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ClientApi::class.java)
    }

    val motorcycleApi: MotorcycleApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(MotorcycleApi::class.java)
    }

    val receptionApi: ReceptionApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ReceptionApi::class.java)
    }

    val diagnosisAPI: DiagnosisAPI by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(DiagnosisAPI::class.java)
    }

    val sparePartsAPI: SparePartsAPI by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(SparePartsAPI::class.java)
    }

    val repairAPI: RepairAPI by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(RepairAPI::class.java)
    }

    val costApprovalAPI: CostApprovalAPI by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(CostApprovalAPI::class.java)
    }

    val controlAPI: ControlAPI by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ControlAPI::class.java)
    }

    val deliveryAPI: DeliveryAPI by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(DeliveryAPI::class.java)
    }

    val messageNotificationAPI: MessageNotificationAPI by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(MessageNotificationAPI::class.java)
    }
}