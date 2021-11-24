package com.app.kainta.ui.home.servicios

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.app.kainta.R
import com.app.kainta.adaptadores.HomeAdapter
import com.app.kainta.databinding.FragmentDestacadoBinding
import com.app.kainta.databinding.FragmentMostrarServiciosBinding
import com.app.kainta.mvc.UsuarioServicioViewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import org.json.JSONArray

class MostrarServiciosFragment : Fragment() {
    private var _binding: FragmentMostrarServiciosBinding? = null
    private lateinit var jsonServicios: JSONArray
    private lateinit var jsonArrayCopia: JSONArray
    private lateinit var adaptador: HomeAdapter
    private lateinit var db: FirebaseFirestore
    private lateinit var model: UsuarioServicioViewModel
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentMostrarServiciosBinding.inflate(inflater, container, false)
        val root: View = binding.root

        db = Firebase.firestore

        model = ViewModelProvider(requireActivity()).get(UsuarioServicioViewModel::class.java)

        setup()

        return binding.root
    }

    private fun setup() {

       binding.btnServiciosSolicitados.setOnClickListener {

       }

    }
}