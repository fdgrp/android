package com.fdgroup.electropts

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.view.Window
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.core.app.ActivityCompat
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.BasicNetwork
import com.android.volley.toolbox.DiskBasedCache
import com.android.volley.toolbox.HurlStack
import com.android.volley.toolbox.JsonObjectRequest
import org.json.JSONObject


private val currentBestLocation: Location? = null

//import android.location.LocationListener
//android:theme="@style/Theme.ElectroPTS"
class MainActivity : Activity() {
    val server = "https://853a-46-18-203-145.eu.ngrok.io"
    override fun onCreate(savedInstanceState: Bundle?) {
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main) 
        val url: String = intent.getStringExtra("https://612e-46-18-203-145.eu.ngrok.io:3000") ?: ""
        val myWebView = WebView(this)
        myWebView.settings.javaScriptEnabled = true
        myWebView.getSettings().setJavaScriptEnabled(true);
        myWebView.getSettings().setDomStorageEnabled(true);
        myWebView.getSettings().setDatabaseEnabled(true);
        myWebView.settings.setSupportZoom(false);
        myWebView.settings.setAllowFileAccess(true);
        myWebView.settings.setAllowContentAccess(true);
        myWebView.getSettings().setMinimumFontSize(1);
        myWebView.getSettings().setMinimumLogicalFontSize(1);
        myWebView.setWebViewClient(WebViewClient())
        myWebView.setWebChromeClient(WebChromeClient())
        setContentView(myWebView)
        myWebView.loadUrl("http://10.1.1.31:3000")



        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            buildAlertMessageNoGps();

        }
        if (!isLocationPermissionGranted()) {
            return
        }

        var currentLocation: Location? = null

        val hasGps = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        val hasNetwork = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)

        val gpsLocationListener: LocationListener = object : LocationListener {
            override fun onLocationChanged(location: Location) {
                var locationByGps = location
            }

            override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {}
            override fun onProviderEnabled(provider: String) {}
            override fun onProviderDisabled(provider: String) {}
        }
//------------------------------------------------------//
        val networkLocationListener: LocationListener = object : LocationListener {
            override fun onLocationChanged(location: Location) {
                var locationByNetwork = location
            }

            override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {}
            override fun onProviderEnabled(provider: String) {}
            override fun onProviderDisabled(provider: String) {}
        }
        if (hasGps) {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return
            }
            locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                5000,
                0F,
                gpsLocationListener
            )
        }
//------------------------------------------------------//
        if (hasNetwork) {
            locationManager.requestLocationUpdates(
                LocationManager.NETWORK_PROVIDER,
                5000,
                0F,
                networkLocationListener
            )
        }
        var locationByGps: Location? = null
        var locationByNetwork: Location? = null
        var longitude: Double = 0.0
        var latitude: Double = 0.0
        val lastKnownLocationByGps =
        locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
        lastKnownLocationByGps?.let {
            locationByGps = lastKnownLocationByGps
        }
//------------------------------------------------------//
        val lastKnownLocationByNetwork =
            locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
        lastKnownLocationByNetwork?.let {
            locationByNetwork = lastKnownLocationByNetwork
        }
//------------------------------------------------------//
        if (locationByGps != null && locationByNetwork != null) {
            if (locationByGps!!.accuracy > locationByNetwork!!.accuracy) {
                currentLocation = locationByGps as Location
                latitude = currentLocation!!.latitude
                longitude = currentLocation!!.longitude
                // use latitude and longitude as per your need
            } else {
                currentLocation = locationByNetwork
                latitude = currentLocation!!.latitude
                longitude = currentLocation!!.longitude
                // use latitude and longitude as per your need
            }
        }
        println("lat: " + latitude)
        println("long: " + longitude)
        var token = auth2("qwe", "qwe")
        //sleep(2000)
        //var token = "1234"
        sendGeo(token,1, latitude, longitude)

    }
    private fun isLocationPermissionGranted(): Boolean {
        var requestcode = 0
        return if (ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    android.Manifest.permission.ACCESS_FINE_LOCATION,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION
                ), requestcode
            )
            false
        } else {
            true
        }
    }
    fun buildAlertMessageNoGps() {
        var builder = AlertDialog.Builder(this);
        builder.setMessage("Your GPS seems to be disabled, do you want to enable it?")
            .setCancelable(false)
            .setPositiveButton(android.R.string.yes) { dialog, which ->
                startActivity(Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
            }
            .setNegativeButton(android.R.string.no) { dialog, which ->
                dialog.cancel();
            }
        var alert = builder.create();
        alert.show();
    }

    fun sendGeo(token: String,id: Int, latitude: Double, longitude: Double){
        // Instantiate the RequestQueue.
        // Instantiate the cache
        val cache = DiskBasedCache(cacheDir, 1024 * 1024) // 1MB cap

// Set up the network to use HttpURLConnection as the HTTP client.
        val network = BasicNetwork(HurlStack())

// Instantiate the RequestQueue with the cache and network. Start the queue.
        val requestQueue = RequestQueue(cache, network).apply {
            start()
        }
        //val queue = Volley.newRequestQueue(this)
        val url = "$server/api/geo/add"
        println("volley")
        val req = JSONObject("""{"access_token":"$token", "car_id":1, "lat":$latitude, "lon":$longitude}""")
        val jsonObjectRequest = JsonObjectRequest(Request.Method.POST, url, req,
            Response.Listener { response ->
                println("Response: %s".format(response.toString()))
            },
            Response.ErrorListener { error ->
                println("Error " + error.toString())
                // TODO: Handle error
            }
        )

        requestQueue.add(jsonObjectRequest)
    }
    fun auth2(login: String, password: String): String {
        var result: String = ""
        // Instantiate the RequestQueue.
        // Instantiate the cache
        val cache2 = DiskBasedCache(cacheDir, 1024 * 1024) // 1MB cap

// Set up the network to use HttpURLConnection as the HTTP client.
        val network2 = BasicNetwork(HurlStack())

// Instantiate the RequestQueue with the cache and network. Start the queue.
        val requestQueue2 = RequestQueue(cache2, network2).apply {
            start()
        }
        //val queue = Volley.newRequestQueue(this)
        val url2 = "$server/api/user/auth"
        println("volley auth")
        val req2 = JSONObject("""{"login":$login, "password":$password}""")
        val jsonObjectRequest2 = JsonObjectRequest(Request.Method.POST, url2, req2,
            Response.Listener { response ->
                println("token: %s".format(response.toString()))
                result = response.toString()
            },
            Response.ErrorListener { error ->
                println("Error token " + error.toString())
                // TODO: Handle error
            })
        return result


        requestQueue2.add(jsonObjectRequest2)
    }
}
