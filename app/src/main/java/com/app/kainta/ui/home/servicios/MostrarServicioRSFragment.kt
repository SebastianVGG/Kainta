package com.app.kainta.ui.home.servicios

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import com.app.kainta.R
import com.app.kainta.adaptadores.DireccionesAdapter
import com.app.kainta.databinding.FragmentEditarDireccionBinding
import com.app.kainta.databinding.FragmentMostrarServicioRSBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import org.json.JSONObject

class MostrarServicioRSFragment : Fragment() {
    private var _binding: FragmentMostrarServicioRSBinding? = null
    private val binding get() = _binding!!
    private var jsonServicio : JSONObject = JSONObject()
    private lateinit var user : FirebaseAuth
    private lateinit var db : FirebaseFirestore

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentMostrarServicioRSBinding.inflate(inflater, container, false)
        val root: View = binding.root

        user = Firebase.auth
        db = Firebase.firestore

        jsonServicio = JSONObject(arguments?.getString("jsonServicio").toString())

        setup()

        return root
    }

    private fun setup(){

        if(jsonServicio.has("requeridos"))
            binding.btnCancelar.text = "Rechazar"
        else
            binding.btnAceptar.visibility = View.GONE

        llenarInformacion()

        binding.btnAceptar.setOnClickListener {

        }

        binding.btnCancelar.setOnClickListener {

        }

    }

    private fun llenarInformacion() {

        binding.txtDireccionNombre.text = jsonServicio.getString("nombre")
        binding.txtDireccionDireccion.text = jsonServicio.getString("direccion")
        binding.txtDireccionColonia.text = jsonServicio.getString("colonia")
        binding.txtDireccionCP.text = jsonServicio.getString("cp")
        binding.txtDireccionCiudad.text = jsonServicio.getString("ciudad")
        binding.txtDireccionTelefono.text = jsonServicio.getString("telefono")

    }

    private fun showAlert(titulo : String,mensaje : String){
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle(titulo)
        builder.setMessage(mensaje)
        builder.setPositiveButton("Aceptar",null)
        val dialog : AlertDialog = builder.create()
        dialog.show()
    }

}