package com.app.kainta.ui.login

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.app.kainta.HomeActivity
import com.app.kainta.ProviderType
import com.app.kainta.R
import com.app.kainta.databinding.FragmentCreateBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.util.regex.Pattern


class CreateFragment : Fragment() {

    private var _binding: FragmentCreateBinding? = null
    private val binding get() = _binding!!
    private val VALID_PASSWORD_REGEX =
        Pattern.compile("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=\\S+\$).{6,}\$")
    val VALID_EMAIL_ADDRESS_REGEX =
        Pattern.compile(
            "^(?=.{1,64}@)[\\p{L}0-9_-]+(\\.[\\p{L}0-9_-]+)*@[^-][\\p{L}0-9-]+(\\.[\\p{L}0-9-]+)*(\\.[\\p{L}]{2,})$"
        )
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentCreateBinding.inflate(inflater, container, false)
        val root: View = binding.root

        setup()


        return root
    }

    private fun setup(){

        binding.btnBack.setOnClickListener{
            activity?.onBackPressed()
        }

        val listEditText = ArrayList<EditText>()
        listEditText.add(binding.editNombre)
        listEditText.add(binding.editBibliografia)
        listEditText.add(binding.editCiudad)
        listEditText.add(binding.editEmail)
        listEditText.add(binding.editPass)
        listEditText.add(binding.editPass2)
        listEditText.add(binding.editTelefono)

        //Boton crear cuenta
        binding.btnCrearCuenta.setOnClickListener {

            val nombre = binding.editNombre.text.toString()
            val telefono = binding.editTelefono.text.toString()
            val bibliografia = binding.editBibliografia.text.toString()
            val ciudad = binding.editCiudad.text.toString()
            val email = binding.editEmail.text.toString()
            val pass = binding.editPass.text.toString()
            val pass2 = binding.editPass2.text.toString()




                if(
                    nombre.isNotEmpty() &&
                    telefono.isNotEmpty() &&
                    bibliografia.isNotEmpty() &&
                    ciudad.isNotEmpty() &&
                    email.isNotEmpty() &&
                    pass.isNotEmpty() &&
                    pass2.isNotEmpty()
                ){
                    if(Pattern.compile("-?\\d+(\\.\\d+)?").matcher(telefono).matches()) {
                        if(validateEmail(email)){
                            if(validatePass(pass, pass2)){
                                FirebaseAuth.getInstance().createUserWithEmailAndPassword(
                                    email,
                                    pass.toString()
                                ).addOnCompleteListener {
                                    if(it.isSuccessful){
                                        //Colocando datos en la base de datos
                                        newUser(nombre,telefono,bibliografia,ciudad,email)
                                    }
                                    else{
                                        when ((it.exception as FirebaseAuthException).errorCode) {
                                            "ERROR_EMAIL_ALREADY_IN_USE" -> {
                                                binding.editEmail.error = "Correo electrónico en uso."
                                                showSnackBar(binding.layoutPrincipal, "Correo electrónico en uso.")
                                            }
                                            else -> showAlert("MENSAJE DE ERROR: " +    (it.exception as FirebaseAuthException).message.toString())
                                        }

                                    }
                                }
                            }
                        }
                    }else{
                    binding.editTelefono.background = context?.getDrawable(R.drawable.custom_edit_error)
                    binding.editTelefono.error = "El teléfono debe de contener sólo números."
                }
                }else{
                    for (edit in listEditText){
                        if(edit.text.toString().isEmpty()){
                            edit.background = context?.getDrawable(R.drawable.custom_edit_error)
                            edit.error = "Debe de llenar este campo."
                        }else{
                            edit.background = context?.getDrawable(R.drawable.custom_edit)
                            edit.error = null
                        }

                    }
                    showSnackBar(binding.layoutPrincipal, "Todos los campos son obligatorios para continuar.")
                }


        }
    }

    private fun validateEmail(email: String): Boolean {
        if (VALID_EMAIL_ADDRESS_REGEX.matcher(email).matches()) {
            binding.editEmail.error = null
            binding.editEmail.background = context?.getDrawable(R.drawable.custom_edit)
            return true
        }else{
            binding.editEmail.background = context?.getDrawable(R.drawable.custom_edit_error)
            binding.editEmail.error = "Coloque un correo electrónico correcto."
            return false
        }
    }

    private fun validatePass(pass: String, pass2: String): Boolean {

            if (VALID_PASSWORD_REGEX.matcher(pass).matches()) {
                binding.editPass.error = null
                if(VALID_PASSWORD_REGEX.matcher(pass2).matches()){
                    binding.editPass2.error = null
                    if(pass == pass2)
                        return true
                    else {
                        binding.editPass.background = context?.getDrawable(R.drawable.custom_edit_error)
                        binding.editPass.error = "La contraseña no es la misma."
                        binding.editPass2.background = context?.getDrawable(R.drawable.custom_edit_error)
                        binding.editPass2.error = "La contraseña no es la misma."
                        return false
                    }
                } else {
                    binding.editPass2.error = "La contraseña debe tener al menos una letra mayúscula, una letra minúscula, un número y mínimo 6 carácteres."
                    return false
                }
            }else{
                binding.editPass.error = "La contraseña debe tener al menos una letra mayúscula, una letra minúscula, un número y mínimo 6 carácteres."
                return false
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
            "provider" to "BASIC",
            "token" to token
        )

        db.collection("usuario")
            .document(email)
            .set(user)
            .addOnSuccessListener { documentReference ->
                showHome( email , ProviderType.BASIC)
            }
            .addOnFailureListener { e ->
                showAlert( e.toString())
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
    private fun showAlert(mensaje : String){
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Error")
        builder.setMessage(mensaje)
        builder.setPositiveButton("Aceptar",null)
        val dialog : AlertDialog = builder.create()
        dialog.show()
    }

    private fun showSnackBar(view: LinearLayout, text: String) {

        val snackbar = Snackbar.make(view, text, Snackbar.LENGTH_INDEFINITE)
        val snackbarLayout : Snackbar.SnackbarLayout = snackbar.view as Snackbar.SnackbarLayout
        val customView = layoutInflater.inflate(R.layout.custom_snackbar, null)

        customView.findViewById<TextView>(R.id.btnOK).setOnClickListener {
            snackbar.dismiss()
        }
        customView.findViewById<TextView>(R.id.txtSnackBar).text = text

        snackbarLayout.setPadding(0,0,0,0)
        snackbarLayout.addView(customView, 0)
        snackbar.show()

        snackbar.view.setBackgroundColor(Color.TRANSPARENT)


    }

}