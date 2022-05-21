package com.example.noteappproject.RoomDatabase;


import static androidx.room.OnConflictStrategy.REPLACE;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.noteappproject.Models.NoteItem;

import java.util.List;


@Dao
public interface MainDAO {

    @Insert(onConflict = REPLACE)
    void insert(NoteItem noteItem);

    @Query("SELECT * FROM notes ORDER BY id DESC ")
    List<NoteItem> getAll();

    @Query("UPDATE notes SET label = :label, subtitle = :subtitle, text_content = :text_content, date = :date, color = :color, imagePath = :imagePath, webLink =:webLink WHERE ID = :id")
    void update(int id, String label, String subtitle, String text_content, String date, String color, String imagePath, String webLink);

    @Query("UPDATE notes SET passwordNote = :passwordNote WHERE ID = :id")
    void updatePasswordNote(int id, String passwordNote);

    @Delete
    void delete(NoteItem noteItem);

    @Query("UPDATE notes SET pinned = :pin WHERE ID = :id ")
    void pin(int id, boolean pin);
}
