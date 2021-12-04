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
    private lateinit var adaptador: GeneralAdapter
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

        binding.progressBar.visibility = View.VISIBLE

        setup()

        return binding.root


    }

    private fun setup() {


        binding.swiperRefresh.setOnRefreshListener {
            cargarInformacion()
            binding.swiperRefresh.isRefreshing = false
        }

        cargarInformacion()


    }

    private fun cargarInformacion() {

        jsonServicios = JSONArray()
        listCorreos = ArrayList()
        val lServicios = ArrayList<String>()
        var jsonUsuario = JSONObject()
        val jsonUsuarios = JSONArray()

        db.collection("servicioN").orderBy("fecha", Query.Direction.DESCENDING).limit(5)
            .get().addOnCompleteListener {
                if (it.isSuccessful) {
                    for (document in it.result.documents){
                        listCorreos.add(document.data?.get("correo") as String)
                        lServicios.add(document.data?.get("servicio") as String)
                    }
                    for (i in 0 until listCorreos.size) {

                        db.collection("usuario").document(listCorreos[i])
                            .get()
                            .addOnFailureListener { e ->
                                Toast.makeText(
                                    context,
                                    (e as FirebaseAuthException).message.toString(),
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                            .addOnCompleteListener { usuario ->
                                if (usuario.isSuccessful) {
                                    jsonUsuario = JSONObject(usuario.result.data)
                                    jsonUsuario.put("servicio", lServicios[i])
                                    jsonUsuarios.put(jsonUsuario)

                                    if (jsonUsuarios.length() == listCorreos.size) {
                                        //Adaptador
                                        adaptador = GeneralAdapter(binding.root.context,
                                            R.layout.adapter_general,
                                            jsonUsuarios,
                                            object : GeneralAdapter.OnItemClickListener {
                                                override fun onItemClick(usuario: JSONObject?) {
                                                    //Abrir activity Servicio
                                                    activity?.let { frActivity ->
                                                        val servicioIntent = Intent(
                                                            frActivity,
                                                            ServicioActivity::class.java
                                                        ).apply {
                                                            putExtra("usuario", usuario.toString())
                                                        }
                                                        frActivity.startActivity(servicioIntent)
                                                    }

                                                }
                                            })

                                        binding.recyclerView.adapter = adaptador
                                        binding.recyclerView.layoutManager =
                                            LinearLayoutManager(requireContext())
                                        binding.progressBar.visibility = View.GONE
                                        binding.swiperRefresh.visibility= View.VISIBLE
                                    }
                                }else{
                                    binding.progressBar.visibility = View.GONE
                                    Toast.makeText(context, "Error al cargar nuevos", Toast.LENGTH_SHORT)
                                        .show()
                                }
                            }
                    }
                }else{
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(context, "Error al cargar nuevos", Toast.LENGTH_SHORT)
                        .show()
                }
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




