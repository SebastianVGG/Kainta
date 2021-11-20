package com.app.kainta.ui.home

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.app.kainta.R
import com.app.kainta.ServicioActivity
import com.app.kainta.adaptadores.GeneralAdapter
import com.app.kainta.databinding.FragmentDestacadoBinding
import org.json.JSONArray
import org.json.JSONObject


class DestacadoFragment : Fragment() {

    private var _binding: FragmentDestacadoBinding? = null
    private lateinit var jsonArray : JSONArray
    private lateinit var jsonArrayCopia : JSONArray
    private lateinit var adaptador : GeneralAdapter
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentDestacadoBinding.inflate(inflater, container, false)
        val root: View = binding.root


        setup()
        ///----JSON



        return binding.root
    }



    //Setup
    private fun setup(){


    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}