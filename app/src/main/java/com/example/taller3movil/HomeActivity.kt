package com.example.taller3movil

import android.Manifest
import android.content.IntentSender
import android.content.pm.PackageManager
import android.content.res.Resources
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import com.example.taller3movil.databinding.ActivityHomeBinding
import com.example.taller3movil.services.LocationService
import com.example.taller3movil.services.MapEventServices
import com.example.taller3movil.services.MapRenderingServices
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.LocationSettingsResponse
import com.google.android.gms.location.SettingsClient
import com.google.android.gms.tasks.Task
import org.json.JSONArray
import org.json.JSONObject
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.io.InputStream
import java.util.Date
import java.util.Scanner

class HomeActivity : AppCompatActivity(), LocationService.LocationUpdateListener {
    private val getSimplePermission= registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { }
    private lateinit var binding: ActivityHomeBinding
    private lateinit var locationService: LocationService
    private lateinit var map: MapView
    private lateinit var mapRenderingService: MapRenderingServices
    private lateinit var mapEventService: MapEventServices

    private val locationSettings= registerForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult()
    ) {
        if (it.resultCode == RESULT_OK) {
            locationService.startLocationUpdates()
        } else {
            //Todo
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityHomeBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        locationService = LocationService(this, this)
        map= binding.mapa
        mapRenderingService= MapRenderingServices(this,map)
        mapEventService= MapEventServices(map, mapRenderingService)
        mapEventService.createOverlayEvents()

        val resourceId = R.raw.locations // Reemplaza con el nombre de tu archivo JSON
        val jsonObject = readJSONFromRaw(resources, resourceId)

        //map.controller.setZoom(30.0)

        if (jsonObject != null) {
            val locations = jsonObject.getJSONObject("locations")
            val locationsArray = jsonObject.getJSONArray("locationsArray")

            for (i in 0 until locationsArray.length()) {

                val jsonObject = locationsArray.getJSONObject(i)
                val latitude = jsonObject.getDouble("latitude")
                val longitude = jsonObject.getDouble("longitude")
                val name = jsonObject.getString("name")
                Log.i("Nicolas", "$latitude, $longitude")
                var ruta = GeoPoint(latitude, longitude)
                mapRenderingService.addMarker(ruta, typeMarker = 'B', descr = name)
                Log.i("lectura", "longitud: $longitude, latitud: $latitude")
            }
        } else {

        }

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)){
                Toast.makeText(this, "Se necesita la ubicacion!!", Toast.LENGTH_LONG).show()
            }
            getSimplePermission.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
        locationService.locationClient.lastLocation.addOnSuccessListener {
            if(it!=null){
                val geo= GeoPoint(it.latitude, it.longitude)
                mapRenderingService.addMarker(geo, typeMarker = 'A', descr = "")
                mapRenderingService.currentLocation= MyLocation(Date(System.currentTimeMillis()),GeoPoint(it.latitude, it.longitude))
                mapRenderingService.center(geo)
                updateUI(it)
            }else{
                locationSettings()
            }
        }
    }

    private fun locationSettings(){
        val builder= LocationSettingsRequest.Builder().addLocationRequest(locationService.locationRequest)
        val client: SettingsClient = LocationServices.getSettingsClient(this)
        val task: Task<LocationSettingsResponse> = client.checkLocationSettings(builder.build())
        task.addOnSuccessListener {
            locationService.startLocationUpdates()
        }
        task.addOnFailureListener{
            if(it is ResolvableApiException){
                try {
                    val isr: IntentSenderRequest = IntentSenderRequest.Builder(it.resolution).build()
                    locationSettings.launch(isr)
                }catch (sendEx: IntentSender.SendIntentException){
                    //eso!!
                }
            }
        }
    }

    override fun onLocationUpdate(location: Location) {
        updateUI(location)
    }

    private fun updateUI(location: Location){
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
                Toast.makeText(this, "Se necesita la ubicacion por favor!", Toast.LENGTH_LONG)
                    .show()
            }
            getSimplePermission.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            return
        } else {
            val distancia = mapRenderingService.currentLocation.distance(GeoPoint(location.latitude, location.longitude))
            Log.i("distancia", "la distancia es: $distancia eso")
            mapRenderingService.currentLocation.geoPoint= GeoPoint(location.latitude,location.longitude)
            mapRenderingService.addMarker(mapRenderingService.currentLocation.geoPoint, typeMarker = 'A', descr = "")
            if(distancia>=30){
                mapRenderingService.currentLocation.fecha=Date(System.currentTimeMillis())
                mapRenderingService.center(mapRenderingService.currentLocation.geoPoint)
            }
        }
    }

    fun readJSONFromRaw(resources: Resources, resourceId: Int): JSONObject? {
        try {
            val inputStream: InputStream = resources.openRawResource(resourceId)
            val jsonContent = Scanner(inputStream).useDelimiter("\\A").next()
            return JSONObject(jsonContent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    fun readLocationsFromFile(filePath: String){
        Log.i("lectura", "file: $filePath")
        val file = File(filePath)
        if (file.exists()) {
            try {
                val input = BufferedReader(FileReader(file))
                val jsonContent = input.readText()
                input.close()
                val locations = mutableListOf<Pair<Double, Double>>()
                val jsonArray = JSONArray(jsonContent)
                for (i in 0 until jsonArray.length()) {
                    val jsonObject = jsonArray.getJSONObject(i)
                    val latitude = jsonObject.getDouble("latitude")
                    val longitude = jsonObject.getDouble("longitude")
                    val name = jsonObject.getString("name")
                    var ruta = GeoPoint(latitude, longitude)
                    mapRenderingService.addMarker(ruta, typeMarker = 'C', descr = name)
                    Log.i("lectura", "longitud: $longitude, latitud: $latitude")
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } else {
        }
    }
}