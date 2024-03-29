package com.app.kainta.ui.perfil


import android.annotation.SuppressLint
import android.app.Dialog
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.app.kainta.adaptadores.DireccionesAdapter
import com.app.kainta.databinding.FragmentConfigDireccionesBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import org.json.JSONArray
import org.json.JSONObject
import com.app.kainta.R


class ConfigDireccionesFragment : Fragment() {
    private var _binding: FragmentConfigDireccionesBinding? = null
    private val binding get() = _binding!!
    private lateinit var adaptador : DireccionesAdapter
    private lateinit var user : FirebaseAuth
    private lateinit var db : FirebaseFirestore
    private lateinit var dialogAlert : Dialog


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentConfigDireccionesBinding.inflate(inflater, container, false)
        val root: View = binding.root

        activity?.findViewById<ImageButton>(R.id.btnBack)?.setOnClickListener {
            activity?.onBackPressed()
        }
        activity?.findViewById<TextView>(R.id.txtToolbar)?.text = "Mis direcciones"


        user = Firebase.auth
        db = Firebase.firestore

        setup()


        return root
    }

    private fun setup(){

        cargarDirecciones()

        binding.btnAddDireccion.setOnClickListener {
            findNavController().navigate(R.id.action_configDireccionesFragment_to_nuevaDireccionFragment)
        }

    }

    private fun cargarDirecciones() {
        val jsons = JSONArray()
        var jsonData : JSONObject = JSONObject()

        //Se obtienen las direcciones del usuario
        val docRef = db.collection("usuario").document(user.currentUser?.email!!).collection("direcciones")
        docRef.get()
            .addOnCompleteListener { documents ->
                if (!documents.result.isEmpty){
                    for (document in documents.result) {
                        jsonData = JSONObject(document.data)
                        jsonData.put("id", document.id)
                        jsons.put(jsonData)
                    }

                    //Adaptador
                    adaptador = DireccionesAdapter(
                        binding.root.context,
                        R.layout.adapter_direcciones,
                        jsons, object : DireccionesAdapter.OnItemClickListener {
                            @SuppressLint("ResourceType")
                            override fun onItemClick(direccion: JSONObject?, editar : Boolean) {

                                if(editar){
                                    val bundle = Bundle()
                                    bundle.putString("jsonDireccion", direccion.toString())
                                    findNavController().navigate(R.id.action_configDireccionesFragment_to_editarDireccionFragment, bundle)
                                } else{
                                    docRef.document(direccion?.get("id").toString()).delete()
                                        .addOnSuccessListener {
                                            showAlert("Correcto", "Se eliminó la dirección")
                                        }
                                        .addOnFailureListener { e -> showAlert("Error", (e as FirebaseAuthException).message.toString()) }
                                }

                            }
                        })

                    binding.recyclerDirecciones.adapter = adaptador
                    binding.recyclerDirecciones.layoutManager = LinearLayoutManager(requireContext())

                    binding.progressBar.visibility = View.GONE
                    binding.layout.visibility = View.VISIBLE

                }else{
                    binding.txtEmptyDirecciones.visibility = View.VISIBLE
                    binding.progressBar.visibility = View.GONE
                    binding.recyclerDirecciones.visibility = View.GONE
                    binding.layout.visibility = View.VISIBLE
                }

            }
            .addOnFailureListener {
                showAlert("Error", (it as FirebaseAuthException).message.toString())
            }
    }


    private fun showAlert(titulo : String,mensaje : String){
        dialogAlert = Dialog(requireContext())

        dialogAlert.setContentView(R.layout.dialog_alert)

        dialogAlert.findViewById<TextView>(R.id.txtTitulo).text = titulo
        dialogAlert.findViewById<TextView>(R.id.txtMensaje).text = mensaje
        dialogAlert.findViewById<ImageButton>(R.id.btnClose).setOnClickListener {
            dialogAlert.dismiss()

        }
        dialogAlert.findViewById<Button>(R.id.btnAceptar).setOnClickListener {
            dialogAlert.dismiss()
        }

        dialogAlert.setOnDismissListener {
            cargarDirecciones()
        }

        if(dialogAlert.window!=null)
            dialogAlert.window?.setBackgroundDrawable(ColorDrawable(0))

        dialogAlert.show()
    }

}