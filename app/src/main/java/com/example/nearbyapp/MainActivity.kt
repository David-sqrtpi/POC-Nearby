package com.example.nearbyapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.connection.AdvertisingOptions
import com.google.android.gms.nearby.connection.DiscoveryOptions
import com.google.android.gms.nearby.connection.Strategy


class MainActivity : AppCompatActivity() {
    private val STRATEGY = Strategy.P2P_CLUSTER
    private val SERVICE_ID = "com.example.nearbyapp.id"
    private val LOCAL_USERNAME = "final32david"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    private fun startAdvertising() {
        val advertisingOptions = AdvertisingOptions.Builder().setStrategy(STRATEGY).build()
        Nearby.getConnectionsClient(this)
            .startAdvertising(
                LOCAL_USERNAME, SERVICE_ID, connectionLifecycleCallback, advertisingOptions
            )
            .addOnSuccessListener { unused: Void? -> }
            .addOnFailureListener { e: Exception? -> }
    }

    private fun startDiscovery() {
        val discoveryOptions = DiscoveryOptions.Builder().setStrategy(STRATEGY).build()
        Nearby.getConnectionsClient(this)
            .startDiscovery(SERVICE_ID, endpointDiscoveryCallback, discoveryOptions)
            .addOnSuccessListener { unused: Void? -> }
            .addOnFailureListener { e: java.lang.Exception? -> }
    }
}