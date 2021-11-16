package com.app.kainta.adaptadores

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.app.kainta.ui.home.DestacadoFragment
import com.app.kainta.ui.home.NuevoFragment
import com.app.kainta.ui.home.RecomendadoFragment


class VPAdapter (fragmentManager : FragmentManager, lifecylce : Lifecycle): FragmentStateAdapter(fragmentManager, lifecylce) {
    override fun getItemCount(): Int {
        return 3
    }

    override fun createFragment(position: Int): Fragment {
     return  when(position){
            0->{
                DestacadoFragment()
            }
            1->{
                RecomendadoFragment()
            }
            2->{
                NuevoFragment()
            }
            else->{
                Fragment()
            }

        }
    }


}