package com.example.taller3movil

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CursorAdapter
import android.widget.ImageView
import android.widget.TextView
import com.google.firebase.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.storage
import kotlinx.coroutines.tasks.await
import java.io.File
import java.io.IOException

class UsersAdapter(context: Context?, c: Cursor?, flags: Int) :
    CursorAdapter(context, c, flags) {
    lateinit var storageRef : StorageReference
    lateinit var firebaseStorage: FirebaseStorage
    override fun newView(context: Context?, cursor: Cursor?, parent: ViewGroup?): View {
        return LayoutInflater.from(context).inflate(R.layout.list_item_layout, parent, false)
    }
    override fun bindView(view: View?, context: Context?, cursor: Cursor?) {
        firebaseStorage = Firebase.storage
        storageRef = firebaseStorage.reference
        val tvName = view!!.findViewById<TextView>(R.id.contactName)
        val name = cursor!!.getString(0)
        tvName.text = name
        val imageView = view!!.findViewById<ImageView>(R.id.fotoPerfil)
        val imageUri = downloadFile(name)
        if (imageUri != null) {
            imageView.setImageURI(imageUri)
        }
    }

    @Throws(IOException::class)
    fun downloadFile(name: String): Uri? {
        val localFile = File.createTempFile("images", "jpg")
        val imageRef: StorageReference = storageRef.child("imagenes/$name")

        try {
            imageRef.getFile(localFile)
                .addOnSuccessListener {
                    Log.i("FBApp", "successfully downloaded")
                    // Puedes obtener el Uri del archivo localFile
                    val fileUri = Uri.fromFile(localFile)
                    // Actualiza la interfaz de usuario utilizando el fileUri si es necesario
                }.addOnFailureListener {
                    // Maneja los errores de descarga aquí si es necesario
                }
        } catch (e: Exception) {
            // Maneja las excepciones aquí si es necesario
            return null
        }

        // Devuelve el Uri del archivo descargado
        return Uri.fromFile(localFile)
    }
}