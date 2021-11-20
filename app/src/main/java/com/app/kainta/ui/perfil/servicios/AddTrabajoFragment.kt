package com.app.kainta.ui.perfil.servicios

import android.app.Activity
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.app.kainta.databinding.FragmentAddTrabajoBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import com.app.kainta.R
import com.app.kainta.adaptadores.ServiciosImagesAdapter
import com.app.kainta.adaptadores.ServiciosImagesURLAdapter
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import com.google.firebase.storage.ktx.storageMetadata
import java.lang.Exception
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.collections.ArrayList


class AddTrabajoFragment : Fragment() {
    private var _binding: FragmentAddTrabajoBinding? = null
    private val binding get() = _binding!!
    private lateinit var servicio : String
    private var nuevoServicio : Boolean = true
    private lateinit var user : FirebaseAuth
    private lateinit var db : FirebaseFirestore
    private lateinit var storage: FirebaseStorage
    private lateinit var mArrayUri: ArrayList<Uri>
    private lateinit var listURLS : ArrayList<String>
    private lateinit var adaptador : ServiciosImagesAdapter

    var position = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentAddTrabajoBinding.inflate(inflater, container, false)
        val root: View = binding.root

        user = Firebase.auth
        db = Firebase.firestore
        storage = Firebase.storage

        mArrayUri = ArrayList<Uri>()


        servicio = arguments?.getString("servicio").toString().lowercase()
        try {
            if(arguments?.containsKey("nuevo") as Boolean)
            nuevoServicio = arguments?.getBoolean("nuevo") as Boolean
        }catch (e : Exception){}

        setup()

        /*next.setOnClickListener {
            if (position < mArrayUri.size - 1) {
                // increase the position by 1
                position++
                imageView.setImageURI(mArrayUri[position])
            } else {
                Toast.makeText(context, "Last Image Already Shown", Toast.LENGTH_SHORT)
                    .show()
            }
        }*/

        // click here to select image

        // click here to select image





        return root
    }

    private fun setup() {

        binding.btnAddFotos.setOnClickListener { // initialising intent
            mArrayUri = ArrayList()
            requestPermission()
        }

        binding.btnAceptar.setOnClickListener {

            binding.progessBar.visibility = View.VISIBLE
            val titulo = binding.editTitulo.text.toString()
            val descripcion = binding.editDescripcion.text.toString()

            listURLS = ArrayList()

            val storageRef = storage.reference

            val metadata = storageMetadata {
                contentType = "image/jpeg"
            }

            //Referencia de donde estará la imagen

            try {
                for (i in 0 until mArrayUri.size) {
                    // adding imageuri in array
                    val imageurl = mArrayUri[i]

                    val spaceRef =
                        storageRef.child("usuario/${(user.currentUser?.email ?: "")}/servicios/$servicio/trabajos/$titulo/${titulo}$i")

                    //Esta variable subira el archivo
                    val uploadTask = imageurl.let { it1 -> spaceRef.putFile(it1) }

                    //Continua subiendo archvio y se toma la url para descargar con GLIDE
                    val urlTask = uploadTask.continueWithTask { task ->
                        if (!task.isSuccessful) {
                            task.exception?.let {
                                throw it
                            }
                        }
                        spaceRef.downloadUrl
                    }
                        .addOnCompleteListener { task ->

                        if (task.isSuccessful) {
                            //Tomamos el url de Descarga
                            val downloadUri = task.result
                            listURLS.add(downloadUri.toString())

                            if(listURLS.size == mArrayUri.size) {
                                for (j in 0 until mArrayUri.size) {
                                    //Se actualiza base de datos para colocar la url del usuario
                                    db.collection("usuario").document(user.currentUser?.email!!)
                                        .collection("servicios").document(servicio)
                                        .collection("trabajos").document(titulo)
                                        .set(
                                            mapOf(
                                                "url$j" to listURLS[j],
                                                "titulo" to titulo,
                                                "descripcion" to descripcion
                                            ), SetOptions.merge()
                                        )
                                        .addOnFailureListener { e ->
                                            Toast.makeText(
                                                context,
                                                (e as FirebaseAuthException).message.toString(),
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                }
                                requireActivity().onBackPressedDispatcher
                                binding.progessBar.visibility = View.INVISIBLE
                            }


                        } else {
                            showAlert("Error", (task.exception as FirebaseException).message.toString())
                        }
                    }

                }



                if(nuevoServicio){
                    db.collection("usuario").document(user.currentUser?.email!!)
                        .collection("servicios").document(servicio)
                        .set(
                            mapOf(
                                "nombre" to servicio
                            ), SetOptions.merge())
                        .addOnSuccessListener {

                            val sdf = SimpleDateFormat("dd/MM/yyyy")
                            val currentDate = sdf.format(Date())

                            db.collection("servicios").document(servicio)
                                .collection("usuario").document(user.currentUser?.email!!)
                                .set(
                                    mapOf(
                                        "correo" to user.currentUser?.email!!,
                                        "fecha" to currentDate
                                    ))
                                .addOnSuccessListener {
                                    db.collection("servicios").document(servicio)
                                        .set(
                                            mapOf(
                                                "nombre" to servicio
                                            ), SetOptions.merge())
                                }
                                .addOnFailureListener { e ->
                                    Toast.makeText(
                                        context,
                                        (e as FirebaseAuthException).message.toString(),
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(
                                context,
                                (e as FirebaseAuthException).message.toString(),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                }
            }catch (e : Exception){
                Toast.makeText(context, e.toString(), Toast.LENGTH_SHORT)
                    .show()
            }
            }
    }


    private fun showAlert(titulo: String, mensaje: String) {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle(titulo)
        builder.setMessage(mensaje)
        builder.setPositiveButton("Aceptar", null)
        val dialog: AlertDialog = builder.create()
        dialog.show()
    }



    //PARA TOMAR UNA IMAGEN DE LA GALERIA
    private fun requestPermission() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            when {

                requireActivity().let {
                    ContextCompat.checkSelfPermission(
                        it,
                        android.Manifest.permission.READ_EXTERNAL_STORAGE
                    )
                } == PackageManager.PERMISSION_GRANTED ->
                    pickPhotoFromGallery()

                else -> requestePermissionLauncher.launch(android.Manifest.permission.READ_EXTERNAL_STORAGE)
            }

        } else {
            pickPhotoFromGallery()
        }

    }


    private val requestePermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) {
        if (it) {
            pickPhotoFromGallery()
        } else {
            Toast.makeText(
                context,
                "Se necesitan permisos",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun pickPhotoFromGallery() {

        val intent = Intent()

        // setting type to select to be image
        intent.type = "image/*"

        // allowing multiple image to be selected
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        intent.action = Intent.ACTION_GET_CONTENT

        startForActivityGallery.launch(Intent.createChooser(intent, "Select Picture"))

    }

    private val startForActivityGallery = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (it.resultCode == Activity.RESULT_OK) {

            //Imagen de la galeria
            val data = it.data
            // Get the Image from data
            if (data != null) {
                position = if (data.clipData != null) {
                    val mClipData = data.clipData
                    val cout = data.clipData!!.itemCount
                    for (i in 0 until cout) {
                        // adding imageuri in array
                        val imageurl = data.clipData!!.getItemAt(i).uri
                        mArrayUri.add(imageurl)
                    }
                    // setting 1st selected image into image switcher
                    0
                } else {
                    val imageurl = data.data
                    mArrayUri.add(imageurl!!)
                    0
                }
            }

            //Adaptador
            adaptador = ServiciosImagesAdapter(
                binding.root.context,
                R.layout.adapter_servicios_images,
                mArrayUri, object : ServiciosImagesAdapter.OnItemClickListener {
                    override fun onItemClick(uri: Uri) {
                        Toast.makeText(context, "Se pico la imagen"+uri.userInfo, Toast.LENGTH_SHORT).show()
                    }
                })

            binding.recyclerImages.adapter = adaptador
            binding.recyclerImages.layoutManager = GridLayoutManager(context, 3)

        }else{
            // show this if no image is selected
            Toast.makeText(context, "You haven't picked Image", Toast.LENGTH_LONG).show()
        }
    }


}