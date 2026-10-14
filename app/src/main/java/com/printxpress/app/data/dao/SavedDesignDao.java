package com.printxpress.app.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.printxpress.app.data.model.SavedDesignEntity;

import java.util.List;

@Dao
public interface SavedDesignDao {
    @Insert
    long insert(SavedDesignEntity design);

    @Query("DELETE FROM saved_designs WHERE id = :id")
    void delete(long id);

    @Query("SELECT * FROM saved_designs WHERE userId = :userId ORDER BY createdAt DESC")
    LiveData<List<SavedDesignEntity>> observeForUser(long userId);
}
