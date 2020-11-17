package com.example.fancytimes.about

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.method.LinkMovementMethod
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import com.example.fancytimes.R
import com.example.fancytimes.databinding.FragmentAboutBinding

class AboutFragment : Fragment() {
    private lateinit var binding: FragmentAboutBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(layoutInflater, R.layout.fragment_about, container, false)
        setupHyperlink()

        val mailIntent = Intent(Intent.ACTION_SENDTO)
        mailIntent.data = Uri.parse("mailto:lhendeavours@gmail.com")

        binding.tvEmail.setOnClickListener {
            try {
                startActivity(mailIntent)
            } catch (e: Exception) {
                Toast.makeText(
                    this.activity,
                    "There are no email clients installed.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }

        return binding.root
    }

    private fun setupHyperlink() {
        binding.tvAboutMeText.movementMethod = LinkMovementMethod.getInstance()
    }
}