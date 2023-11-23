package com.example.prak4_androidmysqlwithimage

//This is our Model Class --> representation of the entities in our table
class Product {
    var id: String = ""
        get() = field
        set(value) { field = value }

    var image: String = ""
        get() = field
        set(value) { field = value }

    var caption: String = ""
        get() = field
        set(value) { field = value }
}