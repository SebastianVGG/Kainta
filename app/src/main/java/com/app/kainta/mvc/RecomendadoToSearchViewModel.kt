package com.app.kainta.mvc

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class RecomendadoToSearchViewModel  : ViewModel() {

    val mldServicioSeleccionado = MutableLiveData<String>()

    fun setData(item: String?) {
        mldServicioSeleccionado.postValue(item)
    }

    fun getData() : MutableLiveData<String> {
        return mldServicioSeleccionado
    }

}