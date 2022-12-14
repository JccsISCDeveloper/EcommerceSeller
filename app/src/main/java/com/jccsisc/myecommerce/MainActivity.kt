package com.jccsisc.myecommerce

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.result.contract.ActivityResultContracts
import androidx.recyclerview.widget.GridLayoutManager
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.ErrorCodes
import com.firebase.ui.auth.IdpResponse
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.jccsisc.myecommerce.Constants.COLL_PRODUCTS
import com.jccsisc.myecommerce.adapter.OnProductListener
import com.jccsisc.myecommerce.adapter.ProductAdapter
import com.jccsisc.myecommerce.databinding.ActivityMainBinding
import com.jccsisc.myecommerce.fragments.addproduct.AddDialogFragment
import com.jccsisc.myecommerce.model.ProductModel
import com.jccsisc.myecommerce.utils.showToast
import com.jccsisc.myecommerce.utils.showView

class MainActivity : AppCompatActivity(), OnProductListener, MainAux {

    lateinit var binding: ActivityMainBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var authStateListener: FirebaseAuth.AuthStateListener

    private lateinit var adapter: ProductAdapter
    private lateinit var firestoreListener: ListenerRegistration

    private var productSelected: ProductModel? = null

    private val resultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            val response = IdpResponse.fromResultIntent(it.data)

            if (it.resultCode == RESULT_OK) {
                val user = FirebaseAuth.getInstance().currentUser

                if (user != null) {
                    showToast("Bienvenido")
                    binding.nsvProducts.showView()
                    binding.linearLayoutProgress.showView(false)
                }
            } else {
                if (response == null) {
                    showToast("Hasta pronto")
                    finish()
                } else {
                    response.error?.let { error ->
                        if (error.errorCode == ErrorCodes.NO_NETWORK) {
                            showToast("No hay conexi??n a internet")
                        } else {
                            showToast("C??digo de error: ${error.errorCode}")
                        }
                    }
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initElements()

    }

    fun configRv() {
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

    fun configAuth() {

        firebaseAuth = FirebaseAuth.getInstance()
        authStateListener = FirebaseAuth.AuthStateListener { auth ->

            if (auth.currentUser != null) {
                supportActionBar?.title = auth.currentUser?.displayName
                binding.linearLayoutProgress.showView(false)
                binding.nsvProducts.showView()
                binding.efab.show()
            } else {
                val providers = arrayListOf(
                    AuthUI.IdpConfig.EmailBuilder().build(),
                    AuthUI.IdpConfig.GoogleBuilder().build()
                )

                //Lanzamos la Intent por defecto de la liber??a con los proveedores
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
        configFifestoreRealtime()
    }

    override fun onPause() {
        super.onPause()
        firebaseAuth.removeAuthStateListener(authStateListener)
        firestoreListener.remove()
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
                        showToast("Cerraste tu sesi??n")
                    }
                    .addOnCompleteListener {
                        if (it.isSuccessful) {
                            showToast("Hacer otra acci??n")
                            binding.nsvProducts.showView(false)
                            binding.linearLayoutProgress.showView()
                            binding.efab.hide()
                        } else {
                            showToast("Hacer otra acci??n")
                        }
                    }
            }
        }
        return super.onOptionsItemSelected(item)
    }

    fun configRifestore() {
        val db = FirebaseFirestore.getInstance()
        db.collection(COLL_PRODUCTS)
            .get()
            .addOnSuccessListener { snapshots ->
                for (document in snapshots) {
                    val producto = document.toObject(ProductModel::class.java)
                    producto.id = document.id
                    adapter.add(producto)
                }
            }
            .addOnFailureListener {
                showToast("Error al consultar datos")
            }
    }

    fun configFifestoreRealtime() {
        val db = FirebaseFirestore.getInstance()

        val productRef = db.collection(COLL_PRODUCTS)

        firestoreListener = productRef.addSnapshotListener { snapshots, error ->
            if (error != null) {
                showToast("Error al consultar datos")
                return@addSnapshotListener
            }

            for (snapshot in snapshots!!.documentChanges) {
                val producto = snapshot.document.toObject(ProductModel::class.java)
                producto.id = snapshot.document.id
                when(snapshot.type) {
                    DocumentChange.Type.ADDED -> adapter.add(producto)
                    DocumentChange.Type.MODIFIED -> adapter.update(producto)
                    DocumentChange.Type.REMOVED -> adapter.delete(producto)
                }
            }
        }

    }

    fun configButtons() {
        binding.efab.setOnClickListener {
            productSelected = null
            AddDialogFragment().show(supportFragmentManager, AddDialogFragment::class.java.simpleName)
        }
    }

    override fun onClick(product: ProductModel) {
        productSelected = product
        AddDialogFragment().show(supportFragmentManager, AddDialogFragment::class.java.simpleName)
    }

    override fun onLongClick(product: ProductModel) {
        val db = FirebaseFirestore.getInstance()
        val productRef = db.collection(COLL_PRODUCTS)

        product.id?.let { id ->
            productRef.document(id)
                .delete()
                .addOnFailureListener {
                    showToast("Error al eleiminar el producto")
                }
        }
    }

    override fun getProductSelected(): ProductModel? = productSelected
}