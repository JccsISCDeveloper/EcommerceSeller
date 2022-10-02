package com.jccsisc.myecommerce.fragments.addproduct

import android.app.Activity
import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.widget.Button
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.jccsisc.myecommerce.Constants.COLL_PRODUCTS
import com.jccsisc.myecommerce.Constants.STORAGE_IMAGE
import com.jccsisc.myecommerce.MainAux
import com.jccsisc.myecommerce.R
import com.jccsisc.myecommerce.databinding.FragmentDialogAddBinding
import com.jccsisc.myecommerce.model.ProductModel
import com.jccsisc.myecommerce.utils.setColor
import com.jccsisc.myecommerce.utils.showToast
import com.jccsisc.myecommerce.utils.showView

/**
 * Project: MyEcommerce
 * FROM: com.jccsisc.myecommerce.fragments.addproduct
 * Created by Julio Cesar Camacho Silva on 30/09/22
 */
class AddDialogFragment : DialogFragment(), DialogInterface.OnShowListener {


    private var binding: FragmentDialogAddBinding? = null

    private var positiveButton: Button? = null
    private var negativeButton: Button? = null

    private var product: ProductModel? = null

    private var photoSelectedUri: Uri? = null
    private val resultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == Activity.RESULT_OK) {
                photoSelectedUri = it.data?.data

                //binding?.imgProduct?.setImageURI(photoSelectedUri)

                //Glide con imagen local
                binding?.let { v ->
                    Glide.with(this)
                        .load(photoSelectedUri)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .centerInside()
                        .into(v.imgProduct)

                    v.imbProduct
                }
            }
        }

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
        configButtons()

        val dialog = dialog as? AlertDialog
        dialog?.let {
            positiveButton = it.getButton(Dialog.BUTTON_POSITIVE)
            negativeButton = it.getButton(Dialog.BUTTON_NEGATIVE)

            positiveButton?.setOnClickListener {
                binding?.let { v ->

                    val nameD = v.tieName.text.toString()
                    val descriptionD = v.tieDescription.text.toString()
                    val quantityD = v.tieQuantity.text.toString()
                    val priceD = v.tiePrice.text.toString()

                    if (dataIsNotEmpty(nameD, descriptionD, priceD)) {
                        enableIU(false)

                        uploadImage(product?.id) { eventPost ->
                            if (eventPost.isSuccess) {
                                if (product == null) {
                                    val product = ProductModel(
                                        name = nameD,
                                        descrption = descriptionD,
                                        quantity = quantityD.toInt(),
                                        price = priceD.toDouble(),
                                        imgUrl = eventPost.photoUrl
                                    )

                                    eventPost.documentId?.let { it1 -> save(product, it1) }
                                } else {
                                    product?.apply {
                                        name = nameD
                                        descrption = descriptionD
                                        quantity = quantityD.toInt()
                                        price = priceD.toDouble()
                                        imgUrl = eventPost.photoUrl

                                        update(this)
                                    }
                                }
                            }
                        }
                    } else {
                        activity?.showToast("Error al ingresar los datos")
                    }
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

                Glide.with(this)
                    .load(product.imgUrl)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .centerInside()
                    .into(it.imgProduct)
            }
        }
    }

    private fun configButtons() {
        binding?.let {
            it.imbProduct.setOnClickListener {
                openGallery()
            }
        }
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        resultLauncher.launch(intent)
    }

    private fun uploadImage(productId: String?, callBack: (EventPost) -> Unit) {
        val eventPost = EventPost()
        eventPost.documentId =
            productId ?: FirebaseFirestore.getInstance().collection(COLL_PRODUCTS).document().id

        val storageRef = FirebaseStorage.getInstance().reference.child(STORAGE_IMAGE)

        photoSelectedUri?.let { uri ->
            binding?.let { v ->

                v.progressBar.showView()

                val photoRef = storageRef.child(eventPost.documentId!!)

                photoRef.putFile(uri)
                    .addOnProgressListener {

                        colorResponse(true)

                        val progress = (100 * it.bytesTransferred / it.totalByteCount).toInt()
                        it.run {
                            v.progressBar.progress = progress
                            v.tvProgress.text = String.format("%s%%", progress)
                        }

                    }
                    .addOnSuccessListener {
                        it.storage.downloadUrl.addOnSuccessListener { downloadUrl ->
                            Log.i("url", downloadUrl.toString())
                            eventPost.isSuccess = true
                            eventPost.photoUrl = downloadUrl.toString()
                            callBack(eventPost)
                        }
                    }
                    .addOnFailureListener {
                        eventPost.isSuccess = false
                        enableIU(true)
                        activity?.showToast("Error al subir imagen")
                        colorResponse(false)
                        callBack(eventPost)
                    }
            }
        }
    }

    private fun save(product: ProductModel, documentId: String) {
        val db = FirebaseFirestore.getInstance()

        db.collection(COLL_PRODUCTS)
            //.add(product)
            .document(documentId)
            .set(product)
            .addOnSuccessListener {
                activity?.showToast("Producto agregado correctamente.")
            }
            .addOnFailureListener {
                activity?.showToast("Error al agregar el producto.")
            }
            .addOnCompleteListener {
                enableIU(true)
                binding?.progressBar?.showView(false)
                dismiss()
            }
    }

    private fun update(product: ProductModel) {
        val db = FirebaseFirestore.getInstance()

        product.id?.let { id ->
            db.collection(COLL_PRODUCTS)
                .document(id)
                .set(product)
                .addOnSuccessListener {
                    activity?.showToast("Producto actualizado correctamente.")
                }
                .addOnFailureListener {
                    activity?.showToast("Error al actualizar el producto.")
                }
                .addOnCompleteListener {
                    enableIU(true)
                    binding?.progressBar?.showView(false)
                    dismiss()
                }
        }
    }

    private fun enableIU(enable: Boolean) {
        positiveButton?.isEnabled = enable
        negativeButton?.isEnabled = enable

        binding?.let {
            with(it) {
                tieName.isEnabled = enable
                tieDescription.isEnabled = enable
                tieQuantity.isEnabled = enable
                tiePrice.isEnabled = enable
            }
        }
    }

    private fun dataIsNotEmpty(name: String, description: String, price: String) =
        name.isNotEmpty() && description.isNotEmpty() && price != "0.0" && price != "0"

    private fun colorResponse(isOk: Boolean) {

        activity?.setColor(if (isOk) R.color.blue_gray_800_dark else R.color.red_100)
            ?.let { it1 -> binding?.tvProgress?.setTextColor(it1) }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

}