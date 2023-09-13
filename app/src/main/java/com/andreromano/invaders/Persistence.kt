package com.andreromano.invaders

import android.annotation.SuppressLint
import android.content.Context
import com.andreromano.invaders.scenes.level.levelState
import com.andreromano.invaders.scenes.level.LevelState
import com.andreromano.invaders.scenes.level.SaveableLevelState
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream

@SuppressLint("StaticFieldLeak")
object Persistence {
    lateinit var context: Context

    private val file: File
        get() = File(context.filesDir, "savegame")

    fun save(saveableLevelState: SaveableLevelState) {
        val fout = FileOutputStream(file)
        val oos = ObjectOutputStream(fout)
        oos.writeObject(saveableLevelState)
    }

    fun load(): SaveableLevelState? {
        return try {
            val fin = FileInputStream(file)
            val ois = ObjectInputStream(fin)
            ois.readObject() as? SaveableLevelState
        } catch (ex: Exception) {
            ex.printStackTrace()
            null
        }
    }
}