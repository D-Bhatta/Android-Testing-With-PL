// ShoppingFragment.kt
package com.example.testapplication.ui

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.testapplication.R

class ShoppingFragment : Fragment(R.layout.fragment_shopping) {

    lateinit var viewModel: ShoppingViewModel
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(requireActivity()).get(ShoppingViewModel::class.java)
    }
}