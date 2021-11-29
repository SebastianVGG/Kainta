package com.app.kainta.ui.perfil

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.app.ProgressDialog
import android.content.Context
import android.content.ContextWrapper
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.graphics.drawable.Drawable
import android.media.ExifInterface
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.net.toFile
import androidx.core.net.toUri
import androidx.navigation.fragment.NavHostFragment.findNavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.app.kainta.R
import com.app.kainta.adaptadores.PerfilServiciosAdapter
import com.app.kainta.databinding.FragmentConfigPerfilBinding
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import com.google.firebase.storage.ktx.storageMetadata
import java.io.*
import java.lang.Exception
import java.util.*
import kotlin.collections.ArrayList
import kotlin.properties.Delegates


class ConfigPerfilFragment : Fragment() {
    private var _binding: FragmentConfigPerfilBinding? = null
    private val binding get() = _binding!!
    private lateinit var imagePicker: ImageView
    private lateinit var user: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var storage: FirebaseStorage
    private lateinit var progress: ProgressDialog
    private lateinit var imagePath: String
    private lateinit var dialog: Dialog
    private lateinit var serviciosArray : ArrayList<String>
    private lateinit var adaptador : PerfilServiciosAdapter
    private lateinit var progressBar: ProgressBar

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentConfigPerfilBinding.inflate(inflater, container, false)
        val root: View = binding.root

        progressBar = binding.progressBar

        user = Firebase.auth
        db = Firebase.firestore
        storage = Firebase.storage

        binding.imageviewPerfil.layoutParams.height = 300
        binding.imageviewPerfil.layoutParams.width = 300



        setup()

        return root
    }

    private fun setup() {


        db.collection("usuario").document(user.currentUser?.email!!).get()
            .addOnCompleteListener {

                if (it.isSuccessful) {

                    binding.txtNombre.text = it.result.get("nombre").toString()
                    binding.txtCiudad.text = it.result.get("ciudad").toString()
                    binding.txtDescripcion.text = it.result.get("bibliografia").toString()

                    if (!(it.result.get("facebook").toString() != "" ||
                                it.result.get("twitter").toString() != "" ||
                                it.result.get("instagram").toString() != "" ||
                                it.result.get("youtube").toString() != "" ||
                                it.result.get("web").toString() != ""
                                )
                    )
                        binding.txtRedesSociales.visibility = View.VISIBLE

                    if (it.result.get("facebook").toString() != "")
                        binding.imageFacebook.visibility = View.VISIBLE

                    if (it.result.get("twitter").toString() != "")
                        binding.imageTwitter.visibility = View.VISIBLE

                    if (it.result.get("instagram").toString() != "")
                        binding.imageInstagram.visibility = View.VISIBLE

                    if (it.result.get("youtube").toString() != "")
                        binding.imageYoutube.visibility = View.VISIBLE

                    if (it.result.get("web").toString() != "")
                        binding.imageWeb.visibility = View.VISIBLE


                    if (it.result.contains("url")) {
                        context?.let { context ->
                            Glide.with(context)
                                .load(it.result.getString("url"))
                                .apply(RequestOptions().override(300, 300))
                                .listener(object : RequestListener<Drawable> {
                                    override fun onLoadFailed(
                                        e: GlideException?,
                                        model: Any?,
                                        target: Target<Drawable>?,
                                        isFirstResource: Boolean
                                    ): Boolean {
                                        progressBar.visibility = View.GONE
                                        binding.layout.visibility = View.VISIBLE
                                        binding.btnAddServicio.visibility =
                                            View.VISIBLE
                                        return false
                                    }

                                    override fun onResourceReady(
                                        resource: Drawable?,
                                        model: Any?,
                                        target: Target<Drawable>?,
                                        dataSource: DataSource?,
                                        isFirstResource: Boolean
                                    ): Boolean {
                                        progressBar.visibility = View.GONE
                                        binding.layout.visibility = View.VISIBLE
                                        binding.btnAddServicio.visibility =
                                            View.VISIBLE
                                        return false
                                    }

                                })
                                .into(binding.imageviewPerfil)
                        }
                    } else {
                        progressBar.visibility = View.GONE
                        binding.layout.visibility = View.VISIBLE
                        binding.btnAddServicio.visibility = View.VISIBLE
                    }

                } else {
                    showAlert(
                        "Error",
                        (it.exception as FirebaseAuthException).message.toString()
                    )
                }


            }.addOnFailureListener {
                showAlert(
                    "Error",
                    (it as FirebaseAuthException).message.toString()
                )
                progressBar.visibility = View.GONE
                binding.layout.visibility = View.VISIBLE
                binding.btnAddServicio.visibility = View.VISIBLE
            }

        binding.imageviewPerfil.setOnClickListener(object :
            View.OnClickListener {
            override fun onClick(v: View?) {
                showDialog()
            }
        })


        //ADAPTADOR DE SERVICIOS
        try{
            serviciosArray = ArrayList()

            db.collection("usuario").document(user.currentUser?.email!!)
                .collection("servicios")
                .get()
                .addOnFailureListener { e ->
                    Toast.makeText(
                        context,
                        (e as FirebaseAuthException).message.toString(),
                        Toast.LENGTH_SHORT
                    ).show()
                }
                .addOnCompleteListener { documents ->
                    if(documents.isSuccessful)
                        if(!documents.result.isEmpty){
                            for ( document in documents.result){
                                serviciosArray.add(document.data["nombre"] as String)
                                println(document.data["nombre"] as String)
                            }

                            //Adaptador
                            adaptador = PerfilServiciosAdapter(
                                binding.root.context,
                                R.layout.adapter_perfil_servicios,
                                serviciosArray, object : PerfilServiciosAdapter.OnItemClickListener {
                                    @SuppressLint("ResourceType")
                                    override fun onItemClick(servicioNombre: String) {

                                        val bundle = Bundle()
                                        bundle.putString("servicioNombre", servicioNombre)
                                        findNavController().navigate(R.id.action_configPerfilFragment_to_editarServicioFragment, bundle)
                                    }
                                })

                            binding.recyclerServicios.adapter = adaptador
                            binding.recyclerServicios.layoutManager = LinearLayoutManager(requireContext())

                            binding.progressBar.visibility = View.GONE
                            binding.layout.visibility = View.VISIBLE
                            binding.btnAddServicio.visibility =
                                View.VISIBLE

                        }else{
                            binding.progressBar.visibility = View.GONE
                            binding.layout.visibility = View.VISIBLE
                            binding.btnAddServicio.visibility =
                                View.VISIBLE
                        }
                }
        }catch(e:Exception){Toast.makeText(
            context,
            "No hay registros de servicios",
            Toast.LENGTH_SHORT

        ).show()}





        binding.btnEditarInfo.setOnClickListener {
            findNavController().navigate(R.id.action_configPerfilFragment_to_editInfoPersonalFragment)
        }

        binding.btnAddServicio.setOnClickListener{
            val bundle = Bundle()
            bundle.putBoolean("fromPerfil", true)
            findNavController().navigate(R.id.action_configPerfilFragment_to_addServicioFragment, bundle)
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

    private fun showDialog() {

        dialog = Dialog(requireContext())

        dialog.setContentView(R.layout.dialog_camera_gallery)

        dialog.findViewById<ImageButton>(R.id.btnGallery)
            .setOnClickListener(object : View.OnClickListener {
                override fun onClick(v: View?) {
                    requestPermission()
                }
            })
        dialog.findViewById<ImageButton>(R.id.btnPhoto)
            .setOnClickListener(object : View.OnClickListener {
                override fun onClick(v: View?) {
                    requestCamera.launch(android.Manifest.permission.CAMERA)
                }
            })

        dialog.findViewById<ImageButton>(R.id.btn_close)
            .setOnClickListener(object : View.OnClickListener {
                override fun onClick(v: View?) {
                    dialog.dismiss()
                }
            })

        dialog.show()

    }


    private val startForActivityGallery = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (it.resultCode == Activity.RESULT_OK) {

            //Imagen de la galeria
            val data = it.data?.data
            uriToFile(requireContext(), data!!, "image_from_camera")?.let { file ->

                compressImage(file.absolutePath, 0.5)
                imagePath = file.absolutePath

            }

            progress = ProgressDialog(context)
            progress.setTitle("Subeidno arcivo...")
            progress.show()

            val storageRef = storage.reference

            val imageurl = Uri.fromFile(File(imagePath))


            val metadata = storageMetadata {
                contentType = "image/jpeg"
            }

            //Referencia de donde estará la imagen
            val spaceRef =
                storageRef.child("usuario/${(user.currentUser?.email ?: "")}/perfil/imagenPerfil")

            //Esta variable subira el archivo
            val uploadTask = imageurl?.let { it1 -> spaceRef.putFile(it1) }

            //Continua subiendo archvio y se toma la url para descargar con GLIDE
            val urlTask = uploadTask?.continueWithTask { task ->
                if (!task.isSuccessful) {
                    task.exception?.let {
                        throw it
                    }
                }
                spaceRef.downloadUrl
            }?.addOnCompleteListener { task ->

                if (task.isSuccessful) {
                    //Tomamos el url de Descarga
                    val downloadUri = task.result
                    //Se actualiza base de datos para colocar la url del usuario
                    db.collection("usuario")
                        .document(user.currentUser?.email!!)
                        .update(
                            mapOf(
                                "url" to downloadUri.toString(),
                            )
                        ).addOnSuccessListener {
                            if (progress.isShowing)
                                progress.dismiss()

                            //Se toma el url de la foto que esta almacenada
                            context?.let {
                                Glide.with(it)
                                    .load(downloadUri.toString())
                                    .apply(
                                        RequestOptions().override(
                                            300,
                                            300
                                        )
                                    )
                                    .into(binding.imageviewPerfil)
                            }

                            Toast.makeText(
                                context,
                                "Correcto",
                                Toast.LENGTH_SHORT
                            ).show()

                        }.addOnFailureListener { e ->
                            Toast.makeText(
                                context,
                                (e as FirebaseAuthException).message.toString(),
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                } else {
                    if (progress.isShowing)
                        progress.dismiss()
                    Toast.makeText(context, "Mal", Toast.LENGTH_SHORT)
                        .show()
                }
            }

        }

    }


    //PARA TOMAR UNA IMAGEN DE LA GALERIA
    private fun requestPermission() {

        dialog.dismiss()

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

        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        startForActivityGallery.launch(intent)

    }

//TOMAR FOTOS

    //PERMISOS
    private val requestCamera =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {

            dialog.dismiss()

            if (it)
                takePhoto()
            else
                Toast.makeText(
                    context,
                    "NOOOO",
                    Toast.LENGTH_SHORT
                ).show()
        }


    //FUNCION PARA TOMAR FOTOS INTENT
    private fun takePhoto() {

        val intent =
            Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE)
        startForActivityCamera.launch(intent)

    }

    //CUANDO SE TERMINA DE TOMAR FOTO HACE LO SIGUIENTE
    private val startForActivityCamera =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { activityResult ->
            if (activityResult.resultCode == Activity.RESULT_OK) {
                //FOTO
                try {
                    val imageBitmap = activityResult.data?.extras?.get("data") as Bitmap
                    val imageUri = bitmapToFile(imageBitmap)
                    uriToFile(requireContext(), imageUri, "image_from_camera")?.let { file ->

                        compressImage(file.absolutePath, 0.5)
                        imagePath = file.absolutePath

                    }
                    progress = ProgressDialog(context)
                    progress.setTitle("Subeidno arcivo...")
                    progress.show()

                    val imageurl = Uri.fromFile(File(imagePath))

                    val storageRef = storage.reference

                    val metadata = storageMetadata {
                        contentType = "image/jpg"
                    }

                    //Referencia de donde estará la imagen
                    val spaceRef =
                        storageRef.child("usuario/${(user.currentUser?.email ?: "")}/perfil/imagenPerfil")

                    //Esta variable subira el archivo
                    val uploadTask = spaceRef.putFile(imageurl)

                    //Continua subiendo archvio y se toma la url para descargar con GLIDE
                    val urlTask = uploadTask.continueWithTask { task ->
                        if (!task.isSuccessful) {
                            task.exception?.let {
                                throw it
                            }
                        }
                        spaceRef.downloadUrl
                    }.addOnCompleteListener { task ->

                        if (task.isSuccessful) {
                            //Tomamos el url de Descarga
                            val downloadUri = task.result
                            //Se actualiza base de datos para colocar la url del usuario
                            db.collection("usuario")
                                .document(user.currentUser?.email!!)
                                .update(
                                    mapOf(
                                        "url" to downloadUri.toString(),
                                    )
                                ).addOnSuccessListener {
                                    if (progress.isShowing)
                                        progress.dismiss()

                                    //Se toma el url de la foto que esta almacenada
                                    context?.let {
                                        Glide.with(it)
                                            .load(downloadUri.toString())
                                            .apply(
                                                RequestOptions().override(
                                                    300,
                                                    300
                                                )
                                            )
                                            .into(binding.imageviewPerfil)
                                    }

                                    Toast.makeText(
                                        context,
                                        "Correcto",
                                        Toast.LENGTH_SHORT
                                    ).show()

                                }.addOnFailureListener { e ->
                                    Toast.makeText(
                                        context,
                                        (e as FirebaseAuthException).message.toString(),
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }

                        } else {
                            if (progress.isShowing)
                                progress.dismiss()
                            println((task.exception as FirebaseException).message.toString())
                        }
                    }

                } catch (e: Exception) {
                    Toast.makeText(
                        context,
                        e.toString(),
                        Toast.LENGTH_SHORT
                    ).show()
                }

            } else {
                Toast.makeText(
                    context,
                    "Error el resultado no es OK",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

    private fun uriToFile(context : Context, uri : Uri, fileName : String) : File?{

        context.contentResolver.openInputStream(uri)?.let { inputStream ->

            val tempFile : File = createImageFile(fileName)
            val fileOutputSteam = FileOutputStream(tempFile)

            inputStream.copyTo(fileOutputSteam)
            inputStream.close()
            fileOutputSteam.close()

            return tempFile

        }

        return null
    }

    private fun createImageFile(fileName: String = "temp_image"): File {

        val storageDir = requireActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(fileName, ".jpg", storageDir)

    }

    private fun compressImage(filePath : String, targetMB : Double = 1.0){

        var image : Bitmap = BitmapFactory.decodeFile(filePath)

        val exif = ExifInterface(filePath)
        val exifOrientation : Int = exif.getAttributeInt(
            ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL
        )

        val exifDegree : Int = exifOrientationToDegress(exifOrientation)

        image = rotateImage(image, exifDegree.toFloat())

        try {

            val file = File(filePath)
            val lenght = file.length()

            val fileSizeInKB = (lenght / 1024).toString().toDouble()
            val fileSizeInMB = (fileSizeInKB / 1024).toString().toDouble()

            var quality = 100
            if(fileSizeInMB > targetMB){
                quality = ((targetMB / fileSizeInMB) * 100).toInt()
            }

            val fileOutputStream = FileOutputStream(filePath)
            image.compress(Bitmap.CompressFormat.JPEG, quality, fileOutputStream)
            fileOutputStream.close()

        }catch (e:Exception){
            e.printStackTrace()
        }

    }

    private fun rotateImage(source : Bitmap, angle : Float): Bitmap{

        val matrix = Matrix()
        matrix.postRotate(angle)

        return Bitmap.createBitmap(source, 0, 0, source.width, source.height, matrix, true)

    }

    private fun exifOrientationToDegress(exifOrientation: Int): Int {

        return  when (exifOrientation){
            ExifInterface.ORIENTATION_ROTATE_90 -> {
                90
            }
            ExifInterface.ORIENTATION_ROTATE_180 -> {
                180
            }
            ExifInterface.ORIENTATION_ROTATE_270 -> {
                270
            }
            else -> 0
        }

    }

    private fun bitmapToFile(bitmap: Bitmap): Uri { // File name like "image.png"
        val wrapper = ContextWrapper(requireContext())
        var file = wrapper.getDir("Images", Context.MODE_PRIVATE)
        file = File(file, "${UUID.randomUUID()}.jpg")
        val stream: OutputStream = FileOutputStream(file)
        bitmap.compress(Bitmap.CompressFormat.JPEG, 25, stream)
        stream.flush()
        stream.close()
        return file.toUri()
    }
}




