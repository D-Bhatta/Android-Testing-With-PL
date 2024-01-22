// ImagePickFragment.kt
package com.example.testapplication.ui

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.example.testapplication.Constants.GRID_SPAN_COUNT
import com.example.testapplication.R
import com.example.testapplication.databinding.FragmentImagePickBinding
import javax.inject.Inject

class ImagePickFragment @Inject constructor(
    val imageAdapter: ImageAdapter
) : Fragment(R.layout.fragment_image_pick) {

    lateinit var viewModel: ShoppingViewModel

    private var fragmentImagePickBinding: FragmentImagePickBinding? = null
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding: FragmentImagePickBinding = FragmentImagePickBinding.bind(view)
        fragmentImagePickBinding = binding

        viewModel = ViewModelProvider(requireActivity()).get(ShoppingViewModel::class.java)

        imageAdapter.setOnItemClickListener { url ->
            findNavController().popBackStack()
            viewModel.setCurrentImageUrl(url = url)
        }

        setupRecyclerView()
    }

    override fun onDestroyView() {
        fragmentImagePickBinding = null
        super.onDestroyView()
    }

    private fun setupRecyclerView() {
        fragmentImagePickBinding?.recyclerViewImages?.apply {
            adapter = imageAdapter
            layoutManager = GridLayoutManager(requireContext(), GRID_SPAN_COUNT)
        }
    }

}
