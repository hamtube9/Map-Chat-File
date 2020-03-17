package com.h.mapkotlin.chat

import com.google.gson.GsonBuilder
import com.jakewharton.retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class ApiUtil {
    companion object {
        const val BASE_URL = "http://123.31.20.166:8080"
        fun getAPIService(): ApiService {

//            val gson = GsonBuilder().setLenient().create()
            val okHttpClient: OkHttpClient = OkHttpClient.Builder()
                .writeTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .connectTimeout(60, TimeUnit.SECONDS)
                .build()

            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(okHttpClient)
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .build().create(ApiService::class.java)
        }
    }
}