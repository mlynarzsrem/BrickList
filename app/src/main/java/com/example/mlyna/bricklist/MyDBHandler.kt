package com.example.mlyna.bricklist

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import java.io.File
import java.io.FileOutputStream

/**
 * Created by mlyna on 18.05.2018.
 */

class MyDBHandler(context : Context,name: String?,factory: SQLiteDatabase.CursorFactory?,version: Int):SQLiteOpenHelper(context,DATABASE_NAME,factory,DATABASE_VERSION){
    private val context = context
    private var myDataBase :SQLiteDatabase? =null
    override fun onUpgrade(p0: SQLiteDatabase?, p1: Int, p2: Int) {

    }

    override fun onCreate(p0: SQLiteDatabase?) {
    }
    public fun createDataBase() : Int{
        Log.i("tagdb","Creating the db")
        val dbExist =checkDataBase()
        if (dbExist==false){
            this.readableDatabase
            try {
                copyDataBase()
                Log.i("tagdb","Data base has been copied")
                return 1

            }
            catch (e : Exception){
                Log.i("tagdb",e.toString())
                return -1
            }
        }
        openDataBase()
        return 0
    }
    fun openDataBase() : Boolean{
        val dbLocation = PATH + DATABASE_NAME
        try {
            myDataBase = SQLiteDatabase.openDatabase(dbLocation, null, SQLiteDatabase.OPEN_READWRITE)
            if(File(dbLocation).isFile) {
                Log.i("tagdb", "Data base has been opemed succsfully")
            }
            return true
        }
        catch (e : Exception){
            Log.i("tagdb",e.toString())
            return false
        }
    }
    private fun checkDataBase() : Boolean{
        val dbLocation = PATH + DATABASE_NAME
        var dbase : SQLiteDatabase? =null
        try {
            dbase = SQLiteDatabase.openDatabase(dbLocation,null,SQLiteDatabase.OPEN_READWRITE)
        }
        catch (e : Exception){

        }
        if(dbase !=null){
            dbase.close()
            return true
        }
        return false
    }
    private fun copyDataBase(){
        val input = context.assets.open(DATABASE_NAME)
        val outFileName = PATH + DATABASE_NAME
        val output = FileOutputStream(outFileName)
        input.use { input ->output.use { fileOut ->
            input.copyTo(fileOut)
        } }
        input.close()
        output.flush()
        output.close()
    }
    companion object {
        private val DATABASE_VERSION=1
        private val DATABASE_NAME="BrickList.db"
        private val PATH ="/data/data/com.example.mlyna.bricklist/databases/";
    }
    fun addToInventory(project: Project) : Boolean{
        val values = ContentValues()
        values.put("_id",getInventory().size +1 )
        values.put("Name",project.name)
        values.put("LastAccessed",project.lastAccessed)
        var active =0
        if(project.archivized==false)
            active=1
        values.put("Active",active)
        try {
            this.writableDatabase.insert("Inventories", null, values)
            Log.i("tagdb", "Added new")
        }
        catch (e :Exception){
            return false
        }
        return true
    }
    fun isItemInInventory(item : Item, inventoryID : Int) : Boolean{
        val query ="SELECT * FROM INVENTORIESPARTS where InventoryID =\"$inventoryID \" and ItemID =\"${item.id}\""
        val cursor= this.readableDatabase.rawQuery(query,null)
        if (cursor.count>0)
            return true
        else
            return false
    }
    fun addItemIoInventory(item : Item, inventoryID : Int){
        if(isItemInInventory(item,inventoryID)==true)
            return;
        val query ="SELECT * FROM INVENTORIESPARTS;"
        val cursor= this.readableDatabase.rawQuery(query,null)
        val itemId = cursor.count + 1
        val values = ContentValues()
        values.put("_id",itemId)
        values.put("InventoryID",inventoryID)
        values.put("ItemID",item.id)
        values.put("TypeID",item.type)
        values.put("QuantityInSet",item.quantity)
        values.put("QuantityInStore",item.found)
        values.put("ColorID",item.color)
        this.writableDatabase.insert("INVENTORIESPARTS",null,values)
    }
    fun getByCode(code : String,table : String, what : Int) : String{
        val query ="SELECT * FROM $table where Code = \"$code\""
        val cursor= this.readableDatabase.rawQuery(query,null)
        if(cursor?.moveToFirst()==true){
            return cursor.getString(what)
        }
        return ""
    }
    fun getById(id : Int,table : String, what : Int) : String{
        val query ="SELECT * FROM $table where _id = \"$id\""
        val cursor= this.readableDatabase.rawQuery(query,null)
        if(cursor?.moveToFirst()==true){
            return cursor.getString(what)
        }
        return ""
    }
    fun changeActiveState(projectId : Int,active  : Boolean){
        var activeInt =1
        if(active==false) {
            activeInt = 0
        }
        val values = ContentValues()
        values.put("Active",activeInt)
        this.writableDatabase.update("INVENTORIES",values,"_id=$projectId",null)
    }
    fun deleteInventory(projectId : Int){
        this.writableDatabase.delete("INVENTORIES","_id=$projectId",null)
    }
    fun getFromPartsByID(id : Int,what :Int ) : String{
        val query ="SELECT * FROM PARTS where _id = \"$id\""
        val cursor= this.readableDatabase.rawQuery(query,null)
        if(cursor?.moveToFirst()==true){
            return cursor.getString(what)
        }
        return ""
    }
    fun itemFound(inventoryID: Int,itemID : Int,colorId : Int, value : Int){
        val values = ContentValues()
        values.put("QuantityInStore",value)
        this.writableDatabase.update("INVENTORIESPARTS",values,"InventoryID=$inventoryID and ItemID = $itemID and ColorID =$colorId",null)
    }
    fun getItemsInInventory(inventoryID: Int) : MutableList<Item>{
        val itemList = mutableListOf<Item>()
        val query ="SELECT * FROM INVENTORIESPARTS where InventoryID =\"$inventoryID \""
        val cursor= this.readableDatabase.rawQuery(query,null)
        while(cursor!=null && cursor?.moveToNext()==true){
            val typeID = Integer.parseInt(cursor.getString(2))
            val itemID = Integer.parseInt(cursor.getString(3))
            val quantity = Integer.parseInt(cursor.getString(4))
            val found = Integer.parseInt(cursor.getString(5))
            val itemColor = Integer.parseInt(cursor.getString(6))
            val name = getFromPartsByID(itemID,3)
            val code =  getFromPartsByID(itemID,2)
            val item =Item(itemID,itemColor,quantity,typeID,code,name)
            item.found =found
            itemList.add(item)
        }
        return itemList
    }
    fun getInventory() : MutableList<Project>{
        val projectList = mutableListOf<Project>()
        val query ="SELECT * FROM INVENTORIES;"
        val cursor= this.readableDatabase.rawQuery(query,null)
        Log.i("tagdb", cursor?.isClosed.toString())
        while(cursor!=null && cursor?.moveToNext()==true){
            val number = Integer.parseInt(cursor.getString(0))
            val name =cursor.getString(1)
            var active =false
            if(Integer.parseInt(cursor.getString(2))==0){
                active = true
            }
            val lastModified = Integer.parseInt(cursor.getString(3))
            val pr = Project(name,number,active,lastModified)
            projectList.add(pr)
        }

        return projectList
    }
}