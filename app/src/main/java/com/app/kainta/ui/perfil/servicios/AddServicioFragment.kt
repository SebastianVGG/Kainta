package com.app.kainta.ui.perfil.servicios

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.navigation.fragment.findNavController
import com.app.kainta.R
import com.app.kainta.adaptadores.PerfilServiciosAdapter
import com.app.kainta.databinding.FragmentAddServicioBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase


class AddServicioFragment : Fragment(), AdapterView.OnItemClickListener {
    private var _binding: FragmentAddServicioBinding? = null
    private val binding get() = _binding!!
    private lateinit var adaptador : PerfilServiciosAdapter
    private lateinit var user : FirebaseAuth
    private var fromPerfil : Boolean = false
    private lateinit var db : FirebaseFirestore
    private lateinit var servicio : String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentAddServicioBinding.inflate(inflater, container, false)
        val root: View = binding.root

        user = Firebase.auth
        db = Firebase.firestore

        activity?.findViewById<ImageButton>(R.id.btnBack)?.setOnClickListener {
            activity?.onBackPressed()
        }
        activity?.findViewById<TextView>(R.id.txtToolbar)?.text = "Agregar Servicio"

        try {
            if(arguments?.containsKey("fromPerfil") as Boolean)
                fromPerfil = true
        }catch (e : Exception){e.printStackTrace()}

        setup()


        return root
    }

    @SuppressLint("ResourceType")
    private fun setup() {

        val serviciosString = requireActivity().resources.getStringArray(R.array.spinner_servicios)
        serviciosString.sort()
        val adapter = ArrayAdapter(requireContext(), R.layout.spinner_list_item, serviciosString)

        with(binding.menuServicios.editText as? AutoCompleteTextView){
            this?.setAdapter(adapter)
            this?.onItemClickListener = this@AddServicioFragment
        }

        binding.btnAddTrabajo.setOnClickListener {
            val bundle = Bundle()
            bundle.putString("servicio",servicio.lowercase())
            if(fromPerfil)
                bundle.putBoolean("fromPerfil1", fromPerfil)
            else
                bundle.putBoolean("fromAddServicio", true)
            findNavController().navigate(R.id.action_addServicioFragment_to_addTrabajoFragment, bundle)
        }

    }

    override fun onItemClick(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        servicio = parent?.getItemAtPosition(position).toString()
        binding.btnAddTrabajo.visibility = View.VISIBLE

    }


}