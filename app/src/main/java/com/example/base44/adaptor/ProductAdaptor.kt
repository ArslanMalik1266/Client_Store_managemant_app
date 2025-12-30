package com.example.base44.adaptor

import com.example.base44.dataClass.Product

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.base44.R
import com.google.android.material.checkbox.MaterialCheckBox

class ProductAdapter(
    private val products: List<Product>,
    private val onAddToCartClicked: (product: Product) -> Unit,
    private val onSelectionChanged: (List<Product>) -> Unit
) : RecyclerView.Adapter<ProductAdapter.ProductViewHolder>() {

    inner class ProductViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imgProduct: ImageView = itemView.findViewById(R.id.imgProduct)
        val tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        val tvCode: TextView = itemView.findViewById(R.id.tvCode)
        val cbAddToCart: MaterialCheckBox = itemView.findViewById(R.id.cbAddToCart)
        val btnAdd: Button = itemView.findViewById(R.id.btnAdd)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.activity_item_view_rv, parent, false)
        return ProductViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val product = products[position]

        holder.imgProduct.setImageResource(product.imageRes)
        holder.tvTitle.text = product.title
        holder.tvCode.text = product.code
        holder.cbAddToCart.isChecked = product.isAddedToCart

        // When checkbox clicked
        holder.cbAddToCart.setOnCheckedChangeListener { _, isChecked ->
            product.isAddedToCart = isChecked
            val selectedProducts = products.filter { it.isAddedToCart }
            onSelectionChanged(selectedProducts)

        }

        // When Add button clicked
        holder.btnAdd.setOnClickListener {
            onAddToCartClicked(product)
        }
    }

    override fun getItemCount(): Int = products.size
}
