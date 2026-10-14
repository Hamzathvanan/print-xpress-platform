package com.printxpress.app.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.printxpress.app.data.model.NotificationEntity;

import java.util.List;

@Dao
public interface NotificationDao {
    @Insert
    long insert(NotificationEntity notification);

    @Insert
    void insertAll(List<NotificationEntity> notifications);

    @Query("SELECT * FROM notifications WHERE userId = :userId ORDER BY createdAt DESC")
    LiveData<List<NotificationEntity>> observeForUser(long userId);

    @Query("UPDATE notifications SET isRead = 1 WHERE userId = :userId")
    void markAllRead(long userId);

    @Query("SELECT COUNT(*) FROM notifications WHERE userId = :userId AND isRead = 0")
    LiveData<Integer> observeUnreadCount(long userId);
}
