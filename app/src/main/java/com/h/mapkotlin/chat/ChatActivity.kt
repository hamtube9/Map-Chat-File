package com.h.mapkotlin.chat

import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.graphics.BitmapFactory
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.text.TextUtils
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.nkzawa.emitter.Emitter
import com.github.nkzawa.socketio.client.IO
import com.github.nkzawa.socketio.client.Socket
import com.h.mapkotlin.R
import com.h.mapkotlin.chat.file.FileResponse
import com.h.mapkotlin.chat.file.UploadFilePresenter
import com.h.mapkotlin.chat.file.UploadFileViewPresenter
import com.h.mapkotlin.maps.UploadImageViewPresenter
import kotlinx.android.synthetic.main.activity_chat.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONException
import org.json.JSONObject
import java.io.File
import java.io.IOException


class ChatActivity : AppCompatActivity(),
    UploadImageViewPresenter,
    SelectImageDialog.SelectImageListener,UploadFileViewPresenter {
    private lateinit var adapter: AdapterChat
    private val list = ArrayList<Chat>()
    private lateinit var presetner: UploadImagePresetner
    private var mSocket = IO.socket("http://123.31.20.166:8080")
    private val REQUEST_PICK_PICTURE = 12
    private val PICKFILE_REQUEST_CODE = 45
    private val RECORD_REQUESt = 115
    private lateinit var presenterFile : UploadFilePresenter
    private var dataImage = ""
    private lateinit var selectImageDialog: SelectImageDialog
    private var imageUri: Uri? = null
    private var uri: Uri? = null


    private  var myRecorder : MediaRecorder?=null
    private  var myPlayer:MediaPlayer?=null
    private var outputFile : String ?=null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)
        presetner = UploadImagePresetner()
        presetner.attachView(this)

        presenterFile = UploadFilePresenter()
        presenterFile.attachView(this)
        adapter = AdapterChat(this, list)
        rvChat.adapter = adapter
        rvChat.layoutManager = LinearLayoutManager(this)

        mSocket.on("Server_send_broadcast_message", emit)


        mSocket.let {
            it!!.connect()
                .on(Socket.EVENT_CONNECT) {
                    Log.d("SignallingClient", "Socket connected!!!!!")
                }
        }


        mSocket.connect()



        eventOnClick()
    }

    private fun eventOnClick() {
        edChat.setOnEditorActionListener(object : TextView.OnEditorActionListener {
            override fun onEditorAction(v: TextView?, actionId: Int, event: KeyEvent?): Boolean {
                if (actionId == 4) {
                    attemptSend()
                }
                return true
            }

        })

        ivSend.setOnClickListener {
            attemptSend()
            //uploadtoSever(dataImage)

        }

        ivMore.setOnClickListener {
            llItemMore.visibility = View.VISIBLE
            ivMore.visibility = View.GONE
        }
        ivLibrary.setOnClickListener { pickImage() }
    }

    private fun pickImage() {
        selectImageDialog = SelectImageDialog(this)
        selectImageDialog.show(supportFragmentManager, "Dialog")
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {

            REQUEST_PICK_PICTURE -> {
                if (resultCode == Activity.RESULT_OK) {
                    uri = data!!.data
                 //   uploadFile(uri!!)
                    val inputStream = contentResolver.openInputStream(imageUri!!)
                    val bitmap = BitmapFactory.decodeStream(inputStream)
                    ivImageChat.setImageBitmap(bitmap)

                }

            }
            2 -> {
                uri = data!!.data
                uploadFileImage(uri!!)

                val filePathColumn = arrayOf(MediaStore.Images.Media.DATA)
                assert(uri != null)
                val cursor = this.contentResolver.query(uri!!, filePathColumn, null, null, null)!!
                cursor.moveToFirst()

                val columnIndex = cursor.getColumnIndex(filePathColumn[0])
                val mediaPath = cursor.getString(columnIndex)
                cursor.close()

                val file = File(mediaPath)
                Log.d("FILE", file.toString())

//                val requestBody = RequestBody.create(MediaType.parse("multipart/form-data"), file)
//                fileToUpload = MultipartBody.Part.createFormData("file", file.name, requestBody)
//                Log.d("FILETOUPLOAD", fileToUpload.toString())
//                uploadFilePresenter.uploadFile(fileToUpload)
//                Log.d("IMAGE_PATH", mediaPath)
                val inputStream = contentResolver.openInputStream(uri!!)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                ivImageChat.setImageBitmap(bitmap)
            }

            PICKFILE_REQUEST_CODE ->{
                if (resultCode == Activity.RESULT_OK) {
                    uri = data!!.data
                    upFile(uri!!)
                 Log.d("fileUri",uri.toString())
                }
            }

        }
    }

    private val emit = Emitter.Listener {

        runOnUiThread(Runnable {
            val data = it.get(0) as JSONObject
            var message: String

            try {
                message = data.getString("msg")
                val time = data.getString("time")
                val chat = Chat("mlem mlem", message, time)
                Log.d("timeChat", chat.time)
                list.add(chat)
                adapter.notifyDataSetChanged()
                rvChat.smoothScrollToPosition(list.size - 1)
            } catch (e: JSONException) {
                Log.d("exception", e.toString())
            }
        })
    }

    private fun attemptSend() {
        val message: String = edChat.text.toString()
        if (TextUtils.isEmpty(message)) {
            return
        }

        edChat.setText("")
        val chat = Chat("Bi", message, "")
        Log.d("timeChat", chat.time + " lmao")
        list.add(chat)
        adapter.notifyDataSetChanged()
        rvChat.smoothScrollToPosition(list.size - 1)
        mSocket.emit("Client_send_path_media",uri)
        mSocket.emit("Client_send_message", message)


    }



    @RequiresApi(Build.VERSION_CODES.KITKAT)
    private fun uploadFileImage(fileUri: Uri) {
        val orifile = FileUtils.getFile(applicationContext, fileUri)
        val requestBody = orifile.asRequestBody("image/*".toMediaTypeOrNull())
        val multiPart = MultipartBody.Part.createFormData("upload", orifile.name, requestBody)
        val description = "image-type".toRequestBody("text/plain".toMediaTypeOrNull())
        presetner.uploadFiletoSever(multiPart, description)
    }

    @RequiresApi(Build.VERSION_CODES.KITKAT)
    private fun upFile(fileUri: Uri) {
        val orifile = FileUtils.getFile(applicationContext, fileUri)
        val requestBody = orifile.asRequestBody("*/*".toMediaTypeOrNull())
        val multiPart = MultipartBody.Part.createFormData("upload", orifile.name, requestBody)
        val description = "file-type".toRequestBody("text/plain".toMediaTypeOrNull())
        presenterFile.uploadFiletoSever(multiPart, description)
    }


    private fun cameraCaptureHightQuality() {
        val values = ContentValues()
        values.put(MediaStore.Images.Media.TITLE, "New Picture")
        values.put(MediaStore.Images.Media.DESCRIPTION, "From your Camera")
        imageUri = contentResolver.insert(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values
        )
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
        startActivityForResult(intent, REQUEST_PICK_PICTURE)
    }

    private fun startRecord(){
        try {
            outputFile = Environment.getExternalStorageDirectory().absolutePath +"lmao.3gpp"
            myRecorder = MediaRecorder()
            myRecorder!!.setAudioSource(MediaRecorder.AudioSource.MIC)
            myRecorder!!.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
            myRecorder!!.setAudioEncoder(MediaRecorder.OutputFormat.AMR_NB)
            myRecorder!!.setOutputFile(outputFile)

            myRecorder!!.prepare()
            myRecorder!!.start()
        }catch ( i :IllegalAccessException){
            i.printStackTrace()
        }catch (e : IOException){
            e.printStackTrace()
        }
    }

    private fun stopRecord(){
        try {
            myRecorder!!.stop()
            myRecorder!!.release()
            myRecorder=null
            Toast.makeText(this,"stop record",Toast.LENGTH_SHORT).show()

        }catch ( i :IllegalAccessException){
            i.printStackTrace()
        }catch (e : IOException){
            e.printStackTrace()
        }
    }


    override fun uploadImage(response: ImageResponse) {
        Toast.makeText(this, "Upload Success", Toast.LENGTH_SHORT).show()
        mSocket.emit("Client_send_path_media", response)
    }

    override fun uploadFile(response: FileResponse) {
        Toast.makeText(this, "Upload File Success", Toast.LENGTH_SHORT).show()

    }

    override fun showError(error: String) {
        Toast.makeText(this, "Fail :" + error, Toast.LENGTH_SHORT).show()

    }

    override fun cameraClick() {
        cameraCaptureHightQuality()
    }

    override fun galleryClick() {
        val pickPhoto = Intent(
            Intent.ACTION_PICK,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        )
        startActivityForResult(pickPhoto, 2)//one can be replaced with any action code
    }

    override fun fileClick() {
        val intent =  Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "*/*"
        startActivityForResult(intent, PICKFILE_REQUEST_CODE)
    }

    override fun startRecordClick() {
//        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
//
//            ActivityCompat.requestPermissions(this,  arrayOf(Manifest.permission.RECORD_AUDIO),RECORD_REQUESt)
//
//        } else {
//            startRecord()
//
//        }
    }

    override fun stopRecordClick() {
        //stopRecord()
    }


}
