package com.jccsisc.myecommerce.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.jccsisc.myecommerce.R
import com.jccsisc.myecommerce.databinding.ItemProductBinding
import com.jccsisc.myecommerce.model.ProductModel

/**
 * Project: MyEcommerce
 * FROM: com.jccsisc.myecommerce.adapter
 * Created by Julio Cesar Camacho Silva on 29/09/22
 */
class ProductAdapter(private val productList: MutableList<ProductModel>, private val listener: OnProductListener): RecyclerView.Adapter<ProductAdapter.ViewHolder>() {

    private lateinit var context: Context

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        context = parent.context
        val view = LayoutInflater.from(context).inflate(R.layout.item_product, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val product = productList[position]
        holder.setListener(product)
        holder.binding.tvName.text = product.name
        holder.binding.tvPrice.text = context.getString(R.string.str_price, product.price.toString())
        holder.binding.tvQuantity.text = product.quantity.toString()

        Glide.with(context)
            .load(product.imgUrl)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .placeholder(R.drawable.ic_time_img)
            .error(R.drawable.ic_error_img)
            .centerInside()
            .into(holder.binding.imgProduct)
    }

    override fun getItemCount() = productList.size


    inner class  ViewHolder(view: View): RecyclerView.ViewHolder(view) {
        val binding = ItemProductBinding.bind(view)

        fun setListener(product: ProductModel) {
            binding.root.setOnClickListener {
                listener.onClick(product)
            }

            binding.root.setOnLongClickListener {
                listener.onLongClick(product)
                true
            }
        }
    }

    fun add(product: ProductModel) {
        if (!productList.contains(product)) {
            productList.add(product)
            notifyItemInserted(productList.size -1)
        } else {
            update(product)
        }
    }

    fun update(product: ProductModel) {
        val index = productList.indexOf(product)
        if (index != -1) {
            productList[index] = product
            notifyItemChanged(index)
        }
    }

    fun delete(product: ProductModel) {
        val index = productList.indexOf(product)
        if (index != -1) {
            productList.removeAt(index)
            notifyItemRemoved(index)
        }
    }
}