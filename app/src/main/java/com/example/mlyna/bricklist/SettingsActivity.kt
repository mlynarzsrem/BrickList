package com.example.mlyna.bricklist

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import kotlinx.android.synthetic.main.activity_settings.*
import java.io.File

class SettingsActivity : AppCompatActivity() {

    var url: String="http://fcds.cs.put.poznan.pl/MyWeb/BL/"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        ReadFromFile()
        btnSave.setOnClickListener({
            writeToFile()
            finish()
        })
        btnCancel.setOnClickListener({
            finish()
        })
    }
    fun ReadFromFile(){
        val settingsFile = File("$filesDir/settings.txt")
        if(settingsFile.isFile) {
            url = settingsFile.readText()
            etUrl.text =Editable.Factory.getInstance().newEditable(url)
        }else {
            etUrl.text =Editable.Factory.getInstance().newEditable(url)
            writeToFile()
        }
    }
    fun writeToFile() {
        url = etUrl.text.toString()
        File("$filesDir/settings.txt").writeText(url)
    }
}
