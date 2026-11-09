package com.phantomgram;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.FileLog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * PhantomVault - Anti-Delete & Message History Management
 * Keeps track of deleted messages and multiple edits locally.
 */
public class PhantomVault {

    private static final String DB_NAME = "phantom_vault.db";
    private static final int DB_VERSION = 1;

    private static PhantomVault instance;
    private final DatabaseHelper dbHelper;
    private SQLiteDatabase db;

    // Fast in-memory cache for O(1) checks during UI rendering: "dialogId_messageId"
    private final Set<String> deletedCache = Collections.newSetFromMap(new ConcurrentHashMap<>());
    private final ConcurrentHashMap<String, Integer> deleteTimestamps = new ConcurrentHashMap<>();

    public static synchronized PhantomVault getInstance() {
        if (instance == null) {
            instance = new PhantomVault();
        }
        return instance;
    }

    private PhantomVault() {
        Context context = ApplicationLoader.applicationContext;
        dbHelper = new DatabaseHelper(context);
        try {
            db = dbHelper.getWritableDatabase();
            loadCache();
        } catch (Exception e) {
            FileLog.e(e);
        }
    }

    private void loadCache() {
        if (db == null) return;
        try (Cursor cursor = db.rawQuery("SELECT dialog_id, message_id, delete_time FROM deleted_messages", null)) {
            while (cursor.moveToNext()) {
                long dialogId = cursor.getLong(0);
                int messageId = cursor.getInt(1);
                int time = cursor.getInt(2);
                String key = dialogId + "_" + messageId;
                deletedCache.add(key);
                deleteTimestamps.put(key, time);
            }
        } catch (Exception e) {
            FileLog.e(e);
        }
    }

    /**
     * Mark a message as deleted locally instead of purging it.
     */
    public void markDeleted(long dialogId, int messageId, int deleteTimestamp) {
        if (!PhantomConfig.saveDeletedMessages) return;

        String key = dialogId + "_" + messageId;
        deletedCache.add(key);
        deleteTimestamps.put(key, deleteTimestamp);

        if (db == null) return;
        try {
            ContentValues cv = new ContentValues();
            cv.put("dialog_id", dialogId);
            cv.put("message_id", messageId);
            cv.put("delete_time", deleteTimestamp);
            db.insertWithOnConflict("deleted_messages", null, cv, SQLiteDatabase.CONFLICT_REPLACE);
        } catch (Exception e) {
            FileLog.e(e);
        }
    }

    /**
     * Check if a message was deleted by peer.
     */
    public boolean isDeleted(long dialogId, int messageId) {
        if (!PhantomConfig.saveDeletedMessages) return false;
        return deletedCache.contains(dialogId + "_" + messageId);
    }

    public int getDeleteTime(long dialogId, int messageId) {
        Integer time = deleteTimestamps.get(dialogId + "_" + messageId);
        return time != null ? time : 0;
    }

    /**
     * Save an edited message version to history.
     */
    public void saveEditVersion(long dialogId, int messageId, String oldText, int editTime) {
        if (!PhantomConfig.saveEditedHistory || oldText == null || oldText.isEmpty()) return;
        if (db == null) return;

        try {
            ContentValues cv = new ContentValues();
            cv.put("dialog_id", dialogId);
            cv.put("message_id", messageId);
            cv.put("text", oldText);
            cv.put("edit_time", editTime);
            db.insert("edited_messages", null, cv);
        } catch (Exception e) {
            FileLog.e(e);
        }
    }

    /**
     * Retrieve all previous versions of an edited message.
     */
    public List<String> getEditHistory(long dialogId, int messageId) {
        List<String> list = new ArrayList<>();
        if (db == null) return list;

        try (Cursor cursor = db.rawQuery(
                "SELECT text, edit_time FROM edited_messages WHERE dialog_id = ? AND message_id = ? ORDER BY edit_time ASC",
                new String[]{String.valueOf(dialogId), String.valueOf(messageId)})) {
            while (cursor.moveToNext()) {
                list.add(cursor.getString(0));
            }
        } catch (Exception e) {
            FileLog.e(e);
        }
        return list;
    }

    private static class DatabaseHelper extends SQLiteOpenHelper {
        public DatabaseHelper(Context context) {
            super(context, DB_NAME, null, DB_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL("CREATE TABLE IF NOT EXISTS deleted_messages (" +
                    "dialog_id INTEGER, " +
                    "message_id INTEGER, " +
                    "delete_time INTEGER, " +
                    "PRIMARY KEY (dialog_id, message_id))");

            db.execSQL("CREATE TABLE IF NOT EXISTS edited_messages (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "dialog_id INTEGER, " +
                    "message_id INTEGER, " +
                    "text TEXT, " +
                    "edit_time INTEGER)");

            db.execSQL("CREATE INDEX IF NOT EXISTS idx_edits ON edited_messages (dialog_id, message_id)");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        }
    }
}
