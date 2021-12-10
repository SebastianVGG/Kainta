package com.app.kainta.ui.login

import android.app.Dialog
import android.content.Context
import android.content.Intent
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
import com.app.kainta.HomeActivity
import com.app.kainta.ProviderType
import com.app.kainta.R
import com.app.kainta.databinding.FragmentFillAccountBinding
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase


class FillAccountFragment : Fragment() {

    private var _binding: FragmentFillAccountBinding? = null
    private val binding get() = _binding!!
    private lateinit var email : String
    private lateinit var provider : String
    private lateinit var dialogAlert : Dialog


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentFillAccountBinding.inflate(inflater, container, false)
        val root: View = binding.root

        email = arguments?.getString("email").toString()
        provider = arguments?.getString("provider").toString()

        setup()


        return root
    }

    private fun setup(){

        //Boton crear cuenta
        binding.btnCrearCuenta.setOnClickListener {

            val nombre = binding.editNombre.text  .toString()
            val telefono = binding.editTelefono.text.toString()
            val bibliografia = binding.editBibliografia.text.toString()
            val ciudad = binding.editCiudad.text.toString()

            if(
                nombre.isNotEmpty()
            ){

            //Colocando datos en la base de datos
            newUser(nombre,telefono,bibliografia,ciudad,email)

            }else showAlert("Error","Se ha producido un error autenticando al usuario")
        }

    }

    //Creando nuevo usuario
    private fun newUser(nombre : String, telefono : String, bibliografia : String, ciudad : String, email : String){

        val db = Firebase.firestore
        val prefs = requireActivity().getSharedPreferences(getString(R.string.user_token), Context.MODE_PRIVATE)
        val token = prefs.getString("token", null).toString()

        val user = hashMapOf(
            "nombre" to nombre,
            "telefono" to telefono,
            "bibliografia" to bibliografia,
            "ciudad" to ciudad,
            "facebook" to "",
            "twitter" to "",
            "instagram" to "",
            "youtube" to "",
            "web" to "",
            "email" to email,
            "provider" to provider,
            "token" to token
        )

        db.collection("usuario")
            .document(email)
            .set(user)
            .addOnSuccessListener { documentReference ->
                showHome( email , ProviderType.BASIC)
            }
            .addOnFailureListener { e ->
                showAlert("Error","Se ha producido un error autenticando al usuario")
            }
    }

    //Iniciar Home
    private fun showHome(email : String, provider : ProviderType){

        activity?.let{
            val homeIntent = Intent(it, HomeActivity::class.java).apply {
                putExtra("email", email)
                putExtra("provider", provider.name)
            }
            it.startActivity(homeIntent)
            it.finish()
        }
    }

    //Alerta
    private fun showAlert(titulo: String, mensaje: String) {

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
        if(dialogAlert.window!=null)
            dialogAlert.window?.setBackgroundDrawable(ColorDrawable(0))

    }

}