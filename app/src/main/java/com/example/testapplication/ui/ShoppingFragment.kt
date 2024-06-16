// ShoppingFragment.kt
@file:Suppress("KDocMissingDocumentation")

package com.example.testapplication.ui

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.ItemTouchHelper.LEFT
import androidx.recyclerview.widget.ItemTouchHelper.RIGHT
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.testapplication.R
import com.example.testapplication.databinding.FragmentShoppingBinding
import com.google.android.material.snackbar.Snackbar
import javax.inject.Inject

class ShoppingFragment @Inject constructor(
    val shoppingItemAdapter: ShoppingItemAdapter, var viewModel: ShoppingViewModel?
) : Fragment(R.layout.fragment_shopping) {

    private var fragmentShoppingBinding: FragmentShoppingBinding? = null
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentShoppingBinding.bind(view)
        fragmentShoppingBinding = binding

        viewModel = viewModel ?: ViewModelProvider(requireActivity())[ShoppingViewModel::class.java]
        subscribeToObservers()
        setupRecyclerView()

        binding.fabAddShoppingItem.setOnClickListener {
            findNavController().navigate(
                ShoppingFragmentDirections.actionShoppingFragmentToAddShoppingItemFragment()
            )
        }
    }

    override fun onDestroyView() {
        fragmentShoppingBinding = null
        super.onDestroyView()
    }

    private val itemTouchCallback = object : ItemTouchHelper.SimpleCallback(
        0, LEFT or RIGHT
    ) {
        override fun onMove(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            target: RecyclerView.ViewHolder
        ): Boolean = true

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            val pos = viewHolder.layoutPosition
            val item = shoppingItemAdapter.shoppingItems[pos]
            viewModel?.deleteShoppingItem(item)
            Snackbar.make(
                requireView(), getString(R.string.item_deleted_successfully), Snackbar.LENGTH_LONG
            ).apply {
                setAction("UNDO") {
                    viewModel?.createShoppingItem(item)
                }
                show()
            }
        }
    }

    private fun setupRecyclerView() {
        fragmentShoppingBinding?.recyclerViewShoppingItems?.apply {
            adapter = shoppingItemAdapter
            layoutManager = LinearLayoutManager(requireContext())
            ItemTouchHelper(itemTouchCallback).attachToRecyclerView(this)
        }
    }

    private fun subscribeToObservers() {
        viewModel?.shoppingItems?.observe(viewLifecycleOwner, Observer {
            shoppingItemAdapter.shoppingItems = it
        })

        viewModel?.totalPrice?.observe(viewLifecycleOwner, Observer {
            val price = it ?: 0f
            val priceText = getString(R.string.total_price) + price.toString()
            fragmentShoppingBinding?.textViewShoppingItemPrice?.text = priceText
        })
    }
}
