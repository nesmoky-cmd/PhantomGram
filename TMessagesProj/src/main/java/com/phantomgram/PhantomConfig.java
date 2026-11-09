package com.phantomgram;

import android.content.Context;
import android.content.SharedPreferences;
import org.telegram.messenger.ApplicationLoader;

/**
 * PhantomGram Central Configuration & Feature Toggles
 */
public class PhantomConfig {
    private static final String PREFS_NAME = "phantomgram_prefs";
    private static SharedPreferences preferences;
    private static boolean initialized = false;

    // --- GHOST MODE (Призрак) ---
    public static boolean ghostModeEnabled = true;          // Главный переключатель Ghost Mode
    public static boolean sendReadPackets = false;           // Отправлять ли отметку о прочтении сообщений
    public static boolean sendOnlinePackets = false;         // Отправлять ли статус "В сети"
    public static boolean sendTypingPackets = false;         // Отправлять ли статус "Печатает..."
    public static boolean sendUploadProgress = false;       // Отправлять ли статус загрузки медиа
    public static boolean stealthStories = true;             // Скрытый просмотр историй (Stories)
    public static boolean markReadOnReply = true;            // Отмечать прочитанным только при отправке ответа

    // --- ANTI-DELETE & VAULT (Сохранение удаленных и изменений) ---
    public static boolean saveDeletedMessages = true;        // Сохранять удаленные сообщения
    public static boolean saveEditedHistory = true;          // Вести историю изменений сообщений
    public static boolean saveDisappearingMedia = true;      // Обход самоуничтожающихся медиа (view-once)
    public static boolean allowScreenshotsEverywhere = true; // Снятие запрета на скриншоты (FLAG_SECURE)
    public static String deletedMessageBadge = " [Удалено]"; // Метка удаленного сообщения
    public static String editedMessageBadge = " [ред.]";    // Метка отредактированного сообщения

    // --- SPY & ONLINE CORRELATION TRACKER ---
    public static boolean onlineTrackerEnabled = true;       // Точный логгер входов/выходов пользователей
    public static boolean correlationEngineEnabled = true;   // Детектор совпадений онлайна (с кем общается)
    public static int correlationThresholdSeconds = 8;       // Порог совпадения входа/выхода в секундах

    // --- iOS STYLE & SMOOTH ANIMATIONS ---
    public static boolean iosStyleTheme = true;              // iOS тема оформления (цвета, пузыри сообщений)
    public static boolean iosSpringAnimations = true;        // Плавные пружинные анимации 120 FPS
    public static boolean iosSwipeBack = true;               // Физический свайп "Назад" как на iOS
    public static boolean iosBlurEffects = true;             // Настоящее размытие (Glass / Blur) шторки и заголовков
    public static boolean iosHapticFeedback = true;          // Мягкий Haptic-отклик при взаимодействиях
    public static boolean iosBottomNav = true;               // Нижняя навигационная панель (Bottom Bar)

    public static void init() {
        if (initialized) return;
        Context context = ApplicationLoader.applicationContext;
        if (context == null) return;

        preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        load();
        initialized = true;
    }

    public static void load() {
        if (preferences == null) return;
        ghostModeEnabled = preferences.getBoolean("ghostModeEnabled", true);
        sendReadPackets = preferences.getBoolean("sendReadPackets", false);
        sendOnlinePackets = preferences.getBoolean("sendOnlinePackets", false);
        sendTypingPackets = preferences.getBoolean("sendTypingPackets", false);
        sendUploadProgress = preferences.getBoolean("sendUploadProgress", false);
        stealthStories = preferences.getBoolean("stealthStories", true);
        markReadOnReply = preferences.getBoolean("markReadOnReply", true);

        saveDeletedMessages = preferences.getBoolean("saveDeletedMessages", true);
        saveEditedHistory = preferences.getBoolean("saveEditedHistory", true);
        saveDisappearingMedia = preferences.getBoolean("saveDisappearingMedia", true);
        allowScreenshotsEverywhere = preferences.getBoolean("allowScreenshotsEverywhere", true);

        onlineTrackerEnabled = preferences.getBoolean("onlineTrackerEnabled", true);
        correlationEngineEnabled = preferences.getBoolean("correlationEngineEnabled", true);
        correlationThresholdSeconds = preferences.getInt("correlationThresholdSeconds", 8);

        iosStyleTheme = preferences.getBoolean("iosStyleTheme", true);
        iosSpringAnimations = preferences.getBoolean("iosSpringAnimations", true);
        iosSwipeBack = preferences.getBoolean("iosSwipeBack", true);
        iosBlurEffects = preferences.getBoolean("iosBlurEffects", true);
        iosHapticFeedback = preferences.getBoolean("iosHapticFeedback", true);
        iosBottomNav = preferences.getBoolean("iosBottomNav", true);
    }

    public static void save() {
        if (preferences == null) return;
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("ghostModeEnabled", ghostModeEnabled);
        editor.putBoolean("sendReadPackets", sendReadPackets);
        editor.putBoolean("sendOnlinePackets", sendOnlinePackets);
        editor.putBoolean("sendTypingPackets", sendTypingPackets);
        editor.putBoolean("sendUploadProgress", sendUploadProgress);
        editor.putBoolean("stealthStories", stealthStories);
        editor.putBoolean("markReadOnReply", markReadOnReply);

        editor.putBoolean("saveDeletedMessages", saveDeletedMessages);
        editor.putBoolean("saveEditedHistory", saveEditedHistory);
        editor.putBoolean("saveDisappearingMedia", saveDisappearingMedia);
        editor.putBoolean("allowScreenshotsEverywhere", allowScreenshotsEverywhere);

        editor.putBoolean("onlineTrackerEnabled", onlineTrackerEnabled);
        editor.putBoolean("correlationEngineEnabled", correlationEngineEnabled);
        editor.putInt("correlationThresholdSeconds", correlationThresholdSeconds);

        editor.putBoolean("iosStyleTheme", iosStyleTheme);
        editor.putBoolean("iosSpringAnimations", iosSpringAnimations);
        editor.putBoolean("iosSwipeBack", iosSwipeBack);
        editor.putBoolean("iosBlurEffects", iosBlurEffects);
        editor.putBoolean("iosHapticFeedback", iosHapticFeedback);
        editor.putBoolean("iosBottomNav", iosBottomNav);
        editor.apply();
    }

    public static boolean isGhostActive() {
        return ghostModeEnabled;
    }
}
