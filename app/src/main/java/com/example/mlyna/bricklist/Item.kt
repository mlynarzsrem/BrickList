package com.example.mlyna.bricklist

class Item(itemID : Int,col : Int,qty : Int,itemType : Int, itemNo: String,itemName: String){
    val id = itemID
    val color =col
    val quantity =qty
    var found = 0
    val type=itemType
    val number = itemNo
    val name = itemName
}