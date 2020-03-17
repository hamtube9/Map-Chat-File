package com.h.mapkotlin.googleDirection

import android.graphics.Color
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.google.gson.Gson
import com.h.mapkotlin.GoogleMapDTO
import com.h.mapkotlin.PolyLine
import com.h.mapkotlin.R
import com.h.mapkotlin.direction.FetchURL
import com.h.mapkotlin.direction.TaskLoadedCallback
import kotlinx.android.synthetic.main.activity_direction.*
import okhttp3.OkHttpClient
import okhttp3.Request
import java.lang.Exception

class DirectionActivity : AppCompatActivity(),OnMapReadyCallback {
    private lateinit var mMap : GoogleMap
    private lateinit var place1 : MarkerOptions
    private lateinit var place2 : MarkerOptions
    private val markerOptionsList = ArrayList<MarkerOptions>()
    private var url =""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_direction)


        place1 = MarkerOptions().position(LatLng(21.031409, 105.850953)).title("Home")
        place2=MarkerOptions().position(LatLng(21.037573, 105.790039)).title("Work")

        url = getURL(place1.position,place2.position,"driving")
        btnDirection.setOnClickListener {
          GetDirection(url)
        }

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.mapDirection) as SupportMapFragment
        mapFragment.getMapAsync(this)

    }

    private fun getURL(origin : LatLng,destination : LatLng,directionMode : String) : String{
        val str_origin = "origin="+origin.latitude+","+origin.longitude
        val str_des = "destination="+destination.latitude+","+destination.longitude
        val mode = "mode="+directionMode
        val parameter =str_origin+"&"+str_des+"&"+mode
        val format = "json"
        val url = "https://maps.googleapis.com/maps/api/direction/"+format+"?"+parameter+"&key=AIzaSyCaRN3f2rVv-abnt3z8cI-BjFDr3Aa3LKQ"
        return url
    }

    override fun onMapReady(googleMap: GoogleMap?) {
        mMap = googleMap!!
        mMap.isMyLocationEnabled = true
        mMap.addMarker(place1)
        mMap.addMarker(place2)
    }

//    private fun showAllMarker() {
//         val builder = LatLngBounds.Builder()
//        for(markerOption in markerOptionsList){
//            builder.include(markerOption.position)
//        }
//        val bounds = builder.build()
//        val witdh = resources.displayMetrics.widthPixels
//        val height = resources.displayMetrics.heightPixels
//        val padding : Int = ((witdh*0.30).toInt())
//
//        val carmeraUpdate = CameraUpdateFactory.newLatLngBounds(bounds,witdh,height,padding)
//
//        mMap.animateCamera(carmeraUpdate)
//    }


    inner class GetDirection(val url : String) :AsyncTask<Void,Void,List<List<LatLng>>>(){
        override fun doInBackground(vararg params: Void?): List<List<LatLng>> {
            val client = OkHttpClient()
            val request =Request.Builder().url(url).build()
            val response = client.newCall(request).execute()
            val data = response.body.toString()
            val result = ArrayList<List<LatLng>>()
            try {

                val responseObj = Gson().fromJson(data,GoogleMapDTO::class.java)
                val path = ArrayList<LatLng>()
                for( i  in 0..responseObj.routes[0].legs[0].steps.size-1){
                    val Startlatlng = LatLng(responseObj.routes[0].legs[0].steps[i].start_location.lat.toDouble(),responseObj.routes[0].legs[0].steps[i].start_location.lng.toDouble())

                    path.add(Startlatlng)
                    val Endlatlng = LatLng(responseObj.routes[0].legs[0].steps[i].end_location.lat.toDouble(),responseObj.routes[0].legs[0].steps[i].end_location.lng.toDouble())
                    path.add(Endlatlng)
                }
                result.add(path)
            }catch (e:Exception){
                e.printStackTrace()
            }
            return result
        }

        override fun onPostExecute(result: List<List<LatLng>>?) {
            for(i in result!!.indices){
                val lineOptions = PolylineOptions()
                lineOptions.addAll(result[i])
                lineOptions.width(10f)
                lineOptions.color(Color.BLUE)
                lineOptions.geodesic(true)

                mMap.addPolyline(lineOptions)
            }
        }

    }
}
