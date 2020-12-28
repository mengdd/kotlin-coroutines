/*
 * Copyright 2019 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.kotlinktxworkshop

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Looper
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.android.myktxlibrary.*
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
    }

    override fun onStart() {
        super.onStart()

        if (!hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)) {
            requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 0)
        }

        lifecycleScope.launch {
            try {
                getLastKnownLocation()
            } catch (e: Exception) {
                findAndSetText(R.id.textView, "Unable to get location.")
                Log.d(TAG, "Unable to get location", e)
            }
        }
        startUpdatingLocation()
    }

    private suspend fun getLastKnownLocation() {
        val lastLocation = fusedLocationClient.awaitLastLocation()
        showLocation(R.id.textView, lastLocation)
    }

    private fun startUpdatingLocation() {
        lifecycleScope.launch {
            fusedLocationClient.locationFlow()
                .conflate()
                .catch { e ->
                    findAndSetText(R.id.textView, "Unable to get location.")
                    Log.d(TAG, "Unable to get location", e)
                }
                .collect { location ->
                    showLocation(R.id.textView, location)
                    Log.d(TAG, location.toString())
                }
        }
    }

    override fun onStop() {
        super.onStop()
        if (listeningToUpdates) {
            stopUpdatingLocation()
        }
    }

    private fun stopUpdatingLocation() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            recreate()
        }
    }
}

const val TAG = "KTXCODELAB"
