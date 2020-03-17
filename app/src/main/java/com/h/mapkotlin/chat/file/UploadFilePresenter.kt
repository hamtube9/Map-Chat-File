package com.h.mapkotlin.chat.file

import android.util.Log
import com.h.mapkotlin.chat.ApiUtil
import ds.vuongquocthanh.socialnetwork.mvp.Presenter
import ds.vuongquocthanh.socialnetwork.mvp.View
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import okhttp3.MultipartBody
import okhttp3.RequestBody

class UploadFilePresenter :Presenter{
    val compositeDisposable = CompositeDisposable()
    lateinit var viewPresenter : UploadFileViewPresenter
    override fun attachView(view: View) {
        viewPresenter = view as UploadFileViewPresenter
    }

    override fun dispose() {
        compositeDisposable.dispose()
    }

    fun uploadFiletoSever(part : MultipartBody.Part, des : RequestBody){
        compositeDisposable.add(
            ApiUtil.getAPIService().uploadFile(part,des).subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread()).subscribe(this::uploadSuccess){ t-> uploadFail(t,"Upload Fail")})
    }

    private fun uploadSuccess(response : FileResponse){
        if (response.status == true){
            viewPresenter.uploadFile(response)

        }
    }

    private fun uploadFail(t:Throwable,error:String){
        viewPresenter.showError(error)
        Log.d("failUpload", t.localizedMessage)
    }
}