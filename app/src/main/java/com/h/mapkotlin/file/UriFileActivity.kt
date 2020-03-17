package com.h.mapkotlin.file

import android.content.ContentUris
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.h.mapkotlin.R
import kotlinx.android.synthetic.main.activity_uri_file.*
import java.io.BufferedReader
import java.io.File
import java.io.FileReader


class UriFileActivity : AppCompatActivity() {

    private var fullerror = ""
    private var FILE_SELECT_CODE: Int = 101
    private val TAG = "uriactivty"
    private var actualfilepath = ""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_uri_file)

        btnGetFile.setOnClickListener {
            showFileChooser()
        }

    }


    private fun showFileChooser() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "*/*"
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        try {
            startActivityForResult(
                Intent.createChooser(intent, "Select a File to Upload"),
                FILE_SELECT_CODE
            )
        } catch (e: Exception) {
            Log.e(TAG, " choose file error $e")
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode === FILE_SELECT_CODE)
        {
            if (resultCode === RESULT_OK)
            {
                try
                {
                    val fileuri = data!!.data
                    var tempID = ""
                    var id = ""
                    val uri = data.data
                    Log.e(TAG, "file auth is " + uri!!.authority)
                    fullerror = fullerror + "file auth is " + uri.authority
                    if (fileuri!!.authority.equals("media"))
                    {
                        tempID = fileuri.toString()
                        tempID = tempID.substring(tempID.lastIndexOf("/") + 1)
                        id = tempID
                        val contenturi = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                        val selector = MediaStore.Images.Media._ID + "=?"
                        actualfilepath = getColunmData(contenturi, selector, arrayOf(id))
                    }
                    else if (fileuri.authority.equals("com.android.providers.media.documents"))
                    {
                        tempID = DocumentsContract.getDocumentId(fileuri)
                        val split = tempID.split((":").toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                        val type = split[0]
                        id = split[1]
                        var contenturi:Uri ?= null
                        when (type) {
                            "image" -> {
                                contenturi = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                            }
                            "video" -> {
                                contenturi = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                            }
                            "audio" -> {
                                contenturi = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                            }
                        }
                        val selector = "_id=?"
                        actualfilepath = getColunmData(contenturi!!, selector, arrayOf(id))
                    }
                    else if (fileuri.authority.equals("com.android.providers.downloads.documents"))
                    {
                        tempID = fileuri.toString()
                        tempID = tempID.substring(tempID.lastIndexOf("/") + 1)
                        id = tempID
                        val contenturi = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), java.lang.Long.valueOf(id))
                        // String selector = MediaStore.Images.Media._ID+"=?";
                        actualfilepath = getColunmData(contenturi, "", arrayOf())
                    }
                    else if (fileuri.authority.equals("com.android.externalstorage.documents"))
                    {
                        tempID = DocumentsContract.getDocumentId(fileuri)
                        val split = tempID.split((":").toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                        val type = split[0]
                        id = split[1]
                        if (type == "primary")
                        {
                            actualfilepath = Environment.getExternalStorageDirectory().toString() + "/" + id
                        }
                    }
                    var myFile = File(actualfilepath)
                    // MessageDialog dialog = new MessageDialog(Home.this, " file details --"+actualfilepath+"\n---"+ uri.getPath() );
                    // dialog.displayMessageShow();
                    var temppath = uri.path
                    if (temppath!!.contains("//"))
                    {
                        temppath = temppath.substring(temppath.indexOf("//") + 1)
                    }
                    Log.e(TAG, " temppath is $temppath")
                    fullerror = fullerror + "\n" + " file details - " + actualfilepath + "\n --" + uri.path + "\n--" + temppath
                    if (actualfilepath.equals("") || actualfilepath.equals(" "))
                    {
                        myFile = File(temppath)
                    }
                    else
                    {
                        myFile = File(actualfilepath)
                    }
                    //File file = new File(actualfilepath);
                    //Log.e(TAG, " actual file path is "+ actualfilepath + " name ---"+ file.getName());
                    // File myFile = new File(actualfilepath);
                    Log.e(TAG, " myfile is " + myFile.absolutePath)


               //     textView.text = myFile.absolutePath

                    textView.text = fileuri.toString()

                    readfile(myFile)
                    // lyf path - /storage/emulated/0/kolektap/04-06-2018_Admin_1528088466207_file.xls
                }
                catch (e:Exception) {
                    Log.e(TAG, " read errro $e")
                }
                //------------ /document/primary:kolektap/30-05-2018_Admin_1527671367030_file.xls
            }
        }
    }


    private fun getColunmData(uri: Uri, selection:String, selectarg:Array<String>):String {
        var filepath = ""
        var cursor: Cursor ?= null
        val colunm = "_data"
        val projection = arrayOf(colunm)
        cursor = contentResolver.query(uri, projection, selection, selectarg, null)
        if (cursor != null)
        {
            cursor.moveToFirst()
            Log.e(TAG, " file path is " + cursor.getString(cursor.getColumnIndex(colunm)))
            filepath = cursor.getString(cursor.getColumnIndex(colunm))
        }
        if (cursor != null)
            cursor.close()
        return filepath
    }

    private fun readfile(file: File) {
        // File file = new File(Environment.getExternalStorageDirectory(), "mytextfile.txt");
        val builder = StringBuilder()
        Log.e("main", "read start")
        val br = BufferedReader(FileReader(file))
        var line=""
        try
        {
            while ({ line = br.readLine(); line }() != null) {
                builder.append(line)
                builder.append("\n")
            }


            br.close()
        }
        catch (e:Exception) {
            Log.e("main", " error is $e")
        }
        Log.e("main", " read text is $builder")
       // textView.text = builder.toString()
    }
}

