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
class ProjectAdapter(private val context: Context,private val dataSource: MutableList<Project>) : BaseAdapter(){
    override fun getCount(): Int {
        return dataSource.size
    }

    override fun getItem(p0: Int): Any {
        return dataSource[p0]
    }

    override fun getView(position: Int, p1: View?, parent: ViewGroup?): View {
        // Get view for row item
        val rowView = inflater.inflate(R.layout.project_item, parent, false)
        val nameTextView = rowView.findViewById<TextView>(R.id.project_name)
        val archivizedTextView = rowView.findViewById<TextView>(R.id.project_archivized)

        val project = getItem(position) as Project
        nameTextView.text = project.name
        if(project.archivized){
            archivizedTextView.text = "Nie aktywny"
        }
        else{
            archivizedTextView.text = "Aktywny"
        }
        return rowView
    }

    override fun getItemId(p0: Int): Long {
        return p0.toLong()
    }

    private val inflater: LayoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
}