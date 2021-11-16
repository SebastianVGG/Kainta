package com.app.kainta.ui.perfil

import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.appcompat.app.AlertDialog
import androidx.navigation.fragment.findNavController
import com.app.kainta.R
import com.app.kainta.databinding.FragmentConfigLoginBinding
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase


class ConfigLoginFragment : Fragment() {
    private var _binding: FragmentConfigLoginBinding? = null
    private val binding get() = _binding!!
    private lateinit var progressBar : ProgressBar

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentConfigLoginBinding.inflate(inflater, container, false)
        val root: View = binding.root

        progressBar = binding.progressBar

        setup()

        return root
    }

    private fun setup(){

        setInfo()

        binding.btnNombre.setOnClickListener {
            findNavController().navigate(R.id.action_configLoginFragment_to_editInfoPersonalFragment)
        }

        binding.btnCorreo.setOnClickListener {
            val bundle = Bundle()
            bundle.putString("motivo", "correo")
            findNavController().navigate(R.id.action_configLoginFragment_to_configLoginInfoFragment, bundle)
        }

        binding.btnPass.setOnClickListener {
            val bundle = Bundle()
            bundle.putString("motivo", "pass")
            findNavController().navigate(R.id.action_configLoginFragment_to_configLoginInfoFragment, bundle)
        }

    }



    //Obtener información de la base de datos
    private fun setInfo(){
        val user = Firebase.auth.currentUser
        val db = Firebase.firestore
        val email : String = user?.email.toString()

        val docRef = db.collection("usuario").document(email)
        docRef.get()
            .addOnSuccessListener { document ->

                if (document.exists()) {
                    val provider = document.get("provider") as CharSequence?
                    if( provider == "GOOGLE"){
                        binding.btnCorreo.isEnabled = false
                        binding.btnCorreo.setBackgroundColor(Color.parseColor("#808080"))
                        binding.btnPass.isEnabled = false
                        binding.btnPass.setBackgroundColor(Color.parseColor("#808080"))
                    }else{
                        binding.btnCorreo.isEnabled = true
                        binding.btnCorreo.setBackgroundColor(Color.parseColor("#febf10"))
                        binding.btnPass.isEnabled = true
                        binding.btnPass.setBackgroundColor(Color.parseColor("#febf10"))
                    }

                    binding.txtNombre.text = document.get("nombre") as CharSequence?
                    binding.txtCiudad.text = document.getString("ciudad")
                    if(document.contains("telefono")){
                        val telefono : Long? = document.getLong("telefono")
                        binding.txtTelefono.text = telefono.toString()
                    }else{
                        binding.txtTelefono.text = "Teléfono sin registrar"
                    }
                    binding.txtCorreo.text =  email
                    binding.txtPass.text = "***********"


                    progressBar.visibility = View.GONE
                    binding.layout.visibility = View.VISIBLE

                } else {
                    showAlert(
                        "No existe el documento")
                }
            }
            .addOnFailureListener { exception ->
                showAlert(
                    (exception as FirebaseAuthException).message.toString())
            }
    }

    private fun showAlert(mensaje : String){
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Error")
        builder.setMessage(mensaje)
        builder.setPositiveButton("Aceptar",null)
        val dialog : AlertDialog = builder.create()
        dialog.show()
    }



}