package com.savebabypanda.game.ui

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.savebabypanda.game.R
import com.savebabypanda.game.data.GameDataManager
import com.savebabypanda.game.databinding.ActivityMainBinding
import com.savebabypanda.game.engine.LevelGenerator
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Main Activity - Entry point of the game
 * Handles navigation between home, level select, and gameplay
 */
class MainActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityMainBinding
    private lateinit var dataManager: GameDataManager
    
    // Current screen state
    private var currentScreen: Screen = Screen.HOME
    
    enum class Screen {
        HOME,
        LEVEL_SELECT,
        GAMEPLAY,
        SHOP,
        SETTINGS,
        DAILY_CHALLENGE
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // Get data manager from application
        dataManager = (application as com.savebabypanda.game.PandaApplication).dataManager
        
        // Initialize AdMob
        (application as com.savebabypanda.game.PandaApplication).adManager.initialize()
        
        setupUI()
        showHomeScreen()
    }
    
    private fun setupUI() {
        // Setup click listeners for home screen buttons
        binding.btnPlay.setOnClickListener { navigateToLevelSelect() }
        binding.btnDailyChallenge.setOnClickListener { showDailyChallenge() }
        binding.btnShop.setOnClickListener { showShop() }
        binding.btnPanda.setOnClickListener { showPandaCollection() }
        binding.btnAchievements.setOnClickListener { showAchievements() }
        binding.btnSettings.setOnClickListener { showSettings() }
        
        // Update stats display
        updateStatsDisplay()
    }
    
    private fun updateStatsDisplay() {
        lifecycleScope.launch {
            while (true) {
                runOnUiThread {
                    binding.tvCoins.text = "${dataManager.coins}"
                    binding.tvStars.text = "${dataManager.totalStars}"
                    
                    val world = com.savebabypanda.game.model.World.getWorldForLevel(dataManager.currentLevel)
                    binding.tvWorldInfo.text = "${getString(R.string.world)} ${world.id}\n${getString(R.string.level)} ${dataManager.currentLevel} / 1000"
                }
                delay(500)
            }
        }
    }
    
    private fun showHomeScreen() {
        currentScreen = Screen.HOME
        
        binding.homeContainer.visibility = View.VISIBLE
        binding.levelSelectContainer.visibility = View.GONE
        binding.gameView.visibility = View.GONE
        binding.shopContainer.visibility = View.GONE
        binding.settingsContainer.visibility = View.GONE
        
        updateStatsDisplay()
    }
    
    private fun navigateToLevelSelect() {
        currentScreen = Screen.LEVEL_SELECT
        
        binding.homeContainer.visibility = View.GONE
        binding.levelSelectContainer.visibility = View.VISIBLE
        
        loadWorldMap()
    }
    
    private fun loadWorldMap() {
        // Display worlds and levels
        // This would normally use a RecyclerView with proper adapter
        // For now, we'll show a simple representation
        
        val unlockedWorlds = dataManager.unlockedWorlds
        
        binding.tvWorldInfo.text = "${getString(R.string.world)} $unlockedWorlds / 20"
        
        // Navigate to first available level
        binding.btnStartLevel.setOnClickListener {
            startLevel(dataManager.currentLevel)
        }
    }
    
    private fun startLevel(levelId: Int) {
        if (levelId > 20000) {
            Toast.makeText(this, "Congratulations! You've completed all levels!", Toast.LENGTH_LONG).show()
            return
        }
        
        currentScreen = Screen.GAMEPLAY
        
        binding.homeContainer.visibility = View.GONE
        binding.levelSelectContainer.visibility = View.GONE
        binding.gameView.visibility = View.VISIBLE
        
        // Generate and load the level
        val level = LevelGenerator.generateLevel(levelId)
        
        // Setup game view with the level
        binding.gameView.loadLevel(level, object : GameView.GameCallback {
            override fun onLevelComplete(stars: Int, coins: Int) {
                handleLevelComplete(levelId, stars, coins)
            }
            
            override fun onLevelFailed() {
                handleLevelFailed()
            }
            
            override fun onBackPressed() {
                showHomeScreen()
            }
        })
    }
    
    private fun handleLevelComplete(levelId: Int, stars: Int, coins: Int) {
        // Save progress
        dataManager.completeLevel(levelId, stars, coins)
        
        // Notify ad manager
        (application as com.savebabypanda.game.PandaApplication).adManager.onLevelCompleted()
        
        // Show level complete dialog
        showLevelCompleteDialog(levelId, stars, coins)
    }
    
    private fun handleLevelFailed() {
        // Show try again dialog
        showLevelFailedDialog()
    }
    
    private fun showLevelCompleteDialog(levelId: Int, stars: Int, coins: Int) {
        // In a real implementation, this would show a dialog
        Toast.makeText(this, "Level Complete! Stars: $stars, Coins: $coins", Toast.LENGTH_LONG).show()
        
        // Option to show rewarded ad for bonus coins
        // AdManager.showRewardedAd(...)
    }
    
    private fun showLevelFailedDialog() {
        Toast.makeText(this, "Try Again!", Toast.LENGTH_SHORT).show()
    }
    
    private fun showDailyChallenge() {
        Toast.makeText(this, "Daily Challenge coming soon!", Toast.LENGTH_SHORT).show()
    }
    
    private fun showShop() {
        currentScreen = Screen.SHOP
        Toast.makeText(this, "Shop coming soon!", Toast.LENGTH_SHORT).show()
    }
    
    private fun showPandaCollection() {
        Toast.makeText(this, "Panda Collection coming soon!", Toast.LENGTH_SHORT).show()
    }
    
    private fun showAchievements() {
        Toast.makeText(this, "Achievements coming soon!", Toast.LENGTH_SHORT).show()
    }
    
    private fun showSettings() {
        currentScreen = Screen.SETTINGS
        binding.homeContainer.visibility = View.GONE
        binding.settingsContainer.visibility = View.VISIBLE
        
        // Setup settings toggles
        binding.switchMusic.isChecked = dataManager.musicEnabled
        binding.switchSound.isChecked = dataManager.soundEnabled
        binding.switchVibration.isChecked = dataManager.vibrationEnabled
        
        binding.switchMusic.setOnCheckedChangeListener { _, isChecked ->
            dataManager.musicEnabled = isChecked
        }
        
        binding.switchSound.setOnCheckedChangeListener { _, isChecked ->
            dataManager.soundEnabled = isChecked
        }
        
        binding.switchVibration.setOnCheckedChangeListener { _, isChecked ->
            dataManager.vibrationEnabled = isChecked
        }
        
        binding.btnBackSettings.setOnClickListener {
            showHomeScreen()
        }
        
        binding.btnResetProgress.setOnClickListener {
            // Show confirmation dialog
            android.app.AlertDialog.Builder(this)
                .setMessage(R.string.confirm_reset)
                .setPositiveButton(R.string.yes) { _, _ ->
                    dataManager.resetProgress()
                    Toast.makeText(this, "Progress reset!", Toast.LENGTH_SHORT).show()
                    showHomeScreen()
                }
                .setNegativeButton(R.string.no, null)
                .show()
        }
    }
    
    override fun onBackPressed() {
        when (currentScreen) {
            Screen.HOME -> {
                // Exit app or show exit dialog
                android.app.AlertDialog.Builder(this)
                    .setTitle("Exit Game")
                    .setMessage("Are you sure you want to exit?")
                    .setPositiveButton("YES") { _, _ -> finish() }
                    .setNegativeButton("NO", null)
                    .show()
            }
            Screen.LEVEL_SELECT, Screen.SHOP, Screen.SETTINGS -> showHomeScreen()
            Screen.GAMEPLAY -> {
                // Pause game and show pause menu
                showHomeScreen()
            }
            else -> super.onBackPressed()
        }
    }
    
    override fun onPause() {
        super.onPause()
        // Save progress
        dataManager.saveProgress()
    }
    
    override fun onResume() {
        super.onResume()
        updateStatsDisplay()
    }
}
