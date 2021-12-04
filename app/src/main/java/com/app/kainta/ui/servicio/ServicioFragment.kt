package com.app.kainta.ui.servicio

import android.annotation.SuppressLint
import android.graphics.drawable.Drawable
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.app.kainta.R
import com.app.kainta.adaptadores.PerfilServiciosAdapter
import com.app.kainta.databinding.FragmentServicioBinding
import com.app.kainta.mvc.UsuarioServicioViewModel
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import org.json.JSONObject
import java.lang.Exception


class ServicioFragment : Fragment() {
    private var _binding: FragmentServicioBinding? = null
    private val binding get() = _binding!!
    private lateinit var imagePicker: ImageView
    private lateinit var user: FirebaseAuth
    private lateinit var nombre : String
    private lateinit var db: FirebaseFirestore
    private lateinit var storage: FirebaseStorage
    private lateinit var jsonUsuario : JSONObject
    private lateinit var model : UsuarioServicioViewModel
    private lateinit var serviciosArray: ArrayList<String>
    private lateinit var adaptador: PerfilServiciosAdapter
    private lateinit var progressBar: ProgressBar

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentServicioBinding.inflate(inflater, container, false)
        val root: View = binding.root

        progressBar = binding.progressBar

        activity?.findViewById<ImageButton>(R.id.btnBack)?.setOnClickListener {
            activity?.finish()
        }
        activity?.findViewById<TextView>(R.id.txtToolbar)?.text = "Perfil"

        user = Firebase.auth
        db = Firebase.firestore
        storage = Firebase.storage
        model = ViewModelProvider(requireActivity()).get(UsuarioServicioViewModel::class.java)
        jsonUsuario = JSONObject()
        try {
            model.mldUsuarioServicio.observe(viewLifecycleOwner, {
                jsonUsuario = JSONObject(it)
                setup()
            })

        }  catch (e : Exception){}


        return root
    }

    private fun setup() {

        db.collection("usuario").document(user.currentUser!!.email!!)
            .get().addOnSuccessListener {
                nombre = it.data?.get("nombre").toString()
            }


        if (jsonUsuario.length() != 0) {

            binding.txtNombre.text = jsonUsuario.getString("nombre").toString()
            binding.txtCiudad.text = jsonUsuario.getString("ciudad").toString()
            binding.txtDescripcion.text = jsonUsuario.getString("bibliografia").toString()

            if (!(jsonUsuario.getString("facebook").toString() != "" ||
                        jsonUsuario.getString("twitter").toString() != "" ||
                        jsonUsuario.getString("instagram").toString() != "" ||
                        jsonUsuario.getString("youtube").toString() != "" ||
                        jsonUsuario.getString("web").toString() != ""
                        )
            )
                binding.txtRedesSociales.visibility = View.VISIBLE

            if (jsonUsuario.getString("facebook").toString() != "")
                binding.imageFacebook.visibility = View.VISIBLE

            if (jsonUsuario.getString("twitter").toString() != "")
                binding.imageTwitter.visibility = View.VISIBLE

            if (jsonUsuario.getString("instagram").toString() != "")
                binding.imageInstagram.visibility = View.VISIBLE

            if (jsonUsuario.getString("youtube").toString() != "")
                binding.imageYoutube.visibility = View.VISIBLE

            if (jsonUsuario.getString("web").toString() != "")
                binding.imageWeb.visibility = View.VISIBLE


            if (jsonUsuario.has("url")) {
                context?.let { context ->
                    Glide.with(context)
                        .load(jsonUsuario.getString("url"))
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
                                return false
                            }

                        })
                        .into(binding.imageviewPerfil)
                }
            } else {
                progressBar.visibility = View.GONE
                binding.layout.visibility = View.VISIBLE
            }

        } else {
            showAlert(
                "Error",
                "Error al cargar el usuario"
            )
        }

        //ADAPTADOR DE SERVICIOS
        try {
            serviciosArray = ArrayList()

            db.collection("usuario").document(jsonUsuario.getString("email"))
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
                    if (documents.isSuccessful)
                        if (!documents.result.isEmpty) {
                            for (document in documents.result) {
                                serviciosArray.add(document.data["nombre"] as String)
                            }

                            //Adaptador
                            adaptador = PerfilServiciosAdapter(
                                binding.root.context,
                                R.layout.adapter_perfil_servicios,
                                serviciosArray,
                                object : PerfilServiciosAdapter.OnItemClickListener {
                                    @SuppressLint("ResourceType")
                                    override fun onItemClick(servicioNombre: String) {

                                        val bundle = Bundle()
                                        bundle.putString("servicioNombre", servicioNombre)
                                        bundle.putString("usuario", jsonUsuario.toString())
                                        findNavController().navigate(
                                            R.id.action_servicioFragment_to_mostrarTrabajosFragment,
                                            bundle
                                        )
                                    }
                                })

                            binding.recyclerServicios.adapter = adaptador
                            binding.recyclerServicios.layoutManager =
                                LinearLayoutManager(requireContext())

                            binding.progressBar.visibility = View.GONE
                            binding.layout.visibility = View.VISIBLE

                        } else {
                            binding.progressBar.visibility = View.GONE
                            binding.layout.visibility = View.VISIBLE
                        }
                }
        } catch (e: Exception) {
            Toast.makeText(
                context,
                "No hay registros de servicios",
                Toast.LENGTH_SHORT

            ).show()
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
}