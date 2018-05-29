package com.example.mlyna.bricklist

import android.content.DialogInterface
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.RadioButton
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {


    var database : MyDBHandler?=null
    val file ="projects9.dat"
    var projectList = mutableListOf<Project>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        //Openning the database
        database = MyDBHandler(this,null,null,1)
        Log.i("tagdb",database?.createDataBase().toString())
        projectList = database?.getInventory()!!

        showProjectsOnList()
        cbShowArchivized.setOnClickListener({
            showProjectsOnList()
        })
        btnNewProject.setOnClickListener({
            val intent = Intent(this, AddProjectActivity::class.java)
            startActivity(intent)
        })
        projectListView.setOnItemClickListener({parent, view, position, id->
            val project = projectListView.getItemAtPosition(position) as Project
            showOptionDialog(project)
        })
        btnSettings.setOnClickListener({
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
        })
    }
    fun showOptionDialog(project: Project){
        val dialogBuilder = AlertDialog.Builder(this)
        val inflater = this.layoutInflater
        dialogBuilder.setTitle("Wybierz akcje ")
        val dialogView = inflater.inflate(R.layout.custom_dialog, null)
        val deleteBtn = dialogView.findViewById<View>(R.id.rbDelete) as RadioButton
        val openBtn = dialogView.findViewById<View>(R.id.rbOpen) as RadioButton
        val archivizeBtn = dialogView.findViewById<View>(R.id.rbArchivize) as RadioButton
        dialogBuilder.setView(dialogView)
        var active =true
        if(project.archivized==false) {
            active=false
            archivizeBtn.text = "Archiwizuj"
        }
        else{
            active=true
            archivizeBtn.text = "Aktywuj"
        }
        dialogBuilder.setPositiveButton("Tak", DialogInterface.OnClickListener { dialog, whichButton ->
            if(archivizeBtn.isChecked) {
                database?.changeActiveState(project.number, active)
                projectList = database?.getInventory()!!
                showProjectsOnList()
            }
            else {
                if (openBtn.isChecked) {
                    val intent = Intent(this, ProjectActivity::class.java)
                    intent.putExtra("id", project.number)
                    intent.putExtra("name", project.name)
                    startActivity(intent)
                }else{
                    database?.deleteInventory(project.number)
                    projectList = database?.getInventory()!!
                    showProjectsOnList()
                }
            }
        })
        dialogBuilder.setNegativeButton("Nie", DialogInterface.OnClickListener { dialog, whichButton ->
        })
        val b = dialogBuilder.create()
        b.show()
    }

    /**
     * Show projects on project list
     */
    fun showProjectsOnList(){
        var toShow = mutableListOf<Project>()
        if(cbShowArchivized.isChecked){
                toShow =projectList
        }
        else{
            for (project in projectList){
                if(project.archivized==false){
                    toShow.add(project)
                }
            }
        }
        val adapter = ProjectAdapter(this,toShow)
        projectListView.adapter = adapter
    }

     override fun onRestart() {
         super.onRestart()
         projectList = database?.getInventory()!!
         showProjectsOnList()
     }

    override fun onResume() {
        super.onResume()
        projectList = database?.getInventory()!!
        showProjectsOnList()
    }
    }
