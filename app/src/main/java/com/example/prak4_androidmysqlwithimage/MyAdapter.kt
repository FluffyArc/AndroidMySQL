package com.example.prak4_androidmysqlwithimage

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.squareup.picasso.Picasso
import org.json.JSONException
import org.json.JSONObject

class MyAdapter(var context: Context, private var productList: List<Product>) : RecyclerView.Adapter<MyAdapter.ImageViewHolder>() {
    @SuppressLint("InflateParams")
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.singledata, null)
        return ImageViewHolder(view)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        //Get the product at the current position
        val product = productList[position]

        //Load the image into the ImageView using Picasso
        Picasso.get().load(product.image).into(holder.imageView)

        //Set the caption text for the TextView in the ViewHolder
        holder.caption.text = productList[position].caption
        holder.flowmenu.setOnClickListener {
            //Create a PopupMenu associated with the flowmenu
            val popupMenu = PopupMenu(context, holder.flowmenu)

            //Inflate the menu layout (flow_menu.xml) into the PopupMenu
            popupMenu.inflate(R.menu.flow_menu)
            popupMenu.setOnMenuItemClickListener { menuItem: MenuItem ->
                when (menuItem.itemId) {
                    //Handle the "Edit" menu item
                    R.id.edit_menu -> {
                        //Prepare a bundle with product data
                        val bundle = Bundle()
                        bundle.putString("id", product.id)
                        bundle.putString("image", product.image)
                        bundle.putString("caption", product.caption)

                        //Create an intent to start the MainActivity with the product data bundle
                        val intent = Intent(context, MainActivity::class.java)
                        intent.putExtra("dataproduct", bundle)

                        //Start the MainActivity with the intent
                        context.startActivity(intent)
                    }

                    //Handle the "Delete" menu item
                    R.id.delete_menu -> {
                        //Show a confirmation dialog using MaterialAlertDialogBuilder
                        MaterialAlertDialogBuilder(context).setTitle("Delete").setMessage("Yakin hapus?")
                            .setPositiveButton("Delete"){_,_->
                                //Perform the deletion by making a network request
                                val url: String = AppConfig().IP_SERVER + "/upload_image/delete_data.php"
                                val strReq = object : StringRequest(Method.POST,url, Response.Listener { response ->
                                    try {
                                        //Handle the response after deletion
                                        val jsonObj = JSONObject(response)
                                        Toast.makeText(context, jsonObj.getString("message"), Toast.LENGTH_SHORT).show()

                                        //Refresh the MainActivity after deletion
                                        val intent = Intent(context, MainActivity::class.java)
                                        context.startActivity(intent)
                                    }
                                    catch (e: JSONException) { e.printStackTrace() } },
                                    Response.ErrorListener {}) {

                                    //Override the getParams method to provide parameters for the POST request
                                    override fun getParams(): HashMap<String,String>{
                                        val params = HashMap<String,String>()
                                        params["id"] = product.id
                                        return params
                                    }
                                }
                                //Add the request to the Volley RequestQueue
                                Volley.newRequestQueue(context).add(strReq)
                            }
                            .setNegativeButton("Cancel"){_,_->}.show()
                    }
                    //Return false for unhandled menu items
                    else -> return@setOnMenuItemClickListener false
                }
                //Return false by default
                false
            }
            //display the PopupMenu
            popupMenu.show()
        }
    }

    override fun getItemCount(): Int {
        return productList.size
    }

    inner class ImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var imageView   : ImageView
        var caption     : TextView
        var flowmenu    : ImageButton

        init {
            imageView   = itemView.findViewById(R.id.imageProduct)
            caption     = itemView.findViewById(R.id.txt_caption)
            flowmenu    = itemView.findViewById(R.id.flowmenu)
        }
    }
}
