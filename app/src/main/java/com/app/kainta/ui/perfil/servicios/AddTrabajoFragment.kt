package com.app.kainta.ui.perfil.servicios

import android.app.Activity
import android.app.Activity.RESULT_OK
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
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
import androidx.core.content.ContextCompat


class AddTrabajoFragment : Fragment() {
    private var _binding: FragmentAddTrabajoBinding? = null
    private val binding get() = _binding!!
    private lateinit var servicio : String
    private lateinit var user : FirebaseAuth
    private lateinit var db : FirebaseFirestore
    private lateinit var mArrayUri: ArrayList<Uri>
    var position = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentAddTrabajoBinding.inflate(inflater, container, false)
        val root: View = binding.root

        user = Firebase.auth
        db = Firebase.firestore

        mArrayUri = ArrayList<Uri>()


        servicio = arguments?.getString("servicio").toString()


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
        binding.btnAddFotos.setOnClickListener { // initialising intent
            requestPermission()
        }

        return root
    }

    private fun setup() {

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
                    imageView.setImageURI(mArrayUri[0])
                    0
                } else {
                    val imageurl = data.data
                    mArrayUri.add(imageurl!!)
                    imageView.setImageURI(mArrayUri[0])
                    0
                }
            }

        }else{
            // show this if no image is selected
            Toast.makeText(context, "You haven't picked Image", Toast.LENGTH_LONG).show()
        }
    }


}