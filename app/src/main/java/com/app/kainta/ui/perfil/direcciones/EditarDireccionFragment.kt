package com.app.kainta.ui.perfil.direcciones

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
import com.app.kainta.R
import com.app.kainta.adaptadores.DireccionesAdapter
import com.app.kainta.databinding.FragmentEditarDireccionBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import org.json.JSONObject


class EditarDireccionFragment : Fragment(){
    private var _binding: FragmentEditarDireccionBinding? = null
    private val binding get() = _binding!!
    private var jsonDireccion : JSONObject = JSONObject()
    private lateinit var user : FirebaseAuth
    private lateinit var db : FirebaseFirestore
    private lateinit var dialogAlert : Dialog

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentEditarDireccionBinding.inflate(inflater, container, false)
        val root: View = binding.root

        user = Firebase.auth
        db = Firebase.firestore

        jsonDireccion = JSONObject(arguments?.getString("jsonDireccion").toString())

        activity?.findViewById<ImageButton>(R.id.btnBack)?.setOnClickListener {
            activity?.onBackPressed()
        }
        activity?.findViewById<TextView>(R.id.txtToolbar)?.text = "Editar Dirección"

        setup()

        return root
    }

    private fun setup(){


        binding.editNombre.setText(jsonDireccion.getString("nombre"))
        binding.editDireccion.setText(jsonDireccion.getString("direccion"))
        binding.editColonia.setText(jsonDireccion.getString("colonia"))
        binding.editCP.setText(jsonDireccion.getString("cp"))
        binding.editCiudad.setText(jsonDireccion.getString("ciudad"))
        binding.editTelefono.setText(jsonDireccion.getString("telefono"))

        binding.btnAceptar.setOnClickListener {


            if(binding.editNombre.text.isNotEmpty()){
                binding.editNombre.background = context?.getDrawable(R.drawable.custom_edit)
                binding.editNombre.error = null

                if(binding.editDireccion.text.isNotEmpty()){

                    binding.editDireccion.background = context?.getDrawable(R.drawable.custom_edit)
                    binding.editDireccion.error = null

                    if(binding.editColonia.text.isNotEmpty()){

                        binding.editColonia.background = context?.getDrawable(R.drawable.custom_edit)
                        binding.editColonia.error = null

                        if(binding.editCP.text.isNotEmpty()){

                            binding.editCP.background = context?.getDrawable(R.drawable.custom_edit)
                            binding.editCP.error = null

                            if(binding.editCiudad.text.isNotEmpty()){

                                binding.editCiudad.background = context?.getDrawable(R.drawable.custom_edit)
                                binding.editCiudad.error = null

                                if(binding.editTelefono.text.isNotEmpty()){

                                    binding.editTelefono.background = context?.getDrawable(R.drawable.custom_edit)
                                    binding.editTelefono.error = null


                                    val nombre = binding.editNombre.text.toString()
                                    val direccion = binding.editDireccion.text.toString()
                                    val colonia = binding.editColonia.text.toString()
                                    val cp = binding.editCP.text.toString()
                                    val ciudad = binding.editCiudad.text.toString()
                                    val telefono = binding.editTelefono.text.toString()

                                    val docRef = db.
                                    collection("usuario").document(user.currentUser?.email!!)
                                        .collection("direcciones").document(jsonDireccion.getString("id"))

                                    docRef.update(mapOf(
                                        "nombre" to nombre,
                                        "direccion" to direccion,
                                        "colonia" to colonia,
                                        "cp" to cp,
                                        "ciudad" to ciudad,
                                        "telefono" to telefono
                                    )).addOnSuccessListener {
                                        showAlert(
                                            "Correcto",
                                            "Se acutalizo correctamente la dirección"
                                        )

                                    }
                                        .addOnFailureListener { e -> showAlert("Error", (e as FirebaseAuthException).message.toString()) }



                                }else{
                                    binding.editTelefono.background = context?.getDrawable(R.drawable.custom_edit_error)
                                    binding.editTelefono.error = "El campo es obligatorio."
                                }

                            }else{
                                binding.editCiudad.background = context?.getDrawable(R.drawable.custom_edit_error)
                                binding.editCiudad.error = "El campo es obligatorio."
                            }

                        }else{
                            binding.editCP.background = context?.getDrawable(R.drawable.custom_edit_error)
                            binding.editCP.error = "El campo es obligatorio."

                        }

                    }else{
                        binding.editColonia.background = context?.getDrawable(R.drawable.custom_edit_error)
                        binding.editColonia.error = "El campo es obligatorio."
                    }

                }else{
                    binding.editDireccion.background = context?.getDrawable(R.drawable.custom_edit_error)
                    binding.editDireccion.error = "El campo es obligatorio."
                }

            }else{
                binding.editNombre.background = context?.getDrawable(R.drawable.custom_edit_error)
                binding.editNombre.error = "El campo es obligatorio."
            }



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
            activity?.onBackPressed()
        }

        if(dialogAlert.window!=null)
            dialogAlert.window?.setBackgroundDrawable(ColorDrawable(0))

        dialogAlert.show()
    }

}