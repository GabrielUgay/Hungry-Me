package com.example.hungryme

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView

interface OnItemQuantityChangeListener {
    fun onQuantityChanged(position: Int, quantity: Int)
}

class ItemAdapter(
    private val itemList: List<Item>,
    private val cartItems: MutableList<Bundle>,
    private val quantityChangeListener: OnItemQuantityChangeListener,
    private val user: String?
) : RecyclerView.Adapter<ItemAdapter.ItemViewHolder>() {

    class ItemViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.itemName)
        val price: TextView = view.findViewById(R.id.itemPrice)
        val image: ImageView = view.findViewById(R.id.imageItem)
        val subtract1: ImageView = view.findViewById(R.id.subtract1)
        val add1: ImageView = view.findViewById(R.id.add1)
        val value1: TextView = view.findViewById(R.id.value1)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_layout, parent, false)
        return ItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val item = itemList[position]
        holder.price.text = item.category
        holder.name.text = item.name

        val quantity = cartItems.find { it.getString("name") == item.name }?.getInt("quantity") ?: 0
        holder.value1.text = quantity.toString()

        holder.subtract1.setOnClickListener {
            val currentQuantity = cartItems.find { it.getString("name") == item.name }?.getInt("quantity") ?: 0
            if (currentQuantity > 0) {
                updateQuantity(item, currentQuantity - 1)
                holder.value1.text = (currentQuantity - 1).toString()
                quantityChangeListener.onQuantityChanged(position, currentQuantity - 1)
            } else {
                Toast.makeText(holder.itemView.context, "No items to be removed.", Toast.LENGTH_SHORT).show()
            }
        }

        holder.add1.setOnClickListener {
            val currentQuantity = cartItems.find { it.getString("name") == item.name }?.getInt("quantity") ?: 0
            if (currentQuantity < 10) {
                updateQuantity(item, currentQuantity + 1)
                holder.value1.text = (currentQuantity + 1).toString()
                quantityChangeListener.onQuantityChanged(position, currentQuantity + 1)
            } else {
                Toast.makeText(holder.itemView.context, "You reached the limit of 10 items.", Toast.LENGTH_SHORT).show()
            }
        }

        try {
            val resId = R.drawable::class.java.getField(item.file).getInt(null)
            holder.image.setImageResource(resId)
        } catch (e: Exception) {
            Log.e("ItemAdapter", "Image not found: ${item.file}, using fallback.", e)
            holder.image.setImageResource(R.drawable.pork)
        }

        // **Navigate to next activity when clicked**
        holder.itemView.setOnClickListener {
            val intent = Intent(holder.itemView.context, ItemActivity::class.java) // Change NextActivity to your actual target
            intent.putExtra("item_id", item.id)
            intent.putExtra("item_name", item.name)
            intent.putExtra("item_price", item.price) // Assuming price is a Double/Float
            Log.d("ItemAdapter", "Passing item price: ${item.price}")
            intent.putExtra("item_stock", item.stock)
            intent.putExtra("item_category", item.category)
            intent.putExtra("item_file", item.file)
            intent.putExtra("item_restaurant", item.restaurant)
            intent.putExtra("user", user)
            holder.itemView.context.startActivity(intent)
        }
    }


    override fun getItemCount() = itemList.size

    private fun updateQuantity(item: Item, newQuantity: Int) {
        val existingItem = cartItems.find { it.getString("name") == item.name }
        if (existingItem != null) {
            existingItem.putInt("quantity", newQuantity)
        } else {
            val bundle = Bundle()
            bundle.putString("name", item.name)
            bundle.putInt("quantity", newQuantity)
            bundle.putDouble("price", item.price)
            cartItems.add(bundle)
        }
    }
}
