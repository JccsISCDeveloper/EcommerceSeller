package com.jccsisc.myecommerce.fragments.addproduct

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.google.firebase.firestore.FirebaseFirestore
import com.jccsisc.myecommerce.MainAux
import com.jccsisc.myecommerce.databinding.FragmentDialogAddBinding
import com.jccsisc.myecommerce.model.ProductModel

/**
 * Project: MyEcommerce
 * FROM: com.jccsisc.myecommerce.fragments.addproduct
 * Created by Julio Cesar Camacho Silva on 30/09/22
 */
class AddDialogFragment: DialogFragment(), DialogInterface.OnShowListener {


    private var binding: FragmentDialogAddBinding? = null

    private var positiveButton: Button? = null
    private var negativeButton: Button? = null

    private var product: ProductModel? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        activity?.let { activity ->
            binding = FragmentDialogAddBinding.inflate(LayoutInflater.from(context))

            binding?.let {
                val builder = AlertDialog.Builder(activity)
                    .setTitle("Agregar producto")
                    .setPositiveButton("Agregar", null)
                    .setNegativeButton("Cancelar", null)
                    .setView(it.root)

                val dialog = builder.create()
                dialog.setOnShowListener(this)

                return dialog

            }
        }
        return super.onCreateDialog(savedInstanceState)
    }

    override fun onShow(dialogInterface: DialogInterface?) {

        initProduct()

        val dialog = dialog as? AlertDialog
        dialog?.let {
            positiveButton = it.getButton(Dialog.BUTTON_POSITIVE)
            negativeButton = it.getButton(Dialog.BUTTON_NEGATIVE)

            positiveButton?.setOnClickListener {
                binding?.let { v ->
                    val product = ProductModel(
                        name = v.tieName.text.toString().trim(),
                        descrption = v.tieDescription.text.toString().trim(),
                        quantity = v.tieQuantity.text.toString().toInt(),
                        price = v.tiePrice.text.toString().toDouble()
                    )

                    save(product)
                }
            }

            negativeButton?.setOnClickListener {
                dismiss()
            }
        }
    }

    private fun initProduct() {
        product = (activity as? MainAux)?.getProductSelected()

        product?.let { product ->
            binding?.let {
                it.tieName.setText(product.name)
                it.tieDescription.setText(product.descrption)
                it.tieQuantity.setText(product.quantity.toString())
                it.tiePrice.setText(product.price.toString())
            }
        }
    }

    private fun save(product: ProductModel) {
        val db = FirebaseFirestore.getInstance()

        db.collection("Products")
            .add(product)
            .addOnSuccessListener {
                Toast.makeText(activity, "Producto agregado correctamente.", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(activity, "Error al agregar el producto.", Toast.LENGTH_SHORT).show()
            }
            .addOnCompleteListener {
                dismiss()
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

}