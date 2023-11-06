package com.example.taller3movil

import android.app.Application
import com.parse.Parse

class ParseConnection : Application() {
    override fun onCreate() {
        super.onCreate()
        Parse.initialize(
            Parse.Configuration.Builder(this)
                .applicationId("ParseAppId")
                .clientKey("") // should correspond to Application Id env variable
                .server(IP_GCP)
                .build()
        )
    }

    companion object {
        const val IP_GCP = "http://34.83.126.117:1337/parse"
    }
}