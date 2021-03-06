package com.example.fancytimes

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController


class TrashFragment : Fragment() {


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        this.findNavController().navigate(TrashFragmentDirections.actionTrashFragmentToHomeFragment())
        return inflater.inflate(R.layout.fragment_trash, container, false)
    }
}