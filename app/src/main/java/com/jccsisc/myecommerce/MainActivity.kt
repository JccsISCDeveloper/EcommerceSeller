package com.jccsisc.myecommerce

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.google.firebase.auth.FirebaseAuth
import com.jccsisc.myecommerce.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.apply {

            val providers = arrayListOf(AuthUI.IdpConfig.EmailBuilder().build())

            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                val response = IdpResponse.fromResultIntent(it.data)

                if (it.resultCode == RESULT_OK) {
                    val user = FirebaseAuth.getInstance().currentUser

                    if (user != null) {
                        Toast.makeText(this@MainActivity, "Bienvenido ", Toast.LENGTH_SHORT).show()
                    }
                }
            }.launch(AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(providers)
                .build())

        }
    }
}