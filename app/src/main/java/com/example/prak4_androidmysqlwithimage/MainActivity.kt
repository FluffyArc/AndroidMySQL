package com.example.prak4_androidmysqlwithimage

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.squareup.picasso.Picasso
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.IOException

class MainActivity : AppCompatActivity() {
    private var id        = ""
    private var resId     = 0
    private lateinit var imageView  : ImageView
    private lateinit var caption    : EditText
    private lateinit var save       : Button
    private lateinit var display    : Button
    private lateinit var edit       : Button
    private lateinit var bitmap     : Bitmap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        imageView   = findViewById(R.id.clickToUploadImg)
        caption     = findViewById(R.id.editCaption)
        save        = findViewById(R.id.btnUpload)
        edit        = findViewById(R.id.btnUpdate)
        display     = findViewById(R.id.btnDisplay)

        imagePick()
        save.setOnClickListener {
            insertData()
        }
        display.setOnClickListener {
            displayData()
        }
        updateData()
    }

    private fun imagePick() {
        val activityResultLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result: ActivityResult ->
            if (result.resultCode == RESULT_OK) {
                val data = result.data!!
                val uri = data.data
                try {
                    bitmap = MediaStore.Images.Media.getBitmap(contentResolver, uri)
                    imageView.setImageBitmap(bitmap)
                    resId = 1
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }

        imageView.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "image/*"
            activityResultLauncher.launch(intent)
        }
    }

    private fun insertData() {
        if (resId == 1){
            //Convert Bitmap to Base64-encode String
            val byteArrayOutputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)
            val bytes = byteArrayOutputStream.toByteArray()
            val base64Image = Base64.encodeToString(bytes, Base64.DEFAULT)

            //Construct the URL for the server
            val url: String = AppConfig().IP_SERVER + "/upload_image/send_data.php"

            //Create a Volley StringRequest for sending data to the server
            val stringRequest = object : StringRequest(Method.POST,url,
                Response.Listener { response ->

                    //Handle the response from the server
                    val jsonObj = JSONObject(response)
                    Toast.makeText(this,jsonObj.getString("message"),
                        Toast.LENGTH_SHORT).show()
                    imageView.setImageResource(R.drawable.ic_upload)
                    caption.setText("")
                    resId = 0
                },

                Response.ErrorListener { _ ->
                    //Handle errors if there is a failure in the network request.
                    Toast.makeText(this,"Gagal Terhubung",
                        Toast.LENGTH_SHORT).show()
                }
            ){
                //Override the getParams method to provide the parameters for
                //the POST request
                override fun getParams(): HashMap<String,String>{
                    val params = HashMap<String,String>()
                    params["image"]     = base64Image
                    params["caption"]   = caption.text.toString()
                    return params
                }
            }
            //Add the StringRequest to the Volley RequestQueue
            Volley.newRequestQueue(this).add(stringRequest)
        }
        else{
            Toast.makeText(this,"Select the image first",Toast.LENGTH_SHORT).show()
        }
    }

    private fun displayData(){
        val intent = Intent(this@MainActivity, DisplayActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)

    }

    private fun updateData() {
        //Retrieve data from the intent bundle
        val bundle = intent.getBundleExtra("dataproduct")
        if (bundle != null) {
            //Extract data from the bundle
            id = bundle.getString("id")!!
            Picasso.get().load(bundle.getString("image")).into(imageView)
            caption.setText(bundle.getString("caption"))

            //visible edit button and hide save button
            save.visibility     = View.GONE
            edit.visibility     = View.VISIBLE

            //Set click listener for the edit button
            edit.setOnClickListener{
                if (resId == 1) {
                    //If new image selected
                    val byteArrayOutputStream = ByteArrayOutputStream()
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)
                    val bytes = byteArrayOutputStream.toByteArray()
                    val base64Image = Base64.encodeToString(bytes, Base64.DEFAULT)

                    //Construct the URL for updating data with image
                    val url1: String =
                        AppConfig().IP_SERVER + "/upload_image/update_dataWithImage.php"

                    //Create a Volley StringRequest for updating data with image
                    val stringRequest = object : StringRequest(Method.POST, url1,
                        Response.Listener { response ->
                            val jsonObj = JSONObject(response)
                            Toast.makeText(this, jsonObj.getString("message"), Toast.LENGTH_SHORT).show()
                            resId = 0
                        },
                        Response.ErrorListener { _ ->
                            Toast.makeText(this, "Gagal Terhubung", Toast.LENGTH_SHORT).show()
                        }
                    ) {
                        //Override the getParams method to provide parameters for the POST request
                        override fun getParams(): HashMap<String, String> {
                            val params = HashMap<String, String>()
                            params["id"] = id
                            params["image"] = base64Image
                            params["caption"] = caption.text.toString()
                            return params
                        }
                    }

                    //Add the StringRequest to the Volley RequestQueue
                    Volley.newRequestQueue(this).add(stringRequest)
                }
                else {
                    //If no new image is selected
                    val url2: String = AppConfig().IP_SERVER + "/upload_image/update_data.php"

                    //Create a Volley StringRequest for updating data without image
                    val stringRequest = object : StringRequest(Method.POST,url2,
                        Response.Listener { response ->
                            val jsonObj = JSONObject(response)
                            Toast.makeText(this,jsonObj.getString("message"),Toast.LENGTH_SHORT).show()
                            resId = 0

                            //Navigate to DisplayActivity after updating data
                            val intent = Intent(this@MainActivity, DisplayActivity::class.java)
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            startActivity(intent)
                        },
                        Response.ErrorListener { _ ->
                            Toast.makeText(this,"Gagal Terhubung",Toast.LENGTH_SHORT).show()
                        }
                    ){
                        //Override the getParams method to provide parameters for the POST request
                        override fun getParams(): HashMap<String,String>{
                            val params = HashMap<String,String>()
                            params["id"]        = id
                            params["caption"]   = caption.text.toString()
                            return params
                        }
                    }
                    //Add the StringRequest to the Volley RequestQueue
                    Volley.newRequestQueue(this).add(stringRequest)
                }
            }
        }
    }
}