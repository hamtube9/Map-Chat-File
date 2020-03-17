package com.h.mapkotlin

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Geocoder
import android.location.Location
import android.os.*
import android.util.Log
import android.view.animation.Interpolator
import android.view.animation.LinearInterpolator
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.common.api.Status
import com.google.android.gms.location.*
import com.google.android.gms.location.places.Place
import com.google.android.gms.location.places.ui.PlaceSelectionListener
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.gson.Gson
import com.h.mapkotlin.direction.FetchURL
import com.h.mapkotlin.direction.TaskLoadedCallback
import kotlinx.android.synthetic.main.activity_maps.*
import okhttp3.OkHttpClient
import okhttp3.Request
import java.lang.Exception
import java.util.*
import kotlin.collections.ArrayList


class MapsActivity : AppCompatActivity(), OnMapReadyCallback, PlaceSelectionListener,
    TaskLoadedCallback {
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback
    private var keyAPI = ""
    lateinit var placesClient: PlacesClient

    var placesField = Arrays.asList(
        com.google.android.libraries.places.api.model.Place.Field.ID,
        com.google.android.libraries.places.api.model.Place.Field.NAME,
        com.google.android.libraries.places.api.model.Place.Field.ADDRESS,
        com.google.android.libraries.places.api.model.Place.Field.LAT_LNG
    )
    private val PERMISSION_REQUEST = 100
    private var latitude = 0.toDouble()
    private var longitude = 0.toDouble()

    private lateinit var mMap: GoogleMap
    private lateinit var location1: LatLng
    private lateinit var location2: LatLng

    private lateinit var mLastLocation: Location
    private var mMarker: Marker? = null

    lateinit var currentPolyLine: PolyLine
    lateinit var mark1: MarkerOptions
    lateinit var mark2: MarkerOptions
    private val listMarker = ArrayList<MarkerOptions>()
    private var isMarkerRotating = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        keyAPI = getString(R.string.google_maps_key)
        initPlaces()

        setupAutoComplete()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkLocationPermission()) {
                buildLocationRequest()
                buildLocationCallback()

                fusedLocationProviderClient =
                    LocationServices.getFusedLocationProviderClient(this)
                fusedLocationProviderClient.requestLocationUpdates(
                    locationRequest, locationCallback,
                    Looper.myLooper()
                )
            }
        } else {
            buildLocationRequest()
            buildLocationCallback()

            fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
            fusedLocationProviderClient.requestLocationUpdates(
                locationRequest, locationCallback,
                Looper.myLooper()
            )
        }


        btnFind.setOnClickListener {
            getLine(mMarker!!.position, location2)
//            val bear = bearingBetweenLocations(mMarker!!.position, location2)
//            rotateMarker(mMarker!!, bear.toFloat())
            val URL = getDirectionURL(location1, location2)
            getDirection(URL).execute()
        }


    }

    private fun setupAutoComplete() {
        val autoComplete =
            supportFragmentManager.findFragmentById(R.id.autocomplete_fragment) as AutocompleteSupportFragment
        autoComplete.setPlaceFields(placesField)
        autoComplete.setOnPlaceSelectedListener(object :
            com.google.android.libraries.places.widget.listener.PlaceSelectionListener {
            override fun onPlaceSelected(place: com.google.android.libraries.places.api.model.Place) {
                Toast.makeText(applicationContext, "" + place.latLng, Toast.LENGTH_LONG)
                    .show()
                val latLng = place.latLng
                val markerOptions =
                    MarkerOptions().position(latLng!!).title(getString(R.string.current_location))
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
                mMarker = mMap.addMarker(markerOptions)
                mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng))
                mMap.animateCamera(CameraUpdateFactory.zoomTo(12f))

            }

            override fun onError(place: Status) {
                Toast.makeText(applicationContext, "" + place.statusMessage, Toast.LENGTH_LONG)
                    .show()

            }

        })
    }

    private fun initPlaces() {
        Places.initialize(this@MapsActivity, keyAPI)
        placesClient = Places.createClient(this@MapsActivity)
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */

    override fun onStop() {
        super.onStop()
        fusedLocationProviderClient.removeLocationUpdates(locationCallback)
    }


    private fun showAllMarker() {
        val lb = LatLngBounds.Builder()
        for (mark: MarkerOptions in listMarker) {
            lb.include(mark.position)
        }

        val bounds = lb.build()
        var width = resources.displayMetrics.widthPixels
        var height = resources.displayMetrics.heightPixels
        val padding = (width * 0.30).toInt()
        val camera = CameraUpdateFactory.newLatLngBounds(bounds, width, height, padding)
        mMap.animateCamera(camera)


    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                mMap.isMyLocationEnabled = true
            }
        } else {
            mMap.isMyLocationEnabled = true

            mMap.uiSettings.isZoomControlsEnabled = true

        }
//
        mMap.setOnMapClickListener { latLng ->
            mMap.clear()


            location2 = LatLng(latLng.latitude, latLng.longitude)
            mark2 =
                MarkerOptions().position(location2).title(getString(R.string.current_location))
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
            listMarker.add(mark2)
//            location2.distanceTo(location1)
//            Log.d("location", location2.distanceTo(location1).toString())

            mMap.addMarker(
                MarkerOptions().position(mark2.position).title("????").rotation(-15.0f).icon(
                    BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)
                )
            )
//            if ( mark1!=null && mark2!=null){
//                FetchURL(this@MapsActivity).execute(
//                    getURL(mark1.position, mark2.position, "driving"),
//                    "driving"
//                )
//            }

            val diachi = getAddress(latLng.latitude, latLng.longitude)
            tvAddress.text = diachi


        }
    }

    //https://maps.googleapis.com/maps/api/directions/
    // json
    // ?mode= driving
    // &transit_routing_preference= less_driving
    // &origin=   21.025953, 105.814218
    // &destination=   destination
    // &key= AIzaSyBWxGdo7HwUu1qXQo9rQYMf3XeZvvY98yM

    private fun getURL(origin: LatLng, des: LatLng, s: String): String? {
        var str_origin = "origin=" + origin.latitude + "," + origin.longitude
        var str_des = "origin=" + des.latitude + "," + des.longitude
        var str_mode = "mode=" + s
        var parameter = str_origin + "&" + str_des + "&" + str_mode
        val format = "json"
        val URL =
            "https:/maps.googleapi.com/maps/api/direction/" + format + "?" + parameter + "&key=" + keyAPI

        return URL
    }

    /////////////////
    fun getDirectionURL(origin: LatLng, dest: LatLng): String {
        //https://maps.googleapis.com/maps/api/directions/json?mode=driving&transit_routing_preference=less_driving&origin=21.025953, 105.814218&destination=destination&key=AIzaSyBWxGdo7HwUu1qXQo9rQYMf3XeZvvY98yM
        return "https://maps.googleapis.com/maps/api/directions/json?origin=${origin.latitude},${origin.longitude}&destination=${dest.latitude},${dest.longitude}&sensor=false&mode=driving&key=${keyAPI}"

    }

    inner class getDirection(var url: String) : AsyncTask<Void, Void, List<List<LatLng>>>() {
        override fun doInBackground(vararg params: Void?): List<List<LatLng>> {
            val client = OkHttpClient()
            val request = Request.Builder().url(url).build()
            val response = client.newCall(request).execute()
            val data = response.body.toString()
            val result = ArrayList<List<LatLng>>()
            try {
                val responseObject = Gson().fromJson(data, GoogleMapDTO::class.java)
                val path = ArrayList<LatLng>()
                for (i in 0..(responseObject.routes[0].legs[0].steps.size - 1)) {
                    val start = LatLng(
                        responseObject.routes[0].legs[0].steps[i].start_location.lat.toDouble(),
                        responseObject.routes[0].legs[0].steps[i].end_location.lng.toDouble()
                    )
                    path.add(start)
                    val end = LatLng(
                        responseObject.routes[0].legs[0].steps[i].start_location.lat.toDouble(),
                        responseObject.routes[0].legs[0].steps[i].end_location.lng.toDouble()
                    )
                    path.add(end)

                }
                result.add(path)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return result
        }


        override fun onPostExecute(result: List<List<LatLng>>?) {
            super.onPostExecute(result)
            val lineOption = PolylineOptions()

            for (i in result!!.indices) {
                lineOption.addAll(result[i])
                lineOption.width(10f)
                lineOption.color(Color.BLUE)
                lineOption.geodesic(true)
            }
            mMap.addPolyline(lineOption)
        }
    }

    ///////////////////
    private fun getAddress(lat: Double, long: Double): String {
        val geocoder = Geocoder(this@MapsActivity, Locale.getDefault())
        val geoList = geocoder.getFromLocation(lat, long, 1)
//        Log.d("geoList",geoList.size.toString())
//        val addressList = ArrayList<Address>()
//        addressList.clear()
//        addressList.addAll(geoList)
//        if (addressList.size > 0) {
//            val address = addressList[0]
//            fullAddress = address.getAddressLine(0)
//            Log.d("address", fullAddress)
//        }

        return geoList[0].getAddressLine(0)
    }
//

    private fun buildLocationRequest() {
        locationRequest = LocationRequest()
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        locationRequest.interval = 5000
        locationRequest.fastestInterval = 3000
        locationRequest.smallestDisplacement = 10f
    }

    private fun buildLocationCallback() {
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(p0: LocationResult?) {
                super.onLocationResult(p0)
                mLastLocation = p0!!.locations.get(p0.locations.size - 1)
                if (mMarker != null) {
                    mMarker!!.remove()
                }
                latitude = mLastLocation.latitude
                longitude = mLastLocation.longitude
                val latLng = LatLng(latitude, longitude)
                location1 = LatLng(latitude, longitude)
//                location1.latitude = latitude
////                location1.longitude = longitude
                Log.d(
                    "location",
                    location1.latitude.toString() + " ---" + location1.longitude.toString()
                )

                mark1 = MarkerOptions().position(latLng).title(getString(R.string.current_location))
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
                listMarker.add(mark1)
                mMarker = mMap.addMarker(mark1)
                mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng))
                mMap.animateCamera(CameraUpdateFactory.zoomTo(12f))
            }
        }
    }


    private fun checkLocationPermission(): Boolean {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            ) {


                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    PERMISSION_REQUEST
                )
            } else {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    PERMISSION_REQUEST
                )
            }
            return false

        } else
            return true
    }

    override fun onPlaceSelected(place: Place) {
        Toast.makeText(
            applicationContext,
            "" + place.name + place.latLng,
            Toast.LENGTH_LONG
        ).show()
    }

    override fun onError(p0: Status?) {
    }

    override fun onTaskDone(vararg values: Any) {
        val value: PolylineOptions? = values[0] as PolylineOptions

        currentPolyLine = mMap.addPolyline(value) as PolyLine

    }


    private fun getLine(startLatLng: LatLng, endLatLng: LatLng) {
        val points: ArrayList<LatLng> = ArrayList()
        val polyLineOptions = PolylineOptions()
        points.add(startLatLng)
        points.add(endLatLng)
        polyLineOptions.width((7 * 1).toFloat())
        polyLineOptions.geodesic(true)
        polyLineOptions.color(this.resources.getColor(R.color.colorBlack))
        polyLineOptions.addAll(points)
        val polyline = mMap.addPolyline(polyLineOptions)
        polyline.setGeodesic(true)
    }


    private fun bearingBetweenLocations(latLng1: LatLng, latLng2: LatLng): Double {
        val PI = 3.14159
        val lat1 = latLng1.latitude * PI / 180
        val long1 = latLng1.longitude * PI / 180
        val lat2 = latLng2.latitude * PI / 180
        val long2 = latLng2.longitude * PI / 180
        val dLon = long2 - long1
        val y = Math.sin(dLon) * Math.cos(lat2)
        val x =
            Math.cos(lat1) * Math.sin(lat2) - (Math.sin(lat1)
                    * Math.cos(lat2) * Math.cos(dLon))
        var brng = Math.atan2(y, x)
        brng = Math.toDegrees(brng)
        brng = (brng + 360) % 360
        return brng
    }

    private fun rotateMarker(marker: Marker, toRotation: Float) {
        if (isMarkerRotating) {
            val handler = Handler()
            val start = SystemClock.uptimeMillis()
            val startRotation = marker.rotation
            val duration: Long = 1000
            val interPolator: Interpolator = LinearInterpolator()


            handler.post(object : Runnable {
                override fun run() {
                    isMarkerRotating = true
                    val elapsed = SystemClock.uptimeMillis() - start
                    val t = interPolator.getInterpolation((elapsed / duration) as Float)
                    val rot = t * toRotation + (1 - t) * startRotation
                    marker.rotation = if (-rot > 180) rot / 2 else rot
                    if (t < 1.0) {
                        handler.postDelayed(this, 16)
                    } else {
                        isMarkerRotating = false
                    }
                }
            })
        }
    }
}
