package com.h.mapkotlin.chat

import com.h.mapkotlin.chat.file.FileResponse
import io.reactivex.Observable
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part


interface ApiService {


    @Multipart
    @POST("/send_media")
    fun uploadImage(@Part file: MultipartBody.Part, @Part("name") requestBody: RequestBody): Observable<ImageResponse>

    @Multipart
    @POST("/send_file")
    fun uploadFile(@Part file: MultipartBody.Part, @Part("name") requestBody: RequestBody): Observable<FileResponse>
}