package com.example.mlyna.bricklist

import java.io.Serializable

/**
 * Created by mlyna on 17.05.2018.
 */

class Project(name: String,number : Int) {
    var name = name;
    var number = number;
    var archivized = false;
    var lastAccessed =1;
    constructor(name: String,number : Int,arch : Boolean, LA : Int) :this(name,number){
        archivized= arch
        lastAccessed=LA
    }
}