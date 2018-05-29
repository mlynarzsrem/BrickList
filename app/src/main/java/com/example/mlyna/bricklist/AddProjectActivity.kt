package com.example.mlyna.bricklist

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import kotlinx.android.synthetic.main.activity_add_project.*

class AddProjectActivity : AppCompatActivity() {
    var database : MyDBHandler?=null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_project)

        //Openning the database
        database = MyDBHandler(this,null,null,1)
        database?.createDataBase()
        btnAdd.setOnClickListener({
            addNewProject()
        })
        btnCancel.setOnClickListener({
            finish()
        })
    }
    fun addNewProject(){
        if(etProjectName.text.isNullOrBlank()==false){
            try {
                val project = Project(etProjectName.text.toString() ,0)
                if(database?.addToInventory(project)==true){
                    finish()
                }

            }
            catch (e : Exception){
                Log.i("tagdb","wrong number")
            }
        }
    }
}
