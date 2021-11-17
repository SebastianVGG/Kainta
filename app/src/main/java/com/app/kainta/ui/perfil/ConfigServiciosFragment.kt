package com.app.kainta.ui.perfil

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.app.kainta.R
import com.app.kainta.databinding.FragmentConfigServiciosBinding
import com.app.kainta.databinding.FragmentFillAccountBinding


class ConfigServiciosFragment : Fragment() {
    private var _binding: FragmentConfigServiciosBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentConfigServiciosBinding.inflate(inflater, container, false)
        val root: View = binding.root

        setup()


        return root
    }

    private fun setup() {

        binding.btnAddServicio.setOnClickListener {
            findNavController().navigate(R.id.action_configServiciosFragment_to_addServicioFragment)
        }

    }


}