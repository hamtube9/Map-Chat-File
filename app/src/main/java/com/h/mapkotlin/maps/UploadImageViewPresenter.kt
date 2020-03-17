package com.h.mapkotlin.maps

import com.h.mapkotlin.chat.ImageResponse
import ds.vuongquocthanh.socialnetwork.mvp.View

interface UploadImageViewPresenter : View{
    fun uploadImage(response : ImageResponse)
}