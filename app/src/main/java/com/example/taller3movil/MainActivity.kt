package com.example.taller3movil

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.example.taller3movil.databinding.ActivityMainBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.parse.ParseObject
import com.parse.ParseQuery

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityMainBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.login.setOnClickListener {
            val user = binding.username.text.toString()
            val pass = binding.password.text.toString()
            if (validateForm()){
                validateUser(user, pass)
            }
        }
        binding.register.setOnClickListener{
            startActivity(Intent(baseContext, RegisterActivity::class.java))
        }
    }

    fun validateUser(user: String, pass: String){
        var correcto = true
        val query: ParseQuery<ParseObject> = ParseQuery.getQuery("LoginUser")
        Log.i("Query", query.toString())
        query.findInBackground { objects, _ ->
            if (objects != null) {
                for (row in objects) {
                    val name = row["username"] as String?
                    val password = row["password"] as String?
                    if(name.equals(user)&&password.equals(pass)){
                        startActivity(Intent(baseContext, HomeActivity::class.java))
                        correcto = true
                        break;
                    } else{
                        correcto = false
                    }
                }
                if(correcto == false){
                    binding.info.text = "Usuario o contraseña incorrectos"
                }
            }
        }
    }

    fun validateForm(): Boolean {
        if(binding.username.text.toString()==""|| binding.password.text.toString()==""){
            Toast.makeText(baseContext, "Complete los campos necesarios", Toast.LENGTH_LONG).show()
        }else{
            return true
        }
        return false
    }
}