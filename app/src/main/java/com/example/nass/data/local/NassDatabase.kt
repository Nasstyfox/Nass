package com.example.nass.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.nass.data.model.CartItem
import com.example.nass.data.model.Product

@Database(entities = [Product::class, CartItem::class], version = 1, exportSchema = false)
abstract class NassDatabase : RoomDatabase() {
    abstract fun nassDao(): NassDao

    companion object {
        @Volatile
        private var INSTANCE: NassDatabase? = null

        fun getDatabase(context: Context): NassDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    NassDatabase::class.java,
                    "nass_database"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}