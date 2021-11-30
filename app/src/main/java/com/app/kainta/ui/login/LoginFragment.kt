package com.app.kainta.ui.login

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.navigation.fragment.findNavController
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.text.InputType
import android.widget.Button
import android.widget.CompoundButton
import android.widget.LinearLayout
import android.widget.TextView
import com.app.kainta.HomeActivity
import com.app.kainta.ProviderType
import com.app.kainta.R
import com.app.kainta.databinding.FragmentLoginBinding
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import java.lang.Exception
import com.google.android.material.snackbar.Snackbar

class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!
    private val GOOGLE_SING_IN = 100
    private lateinit var dialog : Dialog

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        val root: View = binding.root

        setup()

        return binding.root
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private fun setup(){

        inicializarLoading()

        binding.switchPassword.setOnCheckedChangeListener { compoundButton: CompoundButton, b: Boolean ->
            if (b){
                binding.editContrasena.inputType = InputType.TYPE_CLASS_TEXT
                binding.editContrasena.setSelection(binding.editContrasena.text.length)
            }
            else{
                binding.editContrasena.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                binding.editContrasena.setSelection(binding.editContrasena.text.length)
            }
        }


        binding.btnCrearCuenta.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_createFragment)
        }

        //Iniciar sesion BASIC

        binding.btnIniciarSesion.setOnClickListener {

            dialog.show()

            val prefs = requireActivity().getSharedPreferences(getString(R.string.user_token), Context.MODE_PRIVATE)
            val tokenc = prefs.getString("token", null).toString()

            if(binding.editUsuario.text.isNotEmpty()){
                if(binding.editContrasena.text.isNotEmpty()){
                    FirebaseAuth.getInstance().signInWithEmailAndPassword(
                        binding.editUsuario.text.toString(),
                        binding.editContrasena.text.toString()
                    ).addOnCompleteListener {
                        if(it.isSuccessful){
                            val db = Firebase.firestore
                            val docRef = db.collection("usuario").document(it.result?.user?.email ?: "")
                            docRef.get()
                                .addOnSuccessListener { document ->
                                    if (document.exists()) {
                                        if(document.contains("nombre") && document.contains("email")){

                                            val data = hashMapOf("provider" to "BASIC", "token" to tokenc)
                                            val docProvider = db.collection("usuario").document(it.result.user?.email ?: "")
                                            docProvider.set(data, SetOptions.merge())
                                                .addOnCompleteListener { document ->
                                                    if (document.isSuccessful)
                                                        showHome(it.result.user?.email ?: "", ProviderType.BASIC)
                                                    else{
                                                        dialog.dismiss()
                                                        showAlert()
                                                    }
                                                }
                                                .addOnFailureListener {
                                                    dialog.dismiss()
                                                    showAlert()
                                                }
                                        }else{
                                            val bundle = Bundle()
                                            bundle.putString("email", it.result?.user?.email ?: "")
                                            bundle.putString("provider", "BASIC")
                                            findNavController().navigate(R.id.action_loginFragment_to_fillAccountFragment, bundle)
                                        }
                                    } else {
                                        val bundle = Bundle()
                                        bundle.putString("email", it.result?.user?.email ?: "")
                                        bundle.putString("provider", "BASIC")
                                        findNavController().navigate(R.id.action_loginFragment_to_fillAccountFragment, bundle)
                                    }
                                }
                                .addOnFailureListener { exception ->
                                    dialog.dismiss()
                                    showAlert()
                                }
                        }else{
                            try {
                                throw it.exception!!
                            } catch (e: FirebaseAuthInvalidCredentialsException) {
                                binding.editContrasena.background = context?.getDrawable(R.drawable.custom_edit_error)
                                binding.editUsuario.background = context?.getDrawable(R.drawable.custom_edit_error)
                                binding.editUsuario.requestFocus()
                                binding.editContrasena.error = "Correo u contraseña incorrecta."
                                binding.editUsuario.error = "Correo u contraseña incorrecta."
                                dialog.dismiss()
                                showSnackBar(binding.layoutPrincipal, "Correo u contraseña incorrecta.")
                            } catch (e: FirebaseAuthUserCollisionException) {
                                dialog.dismiss()
                                showSnackBar(binding.layoutPrincipal, "Correo u contraseña incorrecta.")
                            } catch (e: Exception) {
                                dialog.dismiss()
                                showSnackBar(binding.layoutPrincipal, "Correo u contraseña incorrecta.")
                            }
                        }
                    }
                }else{
                    dialog.dismiss()
                    binding.editContrasena.background = context?.getDrawable(R.drawable.custom_edit_error)
                    binding.editContrasena.error = "Debe de llenar este campo."
                }
            }else{
                dialog.dismiss()
                binding.editUsuario.background = context?.getDrawable(R.drawable.custom_edit_error)
                binding.editUsuario.error = "Debe de llenar este campo."
            }

        }

        //Iniciar sesion GOOGLE

        binding.btnIniciarSesionGoogle.setOnClickListener {

            val googleConfig = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id_))
                .requestEmail()
                .build()

            val googleClient = GoogleSignIn.getClient(requireActivity(), googleConfig)
            googleClient.signOut()

            startActivityForResult(googleClient.signInIntent, GOOGLE_SING_IN)
        }
    }

    private fun inicializarLoading() {
        dialog = Dialog(requireContext())
        dialog.setContentView(R.layout.dialog_loading)
        dialog.setCancelable(false)
        if(dialog.window!=null)
            dialog.window?.setBackgroundDrawable(ColorDrawable(0))
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

    private fun showAlert(){
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Error")
        builder.setMessage("Error de autenticación.")
        builder.setPositiveButton("Aceptar",null)
        val dialog : AlertDialog = builder.create()
        dialog.show()
    }

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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        dialog.show()

        if(requestCode == GOOGLE_SING_IN){

            val task = GoogleSignIn.getSignedInAccountFromIntent(data)

            try {
                val account = task.getResult(ApiException::class.java)

                if(account != null){

                    //Se recuperan credenciales de Google

                    val credential = GoogleAuthProvider.getCredential(account.idToken, null)

                    //Se inicia sesion en firebase con la credencial de Google

                    FirebaseAuth.getInstance().signInWithCredential(credential).addOnCompleteListener {

                        if(it.isSuccessful){
                            val prefs = requireActivity().getSharedPreferences(getString(R.string.user_token), Context.MODE_PRIVATE)
                            val tokenc = prefs.getString("token", null).toString()
                            val db = Firebase.firestore
                            val docRef = db.collection("usuario").document(account.email)
                                docRef.get()
                                .addOnSuccessListener { document ->
                                    if (document.exists()) {
                                        if(document.contains("nombre") && document.contains("email")){

                                            val data = hashMapOf("provider" to "GOOGLE", "token" to tokenc)
                                            val docProvider = db.collection("usuario").document(account.email)
                                            docProvider.set(data, SetOptions.merge())
                                                .addOnCompleteListener { document ->

                                                    if (document.isSuccessful){
                                                        dialog.dismiss()
                                                        showHome(account.email ?: "", ProviderType.GOOGLE)
                                                    }
                                                    else{
                                                        dialog.dismiss()
                                                        showAlert()
                                                    }

                                                    }
                                        } else{
                                            val bundle = Bundle()
                                            bundle.putString("email", account.email)
                                            bundle.putString("provider", "GOOGLE")
                                            dialog.dismiss()
                                            findNavController().navigate(R.id.action_loginFragment_to_fillAccountFragment, bundle)
                                        }
                                    } else {
                                        //Se coloca este fragmento para llenar informacion faltante
                                        val bundle = Bundle()
                                        bundle.putString("email", account.email)
                                        bundle.putString("provider", "GOOGLE")
                                        dialog.dismiss()
                                        findNavController().navigate(R.id.action_loginFragment_to_fillAccountFragment, bundle)
                                    }
                                }
                                .addOnFailureListener { exception ->
                                    dialog.dismiss()
                                    showAlert()
                                }

                        }else{
                            dialog.dismiss()
                            showAlert()
                        }
                    }

                }
            }catch (e: ApiException){
                dialog.dismiss()
                showAlert()
            }

        }
    }

}


