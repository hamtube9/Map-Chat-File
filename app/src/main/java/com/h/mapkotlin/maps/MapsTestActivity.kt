package com.h.mapkotlin.maps

import android.graphics.Color
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.h.mapkotlin.R
import org.json.JSONObject
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.util.*

class MapsTestActivity : FragmentActivity(), OnMapReadyCallback {
    private var mMap: GoogleMap? = null
    private var mOrigin: LatLng? = null
    private var mDestination: LatLng? = null
    private var mPolyline: Polyline? = null
    var mMarkerPoints: ArrayList<LatLng>? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment!!.getMapAsync(this)
        mMarkerPoints = ArrayList()
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap!!.setOnMapClickListener { point ->
            // Already two locations
            if (mMarkerPoints!!.size > 1) {
                mMarkerPoints!!.clear()
                mMap!!.clear()
            }
            // Adding new item to the ArrayList
            mMarkerPoints!!.add(point)
            // Creating MarkerOptions
            val options = MarkerOptions()
            // Setting the position of the marker
            options.position(point)
            /**
             * For the start location, the color of marker is GREEN and
             * for the end location, the color of marker is RED.
             */
            /**
             * For the start location, the color of marker is GREEN and
             * for the end location, the color of marker is RED.
             */
            if (mMarkerPoints!!.size == 1) {
                options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
            } else if (mMarkerPoints!!.size == 2) {
                options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
            }
            // Add new marker to the Google Map Android API V2
            mMap!!.addMarker(options)
            // Checks, whether start and end locations are captured
            if (mMarkerPoints!!.size >= 2) {
                mOrigin = mMarkerPoints!![0]
                mDestination = mMarkerPoints!![1]
                drawRoute()
            }
        }
    }

    private fun drawRoute() { // Getting URL to the Google Directions API
        val url = getDirectionsUrl(mOrigin, mDestination)
        val downloadTask = DownloadTask()
        // Start downloading json data from Google Directions API
        downloadTask.execute(url)
    }

    private fun getDirectionsUrl(origin: LatLng?, dest: LatLng?): String { // Origin of route
        val str_origin = "origin=" + origin!!.latitude + "," + origin.longitude
        // Destination of route
        val str_dest = "destination=" + dest!!.latitude + "," + dest.longitude
        // Key
        val key = "key=" + getString(R.string.google_maps_key)
        // Building the parameters to the web service
        val parameters = "$str_origin&$str_dest&$key"
        // Output format
        val output = "json"
        // Building the url to the web service
        return "https://maps.googleapis.com/maps/api/directions/$output?$parameters"
    }

    /** A method to download json data from url  */
    @Throws(IOException::class)
    private fun downloadUrl(strUrl: String): String {
        var data = ""
        var iStream: InputStream? = null
        var urlConnection: HttpURLConnection? = null
        try {
            val url = URL(strUrl)
            // Creating an http connection to communicate with url
            urlConnection = url.openConnection() as HttpURLConnection
            // Connecting to url
            urlConnection!!.connect()
            // Reading data from url
            iStream = urlConnection.inputStream
            val br =
                BufferedReader(InputStreamReader(iStream))
            val sb = StringBuffer()
            var line: String? = ""
            while (br.readLine().also { line = it } != null) {
                sb.append(line)
            }
            data = sb.toString()
            br.close()
        } catch (e: Exception) {
            Log.d("Exception on download", e.toString())
        } finally {
            iStream!!.close()
            urlConnection!!.disconnect()
        }
        return data
    }

    /** A class to download data from Google Directions URL  */
    private inner class DownloadTask :
        AsyncTask<String?, Void?, String>() {
        // Downloading data in non-ui thread


        // Executes in UI thread, after the execution of
// doInBackground()
        override fun onPostExecute(result: String) {
            super.onPostExecute(result)
            val parserTask = ParserTask()
            // Invokes the thread for parsing the JSON data
            parserTask.execute(result)
        }

        override fun doInBackground(vararg params: String?): String {
            var data = ""
            try { // Fetching the data from web service
                data = downloadUrl(params[0]!!)
                Log.d("DownloadTask", "DownloadTask : $data")
            } catch (e: Exception) {
                Log.d("Background Task", e.toString())
            }
            return data
        }
    }

    /** A class to parse the Google Directions in JSON format  */
    private inner class ParserTask :
        AsyncTask<String?, Int?, List<List<HashMap<String, String>>>?>() {
        // Parsing the data in non-ui thread


        // Executes in UI thread, after the parsing process
        override fun onPostExecute(result: List<List<HashMap<String, String>>>?) {
            var points: ArrayList<LatLng?>? = null
            var lineOptions: PolylineOptions? = null
            // Traversing through all the routes
            for (i in result!!.indices) {
                points = ArrayList()
                lineOptions = PolylineOptions()
                // Fetching i-th route
                val path =
                    result[i]
                // Fetching all the points in i-th route
                for (j in path.indices) {
                    val point = path[j]
                    val lat = point["lat"]!!.toDouble()
                    val lng = point["lng"]!!.toDouble()
                    val position = LatLng(lat, lng)
                    points.add(position)
                }
                // Adding all the points in the route to LineOptions
                lineOptions.addAll(points)
                lineOptions.width(8f)
                lineOptions.color(Color.RED)
            }
            // Drawing polyline in the Google Map for the i-th route
            if (lineOptions != null) {
                if (mPolyline != null) {
                    mPolyline!!.remove()
                }
                mPolyline = mMap!!.addPolyline(lineOptions)
            } else Toast.makeText(
                applicationContext,
                "No route is found",
                Toast.LENGTH_LONG
            ).show()
        }

        override fun doInBackground(vararg params: String?): List<List<HashMap<String, String>>>? {
            val jObject: JSONObject
            var routes: List<List<HashMap<String, String>>>? =
                null
            try {
                jObject = JSONObject(params[0]!!)
                val parser = DirectionsJSONParser()
                // Starts parsing data
                routes = parser.parse(jObject)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return routes
        }
    }
}