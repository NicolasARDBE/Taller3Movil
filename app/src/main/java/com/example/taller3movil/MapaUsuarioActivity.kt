package com.example.taller3movil

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.taller3movil.databinding.ActivityMapaUsuarioBinding

class MapaUsuarioActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMapaUsuarioBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        binding= ActivityMapaUsuarioBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        val userName = intent.getStringExtra("userName")
    }
}