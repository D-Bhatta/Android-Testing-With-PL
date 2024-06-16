// ShoppingItemAdapter.kt
@file:Suppress("KDocMissingDocumentation")

package com.example.testapplication.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestManager
import com.example.testapplication.data.local.ShoppingItem
import com.example.testapplication.databinding.ItemShoppingBinding
import javax.inject.Inject

class ShoppingItemAdapter @Inject constructor(
    private val glide: RequestManager
) : RecyclerView.Adapter<ShoppingItemAdapter.ShoppingItemHolder>() {

    class ShoppingItemHolder(val itemShoppingBinding: ItemShoppingBinding) :
        RecyclerView.ViewHolder(itemShoppingBinding.root)

    private val diffCallback = object : DiffUtil.ItemCallback<ShoppingItem>() {
        override fun areItemsTheSame(oldItem: ShoppingItem, newItem: ShoppingItem): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: ShoppingItem, newItem: ShoppingItem): Boolean {
            @Suppress(
                "MaxLineLength",
                "LongLine"
            ) return (oldItem.id == newItem.id) && (oldItem.name == newItem.name) && (oldItem.amount == newItem.amount) && (oldItem.price == newItem.price) && (oldItem.imageURL == newItem.imageURL)
        }

    }

    private val differ = AsyncListDiffer(this, diffCallback)

    var shoppingItems: List<ShoppingItem>
        get() = differ.currentList
        set(value) = differ.submitList(value)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ShoppingItemHolder {
        return ShoppingItemHolder(
            ItemShoppingBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun getItemCount(): Int {
        return shoppingItems.size
    }

    override fun onBindViewHolder(holder: ShoppingItemHolder, position: Int) {
        val shoppingItem = shoppingItems[position]
        holder.itemView.apply {
            glide.load(shoppingItem.imageURL)
                .into(holder.itemShoppingBinding.imageViewShoppingImage)
            holder.itemShoppingBinding.textViewShoppingItemName.text = shoppingItem.name
            holder.itemShoppingBinding.textViewShoppingItemPrice.text =
                shoppingItem.price.toString()
            holder.itemShoppingBinding.textViewShoppingItemQuantity.text =
                shoppingItem.amount.toString()
        }
    }
}