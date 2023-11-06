package com.example.taller3movil

import com.parse.ParseObject

class User : ParseObject() {
    lateinit var username : String
    lateinit var password : String
}