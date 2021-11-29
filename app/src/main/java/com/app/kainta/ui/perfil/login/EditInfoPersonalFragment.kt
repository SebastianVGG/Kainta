package com.app.kainta.ui.perfil.login

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.appcompat.app.AlertDialog
import com.app.kainta.databinding.FragmentEditInfoPersonalBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class EditInfoPersonalFragment : Fragment() {
    private var _binding: FragmentEditInfoPersonalBinding? = null
    private val binding get() = _binding!!
    private lateinit var user : FirebaseAuth
    private lateinit var db : FirebaseFirestore
    private lateinit var progressBar: ProgressBar

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentEditInfoPersonalBinding.inflate(inflater, container, false)
        val root: View = binding.root

        progressBar = binding.progressBar

        user = Firebase.auth
        db = Firebase.firestore


        setup()


        return root
    }

    private fun setup() {

        val email: String = user.currentUser?.email.toString()

        binding.btnAceptar.setOnClickListener {

            val telefono = binding.editTelefono.text.toString()
            val nombre = binding.editNombre.text.toString()
            val bibliografia = binding.editBibliografia.text.toString()
            val ciudad = binding.editCiudad.text.toString()
            val facebook = binding.editFacebook.text.toString()
            val twitter = binding.editTwitter.text.toString()
            val instagram = binding.editInstagram.text.toString()
            val youtube = binding.editYoutube.text.toString()
            val web = binding.editPaginaWeb.text.toString()

            val data = hashMapOf(
                "nombre" to nombre,
                "telefono" to telefono,
                "bibliografia" to bibliografia,
                "ciudad" to ciudad,
                "facebook" to facebook,
                "twitter" to twitter,
                "instagram" to instagram,
                "youtube" to youtube,
                "web" to web
            )

            db.collection("usuario").document(email)
                .update(data as Map<String, Any>)
                .addOnCompleteListener {

                    if (it.isSuccessful) showAlert(
                        "Correcto",
                        "Se actualizo correctamente"
                    )
                    else showAlert(
                        "Error",
                        (it.exception as FirebaseAuthException).message.toString()
                    )

                }
        }

        llenarCampos()

    }

    private fun llenarCampos() {

        val email: String = user.currentUser?.email.toString()

        db.collection("usuario").document(email)
            .get()
            .addOnCompleteListener {
                if(it.isSuccessful){
                    binding.editNombre.setText(it.result.data?.get("nombre").toString())
                    binding.editBibliografia.setText(it.result.data?.get("bibliografia").toString())
                    binding.editTelefono.setText(it.result.data?.get("telefono").toString())
                    binding.editCiudad.setText(it.result.data?.get("ciudad").toString())
                    binding.editFacebook.setText(it.result.data?.get("facebook").toString())
                    binding.editTwitter.setText(it.result.data?.get("twitter").toString())
                    binding.editInstagram.setText(it.result.data?.get("instagram").toString())
                    binding.editYoutube.setText(it.result.data?.get("youtube").toString())
                    binding.editPaginaWeb.setText(it.result.data?.get("web").toString())

                    progressBar.visibility = View.GONE
                    binding.layout.visibility = View.VISIBLE

                }else{
                    showAlert(
                        "Error",
                        (it.exception as FirebaseAuthException).message.toString())
                    progressBar.visibility = View.GONE
                    binding.layout.visibility = View.VISIBLE
                }
            }

    }

    private fun showAlert(titulo: String, mensaje: String) {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle(titulo)
        builder.setMessage(mensaje)
        builder.setPositiveButton("Aceptar") { _,_ ->
            activity?.onBackPressed()
        }
        val dialog: AlertDialog = builder.create()
        dialog.show()
    }


}