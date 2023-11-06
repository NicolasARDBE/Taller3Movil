package com.example.taller3movil

import android.Manifest
import android.content.ContentValues
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.example.taller3movil.databinding.ActivityRegisterBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.storage
import com.parse.ParseObject
import com.parse.livequery.ParseLiveQueryClient
import java.io.File
import java.net.URI
import java.util.UUID


class RegisterActivity : AppCompatActivity() {

    val getSimplePermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
        ActivityResultCallback {
            getLocation(it)
        })

    lateinit var storageRef : StorageReference
    lateinit var firebaseStorage: FirebaseStorage
    var firstLocationClient: Location = Location("provider")
    lateinit var locationClient: FusedLocationProviderClient
    lateinit var cameraUri : Uri

    private lateinit var binding: ActivityRegisterBinding
    val getContentGallery = registerForActivityResult(
        ActivityResultContracts.GetContent(),
        ActivityResultCallback {
            loadImage(it!!)
            cameraUri = it
        }
    )

    val getContentCamera = registerForActivityResult(
        ActivityResultContracts.TakePicture(),
        ActivityResultCallback {
            if(it){
                loadImage(cameraUri)
            }
        })

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        locationClient = LocationServices.getFusedLocationProviderClient(this)
        firebaseStorage = Firebase.storage
        binding.registeruser.setOnClickListener {
            saveData()
        }

        binding.gallery.setOnClickListener {
            getContentGallery.launch("image/*")
        }

        binding.camera.setOnClickListener {
            val file = File(getFilesDir(), "picFromCamera");
            cameraUri = FileProvider.getUriForFile(baseContext,baseContext.packageName + ".fileprovider", file)
            getContentCamera.launch(cameraUri)
        }

        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_DENIED){
            if(shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)){
                Toast.makeText(this, "The permission is required to use the map functionality", Toast.LENGTH_LONG).show()
            }
            getSimplePermission.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            getSimplePermission.launch(Manifest.permission.ACCESS_COARSE_LOCATION)
        } else{
            getLocation(true)
        }
    }


    //Guardar lo demás en livequery
    fun saveData() {
        if(validateForm()) {
            val parseLiveQueryClient = ParseLiveQueryClient.Factory.getClient()
            //Guardar la información
            uploadFile(cameraUri)
            Log.i(ContentValues.TAG, "Attempt to write on parse");
            var firstObject = ParseObject.create("LoginUser")
            val username = binding.email.getText().toString();
            val password = binding.contrasenia.getText().toString();
            firstObject.put("username", username);
            firstObject.put("password", password);
            firstObject.saveInBackground {
                if (it != null) {
                    Log.e(ContentValues.TAG, it.getLocalizedMessage());
                } else {
                    Log.d(ContentValues.TAG, "Object saved.");
                    finish()
                }
            }
        }
    }

    fun validateForm(): Boolean {
        if(binding.nombre.text.toString()==""|| binding.apellido.text.toString()=="" || binding.email.text.toString()==""
            || binding.contrasenia.text.toString()=="" || binding.id.text.toString()=="" || cameraUri==null || firstLocationClient.longitude == null){
            Toast.makeText(baseContext, "Complete los campos necesarios", Toast.LENGTH_LONG).show()
        }else{
            return true
        }
        return false
    }

    fun getLocation(permission : Boolean){
        if(permission){
            //Granted
            if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                locationClient.lastLocation.addOnSuccessListener {
                    if (it != null){
                        firstLocationClient.longitude= it.longitude
                        firstLocationClient.latitude = it.latitude
                    }
                }
            }
        }
    }

    private fun uploadFile(cameraUri : Uri) {
        storageRef = firebaseStorage.reference
        val username = binding.email.getText().toString();
        val imageRef: StorageReference = storageRef.child("imagenes/$username")
        imageRef.putFile(cameraUri)
            .addOnSuccessListener { // Get a URL to the uploaded content
                Log.i("Holi", "Succesfully upload image")
            }
            .addOnFailureListener {
                Log.i("Holi", "Problemas al subir la img ${it.printStackTrace()}")
            }
    }

    fun convertAndroidUriToJavaUri(androidUri: Uri): URI {
        // Convert the Android Uri to a string
        val androidUriString = androidUri.toString()

        // Create a Java URI from the string
        return URI(androidUriString)
    }
    fun loadImage(uri : Uri){
        val imageStream = getContentResolver().openInputStream(uri)
        val bitmap = BitmapFactory.decodeStream(imageStream)
        binding.image.setImageBitmap(bitmap)
    }
}