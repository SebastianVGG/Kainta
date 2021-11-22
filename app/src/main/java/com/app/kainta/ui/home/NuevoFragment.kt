package com.app.kainta.ui.home

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import android.view.inputmethod.EditorInfo
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.app.kainta.R
import com.app.kainta.ServicioActivity
import com.app.kainta.adaptadores.GeneralAdapter
import com.app.kainta.adaptadores.HomeAdapter
import com.app.kainta.adaptadores.PerfilServiciosAdapter
import com.app.kainta.databinding.FragmentNuevoBinding
import com.app.kainta.databinding.FragmentSearchBinding
import com.app.kainta.mvc.UsuarioServicioViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import org.json.JSONArray
import org.json.JSONObject

class NuevoFragment : Fragment() {
    private var _binding: FragmentNuevoBinding? = null
    private lateinit var jsonServicios: JSONArray
    private lateinit var listCorreos: ArrayList<String>
    private lateinit var adaptador: HomeAdapter
    private lateinit var user: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var model : UsuarioServicioViewModel
    private lateinit var toggle : ActionBarDrawerToggle
    private lateinit var drawer_Layout : DrawerLayout
    private val binding get() = _binding!!

    @SuppressLint("ResourceType")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentNuevoBinding.inflate(inflater, container, false)
        val root: View = binding.root

        user = Firebase.auth
        db = Firebase.firestore

        model = ViewModelProvider(requireActivity()).get(UsuarioServicioViewModel::class.java)

        setup()

        return binding.root


    }

    private fun setup() {

        jsonServicios = JSONArray()
        var jsonServicio = JSONObject()
        val listServicios = ArrayList<String>()


        db.collection("servicios").orderBy("buscado" , Query.Direction.DESCENDING).limit(5)
            .get().addOnCompleteListener {
                if(it.isSuccessful){
                    for(servicio in it.result.documents)
                        listServicios.add(servicio.data?.get("nombre") as String)
                    //Adaptador
                    adaptador = HomeAdapter(binding.root.context,
                        R.layout.adapter_general,
                        listServicios,
                        object : HomeAdapter.OnItemClickListener {
                            override fun onItemClick(item: String) {
                                //Abrir activity Servicio
                                activity?.let { act ->
                                    val servicioIntent = Intent(
                                        act,
                                        ServicioActivity::class.java
                                    ).apply {
                                        putExtra("usuario", item.toString())
                                    }
                                    act.startActivity(servicioIntent)
                                }
                            }
                        })

                    binding.recyclerView.adapter = adaptador
                    binding.recyclerView.layoutManager =
                        LinearLayoutManager(requireContext())

                }else{
                    Toast.makeText(context, "Error al cargar destacados", Toast.LENGTH_SHORT).show()
                }
            }

      /*  val arrayServicios = resources.getStringArray(R.array.spinner_servicios)
        arrayServicios.sort()
        try {
            for(servicio in arrayServicios)
                db.collection("servicios").document(servicio.lowercase())
                    .set(mapOf(
                        "nombre" to servicio.lowercase(),
                        "buscado" to 0
                    ))
                    .addOnFailureListener { e ->
                        Toast.makeText(
                            context,
                            (e as FirebaseAuthException).message.toString(),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
        } catch (e: Exception) {
            Toast.makeText(
                context,
                "No hay registros de servicios",
                Toast.LENGTH_SHORT

            ).show()
        }*/


    }

}




