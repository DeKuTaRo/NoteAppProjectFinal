package com.example.noteappproject.RoomDatabase;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.noteappproject.Models.NoteItem;

@Database(entities = NoteItem.class, version = 5, exportSchema = false)
public abstract class RoomDB extends RoomDatabase {

    private static RoomDB database;
    private static final String DATABASE_NAME = "NoteAppProject";

    public synchronized static RoomDB getInstance(Context context) {

        if (database == null) {
            database = Room.databaseBuilder(context.getApplicationContext(),
                    RoomDB.class, DATABASE_NAME)
                    .allowMainThreadQueries()
                    .fallbackToDestructiveMigration()
                    .openHelperFactory(null)
                    .build();
        }
        return database;
    }

    public abstract NoteDAO noteDAO();

}
