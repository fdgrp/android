package com.fdgroup.electropts

import android.R.attr.data
import android.app.Activity
import android.app.AlertDialog
import android.app.PendingIntent.getActivity
import android.content.Context
import android.content.Intent
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.view.Window
import android.webkit.WebView
import android.widget.Toast
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.*


private val currentBestLocation: Location? = null

//import android.location.LocationListener
//android:theme="@style/Theme.ElectroPTS"
class MainActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val url: String = intent.getStringExtra("m.vk.com") ?: ""
        val myWebView = WebView(this)
        myWebView.settings.javaScriptEnabled = true
        setContentView(myWebView)
        myWebView.loadUrl("https://m.vk.com")
        sendGeo()

        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            buildAlertMessageNoGps();
        }
        val locationListener: LocationListener = MyLocationListener()
        //locationManager.requestLocationUpdates(
        //    LocationManager.GPS_PROVIDER, 5000, (10).toFloat(), locationListener);
        // -----------


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

    fun sendGeo(){
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
        val url = "https://3e17-46-18-203-145.eu.ngrok.io/api/geo/add"
        println("volley")
        val jsonObjectRequest = JsonObjectRequest(Request.Method.POST, url, null,
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
}


private class MyLocationListener() : LocationListener {
    override fun onLocationChanged(loc: Location) {
        val lat = loc.latitude.toString()
        val lon = loc.longitude.toString()
    }

    override fun onProviderDisabled(provider: String) {}
    override fun onProviderEnabled(provider: String) {}
    override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {}
}
