package com.jccsisc.myecommerce

import android.app.Activity
import android.view.View
import android.widget.Toast

/**
 * Project: MyEcommerce
 * FROM: com.jccsisc.myecommerce
 * Created by Julio Cesar Camacho Silva on 02/10/22
 */


fun View.showView(showV: Boolean = true) {
    if (showV) this.visibility = View.VISIBLE else this.visibility = View.GONE
}

fun Activity.showToast(msg: String, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, msg, duration).show()
}