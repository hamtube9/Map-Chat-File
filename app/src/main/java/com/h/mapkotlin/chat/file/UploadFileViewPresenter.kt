package com.h.mapkotlin.chat.file

import ds.vuongquocthanh.socialnetwork.mvp.View

interface UploadFileViewPresenter :View{
    fun uploadFile(response : FileResponse)
}