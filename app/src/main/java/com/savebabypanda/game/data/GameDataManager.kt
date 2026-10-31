package com.savebabypanda.game.data

import android.content.Context
import android.content.SharedPreferences
import com.savebabypanda.game.model.Level
import com.savebabypanda.game.model.World
import com.savebabypanda.game.engine.LevelGenerator
import org.json.JSONArray
import org.json.JSONObject

/**
 * Manages all game data including progress, coins, stars, and settings
 */
class GameDataManager(private val context: Context) {
    
    private val prefs: SharedPreferences = 
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    
    // Player Progress
    var currentLevel: Int
        get() = prefs.getInt(KEY_CURRENT_LEVEL, 1)
        set(value) = prefs.edit().putInt(KEY_CURRENT_LEVEL, value).apply()
    
    var completedLevels: Set<Int>
        get() = prefs.getStringSet(KEY_COMPLETED_LEVELS, emptySet())?.map { it.toInt() }?.toSet() ?: emptySet()
        private set(value) = prefs.edit().putStringSet(KEY_COMPLETED_LEVELS, value.map { it.toString() }.toSet()).apply()
    
    var levelStars: Map<Int, Int>
        get() = loadLevelStars()
        private set(value) = saveLevelStars(value)
    
    // Currency
    var coins: Int
        get() = prefs.getInt(KEY_COINS, 0)
        set(value) = prefs.edit().putInt(KEY_COINS, value).apply()
    
    var totalStars: Int
        get() = prefs.getInt(KEY_TOTAL_STARS, 0)
        set(value) = prefs.edit().putInt(KEY_TOTAL_STARS, value).apply()
    
    // Unlocks
    var unlockedSkins: Set<String>
        get() = prefs.getStringSet(KEY_UNLOCKED_SKINS, setOf(SKIN_DEFAULT))?.toSet() ?: setOf(SKIN_DEFAULT)
        private set(value) = prefs.edit().putStringSet(KEY_UNLOCKED_SKINS, value).apply()
    
    var equippedSkin: String
        get() = prefs.getString(KEY_EQUIPPED_SKIN, SKIN_DEFAULT) ?: SKIN_DEFAULT
        set(value) = prefs.edit().putString(KEY_EQUIPPED_SKIN, value).apply()
    
    var unlockedWorlds: Int
        get() = prefs.getInt(KEY_UNLOCKED_WORLDS, 1)
        set(value) = prefs.edit().putInt(KEY_UNLOCKED_WORLDS, value).apply()
    
    // Daily Reward
    var lastDailyReward: Long
        get() = prefs.getLong(KEY_LAST_DAILY_REWARD, 0L)
        set(value) = prefs.edit().putLong(KEY_LAST_DAILY_REWARD, value).apply()
    
    var dailyRewardStreak: Int
        get() = prefs.getInt(KEY_DAILY_STREAK, 0)
        set(value) = prefs.edit().putInt(KEY_DAILY_STREAK, value).apply()
    
    // Settings
    var musicEnabled: Boolean
        get() = prefs.getBoolean(KEY_MUSIC_ENABLED, true)
        set(value) = prefs.edit().putBoolean(KEY_MUSIC_ENABLED, value).apply()
    
    var soundEnabled: Boolean
        get() = prefs.getBoolean(KEY_SOUND_ENABLED, true)
        set(value) = prefs.edit().putBoolean(KEY_SOUND_ENABLED, value).apply()
    
    var vibrationEnabled: Boolean
        get() = prefs.getBoolean(KEY_VIBRATION_ENABLED, true)
        set(value) = prefs.edit().putBoolean(KEY_VIBRATION_ENABLED, value).apply()
    
    var masterVolume: Float
        get() = prefs.getFloat(KEY_MASTER_VOLUME, 1.0f)
        set(value) = prefs.edit().putFloat(KEY_MASTER_VOLUME, value).apply()
    
    var musicVolume: Float
        get() = prefs.getFloat(KEY_MUSIC_VOLUME, 0.7f)
        set(value) = prefs.edit().putFloat(KEY_MUSIC_VOLUME, value).apply()
    
    var sfxVolume: Float
        get() = prefs.getFloat(KEY_SFX_VOLUME, 0.8f)
        set(value) = prefs.edit().putFloat(KEY_SFX_VOLUME, value).apply()
    
    // Statistics
    var totalLevelsPlayed: Int
        get() = prefs.getInt(KEY_TOTAL_PLAYED, 0)
        set(value) = prefs.edit().putInt(KEY_TOTAL_PLAYED, value).apply()
    
    var totalLevelsCompleted: Int
        get() = prefs.getInt(KEY_TOTAL_COMPLETED, 0)
        set(value) = prefs.edit().putInt(KEY_TOTAL_COMPLETED, value).apply()
    
    var hintsUsed: Int
        get() = prefs.getInt(KEY_HINTS_USED, 0)
        set(value) = prefs.edit().putInt(KEY_HINTS_USED, value).apply()
    
    var availableHints: Int
        get() = prefs.getInt(KEY_AVAILABLE_HINTS, 3)
        set(value) = prefs.edit().putInt(KEY_AVAILABLE_HINTS, value).apply()
    
    // Achievements
    var unlockedAchievements: Set<String>
        get() = prefs.getStringSet(KEY_ACHIEVEMENTS, emptySet())?.toSet() ?: emptySet()
        private set(value) = prefs.edit().putStringSet(KEY_ACHIEVEMENTS, value).apply()
    
    /**
     * Save current progress atomically
     */
    fun saveProgress() {
        try {
            prefs.edit().apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    /**
     * Load all saved progress
     */
    fun loadProgress() {
        // Data is loaded automatically via property getters
    }
    
    /**
     * Complete a level and update progress
     */
    fun completeLevel(levelId: Int, stars: Int, coinsEarned: Int) {
        val completed = completedLevels.toMutableSet()
        completed.add(levelId)
        
        // Update completed levels
        prefs.edit().apply {
            putStringSet(KEY_COMPLETED_LEVELS, completed.map { it.toString() }.toSet())
            
            // Update stars for this level
            val currentStars = loadLevelStars().toMutableMap()
            val existingStars = currentStars[levelId] ?: 0
            if (stars > existingStars) {
                currentStars[levelId] = stars
                putString(KEY_LEVEL_STARS, JSONObject(currentStars).toString())
            }
            
            // Add coins
            putInt(KEY_COINS, coins + coinsEarned)
            
            // Update total stars
            putInt(KEY_TOTAL_STARS, totalStars + stars)
            
            // Update statistics
            putInt(KEY_TOTAL_COMPLETED, totalLevelsCompleted + 1)
            
            // Unlock next level if this was the current level
            if (levelId == currentLevel && levelId < 20000) {
                putInt(KEY_CURRENT_LEVEL, levelId + 1)
            }
            
            // Unlock new world if needed
            val world = World.getWorldForLevel(levelId)
            if (world.id >= unlockedWorlds && world.id <= 20) {
                putInt(KEY_UNLOCKED_WORLDS, minOf(world.id + 1, 20))
            }
            
            apply()
        }
    }
    
    /**
     * Check if a level is unlocked
     */
    fun isLevelUnlocked(levelId: Int): Boolean {
        return levelId <= currentLevel || completedLevels.contains(levelId - 1)
    }
    
    /**
     * Get stars earned for a specific level
     */
    fun getLevelStars(levelId: Int): Int {
        return loadLevelStars()[levelId] ?: 0
    }
    
    /**
     * Check if a level is completed
     */
    fun isLevelCompleted(levelId: Int): Boolean {
        return completedLevels.contains(levelId)
    }
    
    /**
     * Use a hint
     */
    fun useHint(): Boolean {
        return if (availableHints > 0) {
            availableHints = availableHints - 1
            hintsUsed = hintsUsed + 1
            true
        } else {
            false
        }
    }
    
    /**
     * Add hints
     */
    fun addHints(count: Int) {
        availableHints = availableHints + count
    }
    
    /**
     * Unlock a skin
     */
    fun unlockSkin(skinId: String) {
        val skins = unlockedSkins.toMutableSet()
        skins.add(skinId)
        unlockedSkins = skins
    }
    
    /**
     * Check if skin is unlocked
     */
    fun isSkinUnlocked(skinId: String): Boolean {
        return unlockedSkins.contains(skinId)
    }
    
    /**
     * Unlock an achievement
     */
    fun unlockAchievement(achievementId: String): Boolean {
        return if (!unlockedAchievements.contains(achievementId)) {
            val achievements = unlockedAchievements.toMutableSet()
            achievements.add(achievementId)
            unlockedAchievements = achievements
            true
        } else {
            false
        }
    }
    
    /**
     * Generate a level using the procedural generator
     */
    fun generateLevel(levelId: Int): Level {
        return LevelGenerator.generateLevel(levelId)
    }
    
    /**
     * Reset all game progress
     */
    fun resetProgress() {
        prefs.edit().clear().apply()
        // Set defaults
        currentLevel = 1
        coins = 0
        totalStars = 0
        unlockedSkins = setOf(SKIN_DEFAULT)
        equippedSkin = SKIN_DEFAULT
        unlockedWorlds = 1
        availableHints = 3
        musicEnabled = true
        soundEnabled = true
        vibrationEnabled = true
    }
    
    private fun loadLevelStars(): Map<Int, Int> {
        return try {
            val jsonStr = prefs.getString(KEY_LEVEL_STARS, "{}") ?: "{}"
            val json = JSONObject(jsonStr)
            val result = mutableMapOf<Int, Int>()
            json.keys().forEach { key ->
                result[key.toInt()] = json.getInt(key)
            }
            result
        } catch (e: Exception) {
            emptyMap()
        }
    }
    
    private fun saveLevelStars(stars: Map<Int, Int>) {
        try {
            val json = JSONObject()
            stars.forEach { (level, starCount) ->
                json.put(level.toString(), starCount)
            }
            prefs.edit().putString(KEY_LEVEL_STARS, json.toString()).apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    companion object {
        private const val PREFS_NAME = "save_my_baby_panda_prefs"
        
        private const val KEY_CURRENT_LEVEL = "current_level"
        private const val KEY_COMPLETED_LEVELS = "completed_levels"
        private const val KEY_LEVEL_STARS = "level_stars"
        private const val KEY_COINS = "coins"
        private const val KEY_TOTAL_STARS = "total_stars"
        private const val KEY_UNLOCKED_SKINS = "unlocked_skins"
        private const val KEY_EQUIPPED_SKIN = "equipped_skin"
        private const val KEY_UNLOCKED_WORLDS = "unlocked_worlds"
        private const val KEY_LAST_DAILY_REWARD = "last_daily_reward"
        private const val KEY_DAILY_STREAK = "daily_streak"
        private const val KEY_MUSIC_ENABLED = "music_enabled"
        private const val KEY_SOUND_ENABLED = "sound_enabled"
        private const val KEY_VIBRATION_ENABLED = "vibration_enabled"
        private const val KEY_MASTER_VOLUME = "master_volume"
        private const val KEY_MUSIC_VOLUME = "music_volume"
        private const val KEY_SFX_VOLUME = "sfx_volume"
        private const val KEY_TOTAL_PLAYED = "total_played"
        private const val KEY_TOTAL_COMPLETED = "total_completed"
        private const val KEY_HINTS_USED = "hints_used"
        private const val KEY_AVAILABLE_HINTS = "available_hints"
        private const val KEY_ACHIEVEMENTS = "achievements"
        
        const val SKIN_DEFAULT = "default"
        const val SKIN_EXPLORER = "explorer"
        const val SKIN_RAINCOAT = "raincoat"
        const val SKIN_WINTER = "winter"
        const val SKIN_CHEF = "chef"
        const val SKIN_SPACE = "space"
        const val SKIN_PIRATE = "pirate"
        const val SKIN_GARDEN = "garden"
        const val SKIN_DETECTIVE = "detective"
        const val SKIN_SLEEPY = "sleepy"
        const val SKIN_SCIENTIST = "scientist"
        const val SKIN_TOY = "toy"
        const val SKIN_RAINBOW = "rainbow"
    }
}
