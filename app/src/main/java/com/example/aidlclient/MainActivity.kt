package com.example.aidlclient

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.IBinder
import android.os.Process
import android.view.View
import android.widget.Toast
import com.example.aidlclient.databinding.ActivityMainBinding
import com.example.aidlserver.IMyAidlInterface

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var connected = false
    private var myAidlInterface : IMyAidlInterface? = null
    private lateinit var mServiceConn : ServiceConnection

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    override fun onStart() {
        super.onStart()

        object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                // Gets an instance of the AIDL interface named IMyAidlInterface,
                // which we can use to call on the service
                myAidlInterface = IMyAidlInterface.Stub.asInterface(service)
                binding.txtServerPid.text = myAidlInterface?.pid.toString()
                binding.txtServerConnectionCount.text = myAidlInterface?.connectionCount.toString()
                myAidlInterface?.setDisplayedValue(
                    applicationContext?.packageName,
                    Process.myPid(),
                    binding.edtClientData.text.toString())
                connected = true
            }

            override fun onServiceDisconnected(name: ComponentName?) {
                Toast.makeText(applicationContext, "AIDL server has disconnected unexpectedly", Toast.LENGTH_LONG).show()
                myAidlInterface = null
                connected = false
            }

        }.also { mServiceConn = it }

        binding.btnConnect.setOnClickListener {
            connected = if (connected) {
                disconnectToRemoteService()
                binding.txtServerPid.text = ""
                binding.txtServerConnectionCount.text = ""
                binding.btnConnect.text = getString(R.string.connect)
                binding.linearLayoutClientInfo.visibility = View.INVISIBLE
                false
            } else {
                connectToRemoteService()
                binding.linearLayoutClientInfo.visibility = View.VISIBLE
                binding.btnConnect.text = getString(R.string.disconnect)
                true
            }
        }
    }

    private fun connectToRemoteService() {
        val intent = Intent("AidlServerService")
        val pack = IMyAidlInterface::class.java.`package`
        pack?.let {
            intent.setPackage(it.name)
            applicationContext?.bindService(
                intent, mServiceConn, Context.BIND_AUTO_CREATE
            )
        }
    }

    private fun disconnectToRemoteService() {
        if(connected){
            applicationContext?.unbindService(mServiceConn)
        }
    }
}