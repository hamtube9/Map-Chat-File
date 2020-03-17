package com.h.mapkotlin.chat

import android.util.Log
import com.h.mapkotlin.maps.UploadImageViewPresenter
import ds.vuongquocthanh.socialnetwork.mvp.Presenter
import ds.vuongquocthanh.socialnetwork.mvp.View
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import okhttp3.MultipartBody
import okhttp3.RequestBody

class UploadImagePresetner :Presenter{
    val compositeDisposable = CompositeDisposable()
    lateinit var viewPresenter : UploadImageViewPresenter
    override fun attachView(view: View) {
        viewPresenter = view as UploadImageViewPresenter
    }

    override fun dispose() {
        compositeDisposable.dispose()
    }

    fun uploadFiletoSever(part : MultipartBody.Part, des : RequestBody){
        compositeDisposable.add(ApiUtil.getAPIService().uploadImage(part,des).subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread()).subscribe(this::uploadSuccess){ t-> uploadFail(t,"Upload Fail")})
    }

    private fun uploadSuccess(response : ImageResponse){
        if (response.status == true){
            viewPresenter.uploadImage(response)

        }
    }

    private fun uploadFail(t:Throwable,error:String){
        viewPresenter.showError(error)
       Log.d("failUpload", t.localizedMessage)
    }
}