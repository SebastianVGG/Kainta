package com.app.kainta.ui.perfil

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.navigation.fragment.findNavController
import com.app.kainta.LoginActivity
import com.app.kainta.R
import com.app.kainta.databinding.FragmentFillAccountBinding
import com.app.kainta.databinding.FragmentPerfilBinding
import com.google.firebase.auth.FirebaseAuth


class PerfilFragment : Fragment() {

    private var _binding: FragmentPerfilBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentPerfilBinding.inflate(inflater, container, false)
        val root: View = binding.root

        activity?.findViewById<ImageButton>(R.id.btnBack)?.setOnClickListener {
            activity?.finish()
        }
        activity?.findViewById<TextView>(R.id.txtToolbar)?.text = "Configuración de Perfil"

        setup()


        return root
    }

    private fun setup(){

        //CARD VIEWS CLICKS

        binding.cardLogin.setOnClickListener {
            findNavController().navigate(R.id.action_perfilFragment_to_configLoginFragment)
        }

        binding.cardDirecciones.setOnClickListener {
            findNavController().navigate(R.id.action_perfilFragment_to_configDireccionesFragment)
        }

        binding.cardPerfil.setOnClickListener {
            findNavController().navigate(R.id.action_perfilFragment_to_configPerfilFragment)
        }

        binding.cardServicios.setOnClickListener {
            findNavController().navigate(R.id.action_perfilFragment_to_configServiciosFragment)
        }

        //Cerrar sesion
        binding.btnCerrarSesion.setOnClickListener {

            //Borrar datos

            val prefs = requireActivity().getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE).edit()
            prefs.clear()
            prefs.apply()

            FirebaseAuth.getInstance().signOut()

            activity.let {
                Intent(context, LoginActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }.also { startActivity(it) }
                it?.finish()
            }
        }

    }



}