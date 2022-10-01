package com.jccsisc.myecommerce.model

import com.google.firebase.firestore.Exclude

/**
 * Project: MyEcommerce
 * FROM: com.jccsisc.myecommerce.model
 * Created by Julio Cesar Camacho Silva on 29/09/22
 */
data class ProductModel(
    @get:Exclude var id: String? = null,
    var name: String? = null,
    var descrption: String? = null,
    var imgUrl: String? = null,
    var quantity: Int = 0,
    var price: Double = 0.0

) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ProductModel

        if (id != other.id) return false

        return true
    }

    override fun hashCode(): Int {
        return id?.hashCode() ?: 0
    }
}
