package com.example.nearbyapp

import adapters.MessageAdapter
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.connection.AdvertisingOptions
import com.google.android.gms.nearby.connection.ConnectionInfo
import com.google.android.gms.nearby.connection.ConnectionLifecycleCallback
import com.google.android.gms.nearby.connection.ConnectionResolution
import com.google.android.gms.nearby.connection.ConnectionsStatusCodes
import com.google.android.gms.nearby.connection.DiscoveredEndpointInfo
import com.google.android.gms.nearby.connection.DiscoveryOptions
import com.google.android.gms.nearby.connection.EndpointDiscoveryCallback
import com.google.android.gms.nearby.connection.Payload
import com.google.android.gms.nearby.connection.PayloadCallback
import com.google.android.gms.nearby.connection.PayloadTransferUpdate
import com.google.android.gms.nearby.connection.Strategy
import models.Message
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.util.UUID


private val STRATEGY = Strategy.P2P_CLUSTER

class MainActivity : AppCompatActivity() {
    private val SERVICE_ID = "com.example.nearbyapp.id"
    private val LOCAL_USERNAME = "${android.os.Build.MANUFACTURER} ${android.os.Build.MODEL}"

    private lateinit var messageList: ListView
    private val messages = mutableListOf<Message>()
    private val connections = mutableListOf<String>()
    private lateinit var messageBox: EditText
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        messageList = findViewById(R.id.messageList)
        messageList.adapter = MessageAdapter(this, messages)

        messageBox = findViewById(R.id.messageBox)

        findViewById<TextView>(R.id.name).text = "Yo: ${LOCAL_USERNAME}"

        findViewById<Button>(R.id.sendButton).setOnClickListener {
            val written = messageBox.text
                .toString()

            val message = Message().apply {
                sender = LOCAL_USERNAME.toString()
                content = written
            }

            val byteArrayOutputStream = ByteArrayOutputStream()
            val objectOutputStream = ObjectOutputStream(byteArrayOutputStream)
            objectOutputStream.writeObject(message)
            val byteArray = byteArrayOutputStream.toByteArray()

            val bytesPayload = Payload.fromBytes(byteArray)

            connections.forEach {
                Nearby.getConnectionsClient(this@MainActivity).sendPayload(it, bytesPayload)
            }

            messages.add(message)
            (messageList.adapter as MessageAdapter).notifyDataSetChanged()
            messageList.smoothScrollToPosition(messages.size - 1)

            messageBox.text.clear()
        }

        startAdvertising()
        startDiscovery()
    }

    private fun startAdvertising() {
        val advertisingOptions = AdvertisingOptions.Builder().setStrategy(STRATEGY).build()
        Nearby.getConnectionsClient(this)
            .startAdvertising(
                LOCAL_USERNAME, SERVICE_ID, connectionLifecycleCallback, advertisingOptions
            )
            .addOnSuccessListener { }
            .addOnFailureListener { }
    }

    private fun startDiscovery() {
        val discoveryOptions = DiscoveryOptions.Builder().setStrategy(STRATEGY).build()
        Nearby.getConnectionsClient(this)
            .startDiscovery(SERVICE_ID, endpointDiscoveryCallback, discoveryOptions)
            .addOnSuccessListener { }
            .addOnFailureListener { }
    }

    private val endpointDiscoveryCallback: EndpointDiscoveryCallback =
        object : EndpointDiscoveryCallback() {
            override fun onEndpointFound(endpointId: String, info: DiscoveredEndpointInfo) {
                // An endpoint was found. We request a connection to it.
                Nearby.getConnectionsClient(this@MainActivity)
                    .requestConnection(LOCAL_USERNAME.toString(), endpointId, connectionLifecycleCallback)
                    .addOnSuccessListener { }
                    .addOnFailureListener { }
            }

            override fun onEndpointLost(endpointId: String) {
                // A previously discovered endpoint has gone away.
            }
        }

    private val connectionLifecycleCallback: ConnectionLifecycleCallback =
        object : ConnectionLifecycleCallback() {
            override fun onConnectionInitiated(endpointId: String, connectionInfo: ConnectionInfo) {
                // Automatically accept the connection on both sides.
                Nearby.getConnectionsClient(this@MainActivity).acceptConnection(endpointId, payloadCallback)
            }

            override fun onConnectionResult(endpointId: String, result: ConnectionResolution) {
                when (result.status.statusCode) {
                    ConnectionsStatusCodes.STATUS_OK -> {
                        connections.add(endpointId)
                    }
                    ConnectionsStatusCodes.STATUS_CONNECTION_REJECTED -> {}
                    ConnectionsStatusCodes.STATUS_ERROR -> {}
                    else -> {}
                }
            }

            override fun onDisconnected(endpointId: String) {
                // We've been disconnected from this endpoint. No more data can be
                // sent or received.
            }
        }

    private val payloadCallback: PayloadCallback =
        object : PayloadCallback() {
            override fun onPayloadReceived(p0: String, p1: Payload) {
                if (p1.type == Payload.Type.BYTES) {
                    val receivedBytes: ByteArray? = p1.asBytes()

                    receivedBytes?.let {
                        val byteArrayInputStream = ByteArrayInputStream(it)
                        val objectInputStream = ObjectInputStream(byteArrayInputStream)
                        val message = objectInputStream.readObject() as Message

                        messages.add(message)
                        (messageList.adapter as MessageAdapter).notifyDataSetChanged()
                        messageList.smoothScrollToPosition(messages.size - 1)
                    }
                }
            }

            override fun onPayloadTransferUpdate(p0: String, p1: PayloadTransferUpdate) {

            }

        }
}