package com.jccsisc.myecommerce

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.recyclerview.widget.GridLayoutManager
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.ErrorCodes
import com.firebase.ui.auth.IdpResponse
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.jccsisc.myecommerce.adapter.OnProductListener
import com.jccsisc.myecommerce.adapter.ProductAdapter
import com.jccsisc.myecommerce.databinding.ActivityMainBinding
import com.jccsisc.myecommerce.fragments.addproduct.AddDialogFragment
import com.jccsisc.myecommerce.model.ProductModel

class MainActivity : AppCompatActivity(), OnProductListener {

    private lateinit var binding: ActivityMainBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var authStateListener: FirebaseAuth.AuthStateListener

    private lateinit var adapter: ProductAdapter

    private val resultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            val response = IdpResponse.fromResultIntent(it.data)

            if (it.resultCode == RESULT_OK) {
                val user = FirebaseAuth.getInstance().currentUser

                if (user != null) {
                    Toast.makeText(this@MainActivity, "Bienvenido ", Toast.LENGTH_SHORT).show()
                    binding.nsvProducts.visibility = View.VISIBLE
                    binding.linearLayoutProgress.visibility = View.GONE
                }
            } else {
                if (response == null) {
                    Toast.makeText(this@MainActivity, "Hasta pronto ", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    response.error?.let { error ->
                        if (error.errorCode == ErrorCodes.NO_NETWORK) {
                            Toast.makeText(this, "No hay conexión a internet", Toast.LENGTH_SHORT)
                                .show()
                        } else {
                            Toast.makeText(
                                this,
                                "Código de error: ${error.errorCode}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.apply {

            configAuth()
            configRv()
            configRifestore()
            configButtons()
        }
    }

    private fun configRv() {
        adapter = ProductAdapter(mutableListOf(), this)
        binding.rvProducts.apply {
            layoutManager =
                GridLayoutManager(this@MainActivity, 3, GridLayoutManager.HORIZONTAL, false)
            adapter = this@MainActivity.adapter
        }

        /*(1..20).forEach {
            val product = ProductModel(
                it.toString(),
                "Producto: $it",
                "Este producto es el $it",
                "",
                it,
                it * 1.2
            )
            adapter.add(product)
        }*/
    }

    private fun configAuth() {

        firebaseAuth = FirebaseAuth.getInstance()
        authStateListener = FirebaseAuth.AuthStateListener { auth ->

            if (auth.currentUser != null) {
                supportActionBar?.title = auth.currentUser?.displayName
                binding.linearLayoutProgress.visibility = View.GONE
                binding.nsvProducts.visibility = View.VISIBLE
                binding.efab.show()
            } else {
                val providers = arrayListOf(
                    AuthUI.IdpConfig.EmailBuilder().build(),
                    AuthUI.IdpConfig.GoogleBuilder().build()
                )

                resultLauncher.launch(
                    AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setAvailableProviders(providers)
                        .setIsSmartLockEnabled(false)
                        .build()
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        firebaseAuth.addAuthStateListener(authStateListener)
    }

    override fun onPause() {
        super.onPause()
        firebaseAuth.removeAuthStateListener(authStateListener)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_sign_out -> {
                AuthUI.getInstance().signOut(this)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Cerraste tu sesión", Toast.LENGTH_SHORT).show()
                    }
                    .addOnCompleteListener {
                        if (it.isSuccessful) {
                            Toast.makeText(this, "Hacer otra acción", Toast.LENGTH_SHORT).show()
                            binding.nsvProducts.visibility = View.GONE
                            binding.linearLayoutProgress.visibility = View.VISIBLE
                            binding.efab.hide()
                        } else {
                            Toast.makeText(this, "Hacer otra acción", Toast.LENGTH_SHORT).show()
                        }
                    }
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun configRifestore() {
        val db = FirebaseFirestore.getInstance()
        db.collection("Products")
            .get()
            .addOnSuccessListener { snapshots ->
                for (document in snapshots) {
                    val producto = document.toObject(ProductModel::class.java)
                    adapter.add(producto)
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al consultar datos", Toast.LENGTH_SHORT).show()
            }
    }

    private fun configButtons() {
        binding.efab.setOnClickListener {
            AddDialogFragment().show(supportFragmentManager, AddDialogFragment::class.java.simpleName)
        }
    }

    override fun onClick(product: ProductModel) {

    }

    override fun onLongClick(product: ProductModel) {

    }
}