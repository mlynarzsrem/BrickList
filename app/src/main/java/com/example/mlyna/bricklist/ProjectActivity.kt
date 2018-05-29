package com.example.mlyna.bricklist

import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.AsyncTask
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.text.Editable
import android.util.Log
import android.view.View
import android.widget.RadioButton
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_project.*
import kotlinx.android.synthetic.main.activity_settings.*
import java.net.HttpURLConnection
import java.net.URL
import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.Node
import org.w3c.dom.NodeList
import java.io.*
import javax.xml.parsers.DocumentBuilder
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.transform.OutputKeys
import javax.xml.transform.Transformer
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult

class ProjectActivity : AppCompatActivity() {
    var url : String ="http://fcds.cs.put.poznan.pl/MyWeb/BL/"
    var database : MyDBHandler?=null
    var inventoryId :Int =0
    var inventoryName :String =""
    var finalUrl: String=""
    var brickList = mutableListOf<Item>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_project)
        ReadFromFile()
        database = MyDBHandler(this,null,null,1)
        database?.createDataBase()
        inventoryId = intent.getIntExtra("id",0)
        inventoryName = intent.getStringExtra("name")
        ListView.setOnItemClickListener({parent, view, position, id->
            val item = ListView.getItemAtPosition(position) as Item
            showOptionDialog(item)
        })
        btnExport.setOnClickListener({
            try {
                writeXML()
            }
            catch (e : Exception){
                Log.i("tagdb","Error: ${e.toString()}")
            }
        })
        File("$filesDir/XML").deleteRecursively()
        val task = BgTask()
        task.execute()
    }
    fun ReadFromFile(){
        val settingsFile = File("$filesDir/settings.txt")
        if(settingsFile.isFile) {
            url = settingsFile.readText()
        }else {
            url = "http://fcds.cs.put.poznan.pl/MyWeb/BL/"
        }
    }
    private fun writeXML(){
        val docBuilder : DocumentBuilder =DocumentBuilderFactory.newInstance().newDocumentBuilder()
        val doc : Document =docBuilder.newDocument()

        val rootElement : Element = doc.createElement("INVENTORY")
        for (i in 0..brickList.size -1 ){
            val found = brickList[i].found
            val quantiyy =brickList[i].quantity
            if( found!= quantiyy){
                val item : Element =doc.createElement("ITEM")
                val type : Element = doc.createElement("ITEMTYPE")
                type.appendChild(doc.createTextNode("${database?.getById(brickList[i].type,"ItemTypes",1)}"))
                item.appendChild(type)
                val id : Element = doc.createElement("ITEMID")
                id.appendChild(doc.createTextNode("${database?.getById(brickList[i].id,"Parts",2)}"))
                item.appendChild(id)
                val color : Element = doc.createElement("COLOR")
                color.appendChild(doc.createTextNode("${database?.getById(brickList[i].color, "Colors",1)}"))
                item.appendChild(color)
                val qty : Element = doc.createElement("QTYFILLED")
                qty.appendChild(doc.createTextNode("${quantiyy - found}"))
                item.appendChild(qty)
                rootElement.appendChild(item)
            }
        }
        doc.appendChild(rootElement)
        val transformer : Transformer = TransformerFactory.newInstance().newTransformer()
        transformer.setOutputProperty(OutputKeys.INDENT,"yes")
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount","2")
        val path = "${this.filesDir}/XMLOUT"
        val outDir = File(path)
        outDir.mkdir()
        val file = File(outDir,"${inventoryName}_extra.xml")
        transformer.transform(DOMSource(doc),StreamResult(file))

        if(file.exists()) {
            val testUrl ="http://fcds.cs.put.poznan.pl/MyWeb/BL/615.xml"
            val i = Intent(Intent.ACTION_VIEW)
            i.addCategory(Intent.CATEGORY_BROWSABLE);
            i.setData(Uri.fromFile(file))
            //i.setDataAndType(Uri.fromFile(file), "application/x-webarchive-xml")
            i.setClassName("com.android.browser", "com.android.browser.BrowserActivity");
            startActivity(i)
        }

    }
    private fun readXMLFile(){
        val xmlFile = File("$filesDir/XML/$inventoryName.xml")
        var text =xmlFile.readText().replace("[^\\x20-\\x7e]", "")
        if(xmlFile.exists()){
            val xmlDoc : Document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(xmlFile)
            xmlDoc.documentElement.normalize()
            val itemList :NodeList =xmlDoc.getElementsByTagName("ITEM")
            for(i in 0..itemList.length-1){
                val itemNode : Node= itemList.item(i)
                if(itemNode.nodeType ===Node.ELEMENT_NODE){
                    val elem  =itemNode as Element
                    if(elem.getElementsByTagName("ALTERNATE").item(0).textContent.equals("N")!=false){
                        val itemNo = elem.getElementsByTagName("ITEMID").item(0).textContent
                        val itemID = database?.getByCode(elem.getElementsByTagName("ITEMID").item(0).textContent,"Parts",0)
                        val color  =  database?.getByCode(elem.getElementsByTagName("COLOR").item(0).textContent,"Colors",0)
                        val quantity  =  elem.getElementsByTagName("QTY").item(0).textContent.toInt()
                        val itemType = database?.getByCode(elem.getElementsByTagName("ITEMTYPE").item(0).textContent,"ItemTypes",0)
                        val name = database?.getByCode(elem.getElementsByTagName("ITEMID").item(0).textContent,"Parts",3)
                        if(itemID.isNullOrBlank()==false) {
                            val item = Item(itemID?.toInt()!!,color?.toInt()!!,quantity,itemType?.toInt()!!,itemNo,name!!)
                            database?.addItemIoInventory(item,inventoryId)
                            brickList.add(item)
                        }
                    }
                }
            }
        }
        brickList = database?.getItemsInInventory(inventoryId)!!
        val adapter = ItemAdapter(this, brickList)
        ListView.adapter = adapter
    }
    fun showOptionDialog(item: Item){
        val dialogBuilder = AlertDialog.Builder(this)
        val inflater = this.layoutInflater
        dialogBuilder.setTitle("Wybierz akcje ")
        val dialogView = inflater.inflate(R.layout.manage_bricks, null)
        val addBtn = dialogView.findViewById<View>(R.id.rbAdd) as RadioButton
        val subBtn = dialogView.findViewById<View>(R.id.rbSubstact) as RadioButton
        if(item.quantity==item.found){
            addBtn.isClickable =false
            addBtn.isChecked=false
            subBtn.isChecked =true
            addBtn.isEnabled =false
        }
        if(item.found==0){
            subBtn.isClickable =false
            subBtn.isEnabled =false
            subBtn.isChecked=false
            addBtn.isChecked =true

        }
        dialogBuilder.setView(dialogView)
        dialogBuilder.setPositiveButton("Tak", DialogInterface.OnClickListener { dialog, whichButton ->
            if(addBtn.isChecked) {
                if(item.quantity>item.found) {
                    database?.itemFound(inventoryId, item.id, item.color, item.found + 1)
                    brickList = database?.getItemsInInventory(inventoryId)!!
                    val adapter = ItemAdapter(this, brickList)
                    ListView.adapter = adapter
                }
            }
            else {
                if(0<item.found) {

                    database?.itemFound(inventoryId, item.id, item.color, item.found - 1)
                    brickList = database?.getItemsInInventory(inventoryId)!!
                    val adapter = ItemAdapter(this, brickList)
                    ListView.adapter = adapter
                }
            }
        })
        dialogBuilder.setNegativeButton("Nie", DialogInterface.OnClickListener { dialog, whichButton ->
        })
        val b = dialogBuilder.create()
        b.show()
    }
    private inner  class BgTask : AsyncTask<String,Int,String>(){
        override fun onPreExecute() {
            super.onPreExecute()
            finalUrl ="$url$inventoryName.xml"
        }
        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            try {
                readXMLFile()
            }
            catch (e : Exception){
                Log.i("tagdb","Error: ${e.toString()}")
            }
            Log.i("tagdb","Readen: ${brickList.size}")
        }
        override fun doInBackground(vararg p0: String?): String {
            Log.i("tagdb","Readen: ${finalUrl}")
            if(finalUrl.isBlank()==false){
                try {
                    val obj =URL(finalUrl)
                    val con = obj.openConnection() as HttpURLConnection
                    con.connect()
                    val lenghtOfFile = con.contentLength
                    val isStream = obj.openStream()
                    val testDirectory = File("$filesDir/XML")
                    if(!testDirectory.exists()) testDirectory.mkdir()
                    isStream.use { input ->
                        File("$testDirectory/$inventoryName.xml").outputStream().use { fileOut ->
                            input.copyTo(fileOut)
                        }
                    }
                    isStream.close()
                }
                catch (e : Exception){
                    Log.i("tagdb",e.toString())
                    return e.message.toString()
                }
            }
            return ""
        }

    }
}
