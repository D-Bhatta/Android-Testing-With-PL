// AddShoppingItemFragment.kt
package com.example.testapplication.ui

import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.RequestManager
import com.example.testapplication.R
import com.example.testapplication.Status
import com.example.testapplication.databinding.FragmentAddShoppingItemBinding
import com.google.android.material.snackbar.Snackbar
import javax.inject.Inject

@Suppress("KDocMissingDocumentation")
class AddShoppingItemFragment @Inject constructor(
    val glide: RequestManager
) : Fragment(R.layout.fragment_add_shopping_item) {

    lateinit var viewModel: ShoppingViewModel

    private var fragmentAddShoppingItemBinding: FragmentAddShoppingItemBinding? = null
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentAddShoppingItemBinding.bind(view)
        fragmentAddShoppingItemBinding = binding

        viewModel = ViewModelProvider(requireActivity())[ShoppingViewModel::class.java]

        subscribeToObservers()

        binding.imageViewShoppingImage.setOnClickListener {
            findNavController().navigate(
                AddShoppingItemFragmentDirections.actionAddShoppingItemFragmentToImagePickFragment()
            )
        }
        binding.addShoppingItemBtn.setOnClickListener {
            viewModel.insertShoppingItem(
                binding.editTextShoppingItemName.text.toString(),
                binding.editTextShoppingItemQuantity.text.toString(),
                binding.editTextShoppingItemPrice.text.toString()
            )
        }

        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                viewModel.setCurrentImageUrl("")
                findNavController().popBackStack()
            }
        }

        requireActivity().onBackPressedDispatcher.addCallback(callback)
    }

    override fun onDestroyView() {
        fragmentAddShoppingItemBinding = null
        super.onDestroyView()
    }

    private fun subscribeToObservers() {
        viewModel.currentImageUrl.observe(viewLifecycleOwner, Observer { url ->
            fragmentAddShoppingItemBinding?.let {
                glide.load(url).into(it.imageViewShoppingImage)
            }
        })

        viewModel.insertShoppingItemStatus.observe(viewLifecycleOwner, Observer {
            it.getContentIfUnintercepted()?.let { result ->
                when (result.status) {
                    Status.SUCCESS -> {
                        Snackbar.make(
                            requireView(),
                            getString(R.string.shopping_item_added),
                            Snackbar.LENGTH_LONG
                        ).show()
                    }

                    Status.ERROR -> {
                        Snackbar.make(
                            requireView(),
                            result.message?.toString()
                                ?: getString(R.string.error_adding_shopping_item),
                            Snackbar.LENGTH_LONG
                        ).show()
                    }

                    Status.LOADING -> {
                        /* NO-OP */
                    }
                }
            }
        })
    }
}
