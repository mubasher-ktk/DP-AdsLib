package com.dp.ads.lib.activities

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.dp.ads.lib.databinding.FragmentWalkThroughFullScreenAdBinding

class WTFullScreenAdFragment : Fragment() {

    private lateinit var binding: FragmentWalkThroughFullScreenAdBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentWalkThroughFullScreenAdBinding.inflate(layoutInflater)
        return binding.root
    }


}