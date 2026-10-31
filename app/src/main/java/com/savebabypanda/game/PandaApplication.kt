package com.savebabypanda.game

import android.app.Application
import com.savebabypanda.game.data.GameDataManager
import com.savebabypanda.game.ads.AdManager

class PandaApplication : Application() {
    
    companion object {
        lateinit var instance: PandaApplication
            private set
    }
    
    lateinit var dataManager: GameDataManager
        private set
    
    lateinit var adManager: AdManager
        private set
    
    override fun onCreate() {
        super.onCreate()
        instance = this
        
        // Initialize managers
        dataManager = GameDataManager(this)
        adManager = AdManager(this)
        
        // Load saved data
        dataManager.loadProgress()
    }
    
    override fun onTerminate() {
        super.onTerminate()
        dataManager.saveProgress()
    }
}
