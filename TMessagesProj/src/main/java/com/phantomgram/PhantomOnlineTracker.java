package com.phantomgram;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.FileLog;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * PhantomOnlineTracker - Spy Online Logger & Pair Correlation Engine
 * Tracks exact online/offline timestamps down to the second and detects
 * simultaneous online activity between contacts to identify conversation partners.
 */
public class PhantomOnlineTracker {

    private static final String DB_NAME = "phantom_tracker.db";
    private static final int DB_VERSION = 1;

    private static PhantomOnlineTracker instance;
    private final DatabaseHelper dbHelper;
    private SQLiteDatabase db;

    // Cache: userId -> last known online unix timestamp in seconds
    private final ConcurrentHashMap<Long, Integer> lastSeenOnline = new ConcurrentHashMap<>();
    // Cache: userId -> current session start time (or 0 if offline)
    private final ConcurrentHashMap<Long, Integer> activeSessions = new ConcurrentHashMap<>();

    public static synchronized PhantomOnlineTracker getInstance() {
        if (instance == null) {
            instance = new PhantomOnlineTracker();
        }
        return instance;
    }

    private PhantomOnlineTracker() {
        Context context = ApplicationLoader.applicationContext;
        dbHelper = new DatabaseHelper(context);
        try {
            db = dbHelper.getWritableDatabase();
        } catch (Exception e) {
            FileLog.e(e);
        }
    }

    /**
     * Called whenever an MTProto userStatus update arrives.
     */
    public void onUserStatusChange(long userId, boolean isOnline, int timestamp) {
        if (!PhantomConfig.onlineTrackerEnabled) return;

        if (isOnline) {
            lastSeenOnline.put(userId, timestamp);
            activeSessions.put(userId, timestamp);
            logEvent(userId, "ONLINE", timestamp);
        } else {
            Integer start = activeSessions.remove(userId);
            int duration = (start != null && timestamp > start) ? (timestamp - start) : 0;
            logEvent(userId, "OFFLINE", timestamp);
            if (duration > 0) {
                recordSession(userId, start != null ? start : timestamp, timestamp, duration);
            }
        }
    }

    /**
     * Get precise last seen online timestamp, bypassing "last seen recently".
     */
    public int getExactLastSeen(long userId) {
        Integer cached = lastSeenOnline.get(userId);
        if (cached != null) return cached;

        if (db == null) return 0;
        try (Cursor c = db.rawQuery(
                "SELECT timestamp FROM online_events WHERE user_id = ? ORDER BY timestamp DESC LIMIT 1",
                new String[]{String.valueOf(userId)})) {
            if (c.moveToFirst()) {
                int ts = c.getInt(0);
                lastSeenOnline.put(userId, ts);
                return ts;
            }
        } catch (Exception e) {
            FileLog.e(e);
        }
        return 0;
    }

    private void logEvent(long userId, String type, int timestamp) {
        if (db == null) return;
        try {
            ContentValues cv = new ContentValues();
            cv.put("user_id", userId);
            cv.put("event_type", type);
            cv.put("timestamp", timestamp);
            db.insert("online_events", null, cv);
        } catch (Exception e) {
            FileLog.e(e);
        }
    }

    private void recordSession(long userId, int start, int end, int duration) {
        if (db == null) return;
        try {
            ContentValues cv = new ContentValues();
            cv.put("user_id", userId);
            cv.put("start_time", start);
            cv.put("end_time", end);
            cv.put("duration", duration);
            db.insert("online_sessions", null, cv);
        } catch (Exception e) {
            FileLog.e(e);
        }
    }

    /**
     * Correlation Engine: Calculates whether User A and User B are entering/leaving
     * Telegram simultaneously (within threshold seconds).
     * @return Score from 0 to 100% representing communication likelihood.
     */
    public CorrelationResult calculateCorrelation(long userA, long userB) {
        if (db == null) return new CorrelationResult(0, 0, 0);

        int threshold = PhantomConfig.correlationThresholdSeconds;
        int matches = 0;
        int totalA = 0;

        try {
            List<Integer> eventsA = getUserEvents(userA);
            List<Integer> eventsB = getUserEvents(userB);

            totalA = eventsA.size();
            if (totalA == 0 || eventsB.isEmpty()) {
                return new CorrelationResult(0, 0, totalA);
            }

            for (int tsA : eventsA) {
                for (int tsB : eventsB) {
                    if (Math.abs(tsA - tsB) <= threshold) {
                        matches++;
                        break;
                    }
                }
            }

            int score = (int) Math.min(100, Math.round(((double) matches / totalA) * 100));
            return new CorrelationResult(score, matches, totalA);

        } catch (Exception e) {
            FileLog.e(e);
            return new CorrelationResult(0, 0, 0);
        }
    }

    private List<Integer> getUserEvents(long userId) {
        List<Integer> list = new ArrayList<>();
        try (Cursor c = db.rawQuery(
                "SELECT timestamp FROM online_events WHERE user_id = ? ORDER BY timestamp DESC LIMIT 200",
                new String[]{String.valueOf(userId)})) {
            while (c.moveToNext()) {
                list.add(c.getInt(0));
            }
        }
        return list;
    }

    public static class CorrelationResult {
        public final int percentage;
        public final int matchedEvents;
        public final int totalEvents;

        public CorrelationResult(int percentage, int matchedEvents, int totalEvents) {
            this.percentage = percentage;
            this.matchedEvents = matchedEvents;
            this.totalEvents = totalEvents;
        }

        @Override
        public String toString() {
            return percentage + "% (совпадений: " + matchedEvents + "/" + totalEvents + ")";
        }
    }

    private static class DatabaseHelper extends SQLiteOpenHelper {
        public DatabaseHelper(Context context) {
            super(context, DB_NAME, null, DB_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL("CREATE TABLE IF NOT EXISTS online_events (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "user_id INTEGER, " +
                    "event_type TEXT, " +
                    "timestamp INTEGER)");

            db.execSQL("CREATE INDEX IF NOT EXISTS idx_user_events ON online_events (user_id, timestamp)");

            db.execSQL("CREATE TABLE IF NOT EXISTS online_sessions (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "user_id INTEGER, " +
                    "start_time INTEGER, " +
                    "end_time INTEGER, " +
                    "duration INTEGER)");

            db.execSQL("CREATE INDEX IF NOT EXISTS idx_user_sessions ON online_sessions (user_id)");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        }
    }
}
