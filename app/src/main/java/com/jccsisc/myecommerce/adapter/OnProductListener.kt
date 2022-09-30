package com.jccsisc.myecommerce.adapter

import com.jccsisc.myecommerce.model.ProductModel

/**
 * Project: MyEcommerce
 * FROM: com.jccsisc.myecommerce.adapter
 * Created by Julio Cesar Camacho Silva on 29/09/22
 */
interface OnProductListener {
    fun onClick(product: ProductModel)
    fun onLongClick(product: ProductModel)
}