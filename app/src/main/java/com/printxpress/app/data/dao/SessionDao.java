package com.printxpress.app.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.printxpress.app.data.model.SessionEntity;

@Dao
public interface SessionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void set(SessionEntity session);

    @Query("SELECT * FROM session WHERE id = 1 LIMIT 1")
    LiveData<SessionEntity> observe();

    @Query("SELECT * FROM session WHERE id = 1 LIMIT 1")
    SessionEntity get();

    @Query("DELETE FROM session")
    void clear();
}
