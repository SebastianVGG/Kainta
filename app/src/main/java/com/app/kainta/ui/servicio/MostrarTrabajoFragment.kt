package com.app.kainta.ui.servicio

import android.app.Dialog
import android.graphics.Color
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.ImageButton
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.app.kainta.R
import com.app.kainta.adaptadores.ServiciosImagesURLAdapter
import com.app.kainta.adaptadores.SliderAdapter
import com.app.kainta.databinding.FragmentMostrarTrabajoBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import com.smarteist.autoimageslider.IndicatorView.animation.type.IndicatorAnimationType
import com.smarteist.autoimageslider.SliderAnimations
import com.smarteist.autoimageslider.SliderView
import org.json.JSONObject
import java.lang.Exception

class MostrarTrabajoFragment : Fragment() {
    private var _binding: FragmentMostrarTrabajoBinding? = null
    private val binding get() = _binding!!
    private lateinit var user : FirebaseAuth
    private lateinit var db : FirebaseFirestore
    private lateinit var storage: FirebaseStorage
    private lateinit var jsonTrabajo : JSONObject
    private lateinit var jsonUsuario : JSONObject
    private var listURL : ArrayList<String> = ArrayList()
    private lateinit var dialogSlider : Dialog
    private lateinit var adaptador : ServiciosImagesURLAdapter


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentMostrarTrabajoBinding.inflate(inflater, container, false)
        val root: View = binding.root

        activity?.findViewById<ImageButton>(R.id.btnBack)?.setOnClickListener {
            activity?.onBackPressed()
        }
        activity?.findViewById<TextView>(R.id.txtToolbar)?.text = "Ver Trabajo"


        user = Firebase.auth
        db = Firebase.firestore
        storage = Firebase.storage

        jsonTrabajo = JSONObject(arguments?.getString("jsonTrabajo").toString())
        jsonUsuario = JSONObject(arguments?.getString("jsonUsuario") as String)

        setup()


        return root
    }

    private fun setup() {

        listURL = ArrayList()

        if(jsonUsuario.getString("email") == user.currentUser?.email)
            binding.btnSolicitarServicio.visibility = View.GONE

        binding.btnSolicitarServicio.setOnClickListener {
            val bundle = Bundle()
            bundle.putString("jsonUsuario", jsonUsuario.toString())
            findNavController().navigate(R.id.action_mostrarTrabajoFragment_to_solicitarServicioFragment, bundle)
        }

        try {
            binding.txtTitulo.text = jsonTrabajo.getString("titulo").toString()
            binding.txtDescripcion.text = jsonTrabajo.getString("descripcion").toString()

            for (i in 0 until jsonTrabajo.length()-3)
                listURL.add(jsonTrabajo.getString("url$i"))

            //Adaptador
            adaptador =object :  ServiciosImagesURLAdapter(
                binding.root.context,
                R.layout.adapter_servicios_images,
                listURL){
                override fun finishedGlide() {
                    binding.progressBar.visibility = View.GONE
                    binding.layoutPrincipal.alpha = 1F
                }
                override fun onItemClick(uri: String, posicion : Int) {

                    inicializarSliderAdapter(listURL, posicion)
                    dialogSlider.show()
                }
            }

            binding.recyclerImages.adapter = adaptador
            binding.recyclerImages.layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)

           /* viewModel.mldObserve.observe(viewLifecycleOwner,{
                if(it){

                    binding.progressBar.visibility = View.GONE
                    binding.layoutPrincipal.visibility = View.VISIBLE
                }
            })*/


        }catch (e:Exception){
            showSnackBar(binding.layoutPrincipal, "Error al cargar el trabajo")
        }

    }

    private fun inicializarSliderAdapter(listImages : ArrayList<String>, posicion : Int) {
        dialogSlider = Dialog(requireContext(), android.R.style.Theme_Translucent_NoTitleBar)

        dialogSlider.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialogSlider.setContentView(R.layout.dialog_slider_images)

        val window: Window? = dialogSlider.window
        val wlp: WindowManager.LayoutParams = (window?.attributes ?: null) as WindowManager.LayoutParams

        wlp.gravity = Gravity.CENTER
        wlp.flags = wlp.flags and WindowManager.LayoutParams.FLAG_BLUR_BEHIND.inv()
        window?.attributes = wlp
        dialogSlider.window?.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT)


        val sliderView : SliderView = dialogSlider.findViewById<SliderView>(R.id.imageSlider)
        val sliderAdapter = SliderAdapter(listImages, true)

        sliderView.setSliderAdapter(sliderAdapter)
        sliderView.setIndicatorAnimation(IndicatorAnimationType.WORM)
        sliderView.setSliderTransformAnimation(SliderAnimations.DEPTHTRANSFORMATION)
        sliderView.currentPagePosition = posicion


    }

    private fun showSnackBar(view: ConstraintLayout, text: String) {

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