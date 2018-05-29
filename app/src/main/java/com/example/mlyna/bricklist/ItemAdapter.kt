package com.example.mlyna.bricklist

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView


/**
 * Created by mlyna on 17.05.2018.
 */
class ItemAdapter(private val context: Context, private val dataSource: MutableList<Item>) : BaseAdapter(){
    override fun getCount(): Int {
        return dataSource.size
    }

    override fun getItem(p0: Int): Any {
        return dataSource[p0]
    }

    override fun getView(position: Int, p1: View?, parent: ViewGroup?): View {
        // Get view for row item
        val rowView = inflater.inflate(R.layout.brick_item, parent, false)
        val quantity = rowView.findViewById<TextView>(R.id.qtyInInv)
        val found = rowView.findViewById<TextView>(R.id.qtyFind)
        val itemNo =rowView.findViewById<TextView>(R.id.ItemNo)
        val itemName =rowView.findViewById<TextView>(R.id.itemName)
        val item = getItem(position) as Item
        quantity.text = item.quantity.toString()
        found.text = item.found.toString()
        itemNo.text = item.number
        itemName.text =item.name

        return rowView
    }

    override fun getItemId(p0: Int): Long {
        return p0.toLong()
    }

    private val inflater: LayoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
}