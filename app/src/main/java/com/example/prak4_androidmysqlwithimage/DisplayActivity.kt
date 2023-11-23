package com.example.prak4_androidmysqlwithimage

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject

class DisplayActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    var productList = ArrayList<Product>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_display)

        recyclerView = findViewById(R.id.rv)
        recyclerView.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)

        displayData()
    }

    private fun displayData() {
        //Construct the URL for retrieving data from the server
        val url: String = AppConfig().IP_SERVER + "/upload_image/view_data.php"

        //Create a Volley StringRequest for making a GET request to the server
        val stringRequest = object : StringRequest(Method.GET,url, Response.Listener { response ->
            //Clear the existing productList
            productList.clear()

            //Process the JSON response from the server
            val jsonObj = JSONObject(response)
            Toast.makeText(this,jsonObj.getString("message"),Toast.LENGTH_SHORT).show()

            //Extract the "data" array from the JSON response
            val jsonArray = jsonObj.getJSONArray("data")
            var product: Product
            productList.clear()

            //Iterate through the JSON array and create Product objects
            for (i in 0..jsonArray.length()-1) {
                val item = jsonArray.getJSONObject(i)
                product = Product()
                //Populate the Product object with data from the JSON object
                product.id      = item.getString("id")
                product.image   = AppConfig().IP_SERVER+"/upload_image/" + item.getString("image")
                product.caption = item.getString("caption")
                //Add the Product object to the productList
                productList.add(product)
            }
            //Set the productList in the adapter and update the RecyclerView
            recyclerView.adapter = MyAdapter(this@DisplayActivity, productList)
        },
            Response.ErrorListener { error->
                Toast.makeText(this,"Gagal Terhubung",Toast.LENGTH_SHORT).show()
            }
        ){}
        Volley.newRequestQueue(this).add(stringRequest)
    }
}