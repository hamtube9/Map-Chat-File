package com.h.mapkotlin.chat

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import androidx.fragment.app.DialogFragment
import com.h.mapkotlin.R
import kotlinx.android.synthetic.main.dialog_choose_image.view.*

class SelectImageDialog(private var listener:SelectImageListener) : DialogFragment()  {

    private lateinit var viewDialog: View

    interface SelectImageListener{
        fun cameraClick()
        fun galleryClick()
        fun fileClick()
        fun startRecordClick()
        fun stopRecordClick()
    }

    @SuppressLint("InflateParams")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(activity!!)
        viewDialog = activity!!.layoutInflater.inflate(R.layout.dialog_choose_image, null)
        builder.setView(viewDialog)
        val dialog = builder.create()
        val window = dialog.window
        window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val wlp = window.attributes
        wlp.gravity = Gravity.BOTTOM
        wlp.flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND
        window!!.attributes = wlp

        viewDialog.ln_camera.setOnClickListener {
            dismiss()
            listener.cameraClick()
        }

        viewDialog.ln_gallery.setOnClickListener {
            dismiss()
            listener.galleryClick()
        }
        viewDialog.ln_file.setOnClickListener {
            dismiss()
            listener.fileClick()
        }
        viewDialog.ln_start_record.setOnClickListener {
            dismiss()
            listener.startRecordClick()
        }

        viewDialog.ln_stop_record.setOnClickListener {
            dismiss()
            listener.stopRecordClick()
        }

        return dialog
    }
}