package com.example.taller3movil

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.get
import com.example.taller3movil.databinding.ActivityUsuariosDisponiblesBinding
import com.google.firebase.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.storage
import java.io.File
import java.io.IOException


class UsuariosDisponiblesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityUsuariosDisponiblesBinding
    lateinit var adapter : UsersAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityUsuariosDisponiblesBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val density = resources.displayMetrics.density
        adapter = UsersAdapter(this, null, 0)
        binding.listaUsuarios.adapter=adapter
        val posActualButton = findViewById<Button>(R.id.posActual)
        posActualButton.setOnClickListener(){
            val nameTextView = it.findViewById<TextView>(R.id.contactName)
            val userName = nameTextView.text.toString()
            val intent = Intent(this, MapaUsuarioActivity::class.java)
            intent.putExtra("userName", userName)
            startActivity(intent)
        }
    }
}