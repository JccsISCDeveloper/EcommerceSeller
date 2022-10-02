package com.jccsisc.myecommerce

/**
 * Project: MyEcommerce
 * FROM: com.jccsisc.myecommerce
 * Created by Julio Cesar Camacho Silva on 02/10/22
 */
fun MainActivity.initElements() {
    binding.apply {

        configAuth()
        configRv()
        //configRifestore()
        configRifestoreRealtime()
        configButtons()

    }
}