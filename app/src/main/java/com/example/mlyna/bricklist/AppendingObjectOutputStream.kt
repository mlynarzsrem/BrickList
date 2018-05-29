package com.example.mlyna.bricklist

import java.io.ObjectOutputStream
import java.io.OutputStream

/**
 * Created by mlyna on 17.05.2018.
 */
class AppendingObjectOutputStream(out : OutputStream) :ObjectOutputStream(out){
    override fun writeStreamHeader() {
        reset()
    }
}