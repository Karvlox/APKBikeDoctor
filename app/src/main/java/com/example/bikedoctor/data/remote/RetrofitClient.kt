package com.example.bikedoctor.data.remote

import com.example.bikedoctor.MyApplication
import com.example.bikedoctor.data.model.CostApproval
import com.example.bikedoctor.data.model.QualityControl
import com.example.bikedoctor.data.repository.SessionRepository
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {
    private const val BASE_URL = "https://bikedoctor-production-b048.up.railway.app/"
    private const val CONNECT_TIMEOUT = 30L
    private const val READ_TIMEOUT = 30L
    //https://bikedoctor.onrender.com/
    //https://bikedoctor-production-b048.up.railway.app/

    private var currentToken: String? = null

    private val sessionRepository = SessionRepository(MyApplication.context)

    private val authInterceptor = Interceptor { chain ->
        val original: Request = chain.request()
        val requestBuilder = original.newBuilder()
            .header("Authorization", "Bearer ${currentToken ?: ""}")
        val request = requestBuilder.build()
        chain.proceed(request)
    }

    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS)
            .readTimeout(READ_TIMEOUT, TimeUnit.SECONDS)
            .addInterceptor(authInterceptor)
            .build()
    }

    // Método para actualizar el token globalmente
    fun updateToken(token: String?) {
        currentToken = token
    }

    val clientApi: ClientApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ClientApi::class.java)
    }

    val motorcycleApi: MotorcycleApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(MotorcycleApi::class.java)
    }

    val receptionApi: ReceptionApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ReceptionApi::class.java)
    }

    val diagnosisAPI: DiagnosisAPI by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(DiagnosisAPI::class.java)
    }

    val sparePartsAPI: SparePartsAPI by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(SparePartsAPI::class.java)
    }

    val repairAPI: RepairAPI by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(RepairAPI::class.java)
    }

    val costApprovalAPI: CostApprovalAPI by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(CostApprovalAPI::class.java)
    }

    val controlAPI: ControlAPI by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ControlAPI::class.java)
    }

    val deliveryAPI: DeliveryAPI by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
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