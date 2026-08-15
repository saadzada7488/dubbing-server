package com.savebabypanda.game.ads

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.gms.ads.*
import com.google.android.gms.ads.rewarded.RewardItem
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.savebabypanda.game.PandaApplication

/**
 * Manages AdMob advertisements with frequency control and proper lifecycle handling
 */
class AdManager(private val context: Context) {
    
    private var interstitialAd: InterstitialAd? = null
    private var rewardedAd: RewardedAd? = null
    
    private var interstitialLoadTime: Long = 0
    private var levelsSinceLastInterstitial = 0
    
    private val isAdAvailable: Boolean
        get() = isNetworkAvailable()
    
    companion object {
        private const val TAG = "AdManager"
        
        // Test Ad IDs - Replace with production IDs before release
        private const val TEST_INTERSTITIAL_ID = "ca-app-pub-3940256099942544/1033173712"
        private const val TEST_REWARDED_ID = "ca-app-pub-3940256099942544/5224354917"
        private const val TEST_BANNER_ID = "ca-app-pub-3940256099942544/6300978111"
        
        // Production Ad IDs - Replace with your actual AdMob IDs
        private const val PRODUCTION_INTERSTITIAL_ID = "YOUR_PRODUCTION_INTERSTITIAL_ID"
        private const val PRODUCTION_REWARDED_ID = "YOUR_PRODUCTION_REWARDED_ID"
        private const val PRODUCTION_BANNER_ID = "YOUR_PRODUCTION_BANNER_ID"
        
        // Frequency settings
        private const val INTERSTITIAL_FREQUENCY = 3 // Show after every 3 levels
        private const val MIN_TIME_BETWEEN_INTERSTITIALS = 60_000L // 1 minute
    }
    
    /**
     * Initialize AdMob
     */
    fun initialize() {
        if (!isAdAvailable) return
        
        MobileAds.initialize(context) { initializationStatus ->
            Log.d(TAG, "AdMob initialized: ${initializationStatus.adapterStatusMap.size} adapters")
        }
        
        loadInterstitial()
        loadRewardedAd()
    }
    
    /**
     * Load an interstitial ad
     */
    fun loadInterstitial() {
        if (!isAdAvailable) return
        
        val adId = getAdId(false)
        
        InterstitialAd.load(
            context,
            adId,
            AdRequest.Builder().build(),
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    interstitialAd = ad
                    interstitialLoadTime = System.currentTimeMillis()
                    Log.d(TAG, "Interstitial ad loaded")
                    
                    ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                        override fun onAdDismissedFullScreenContent() {
                            interstitialAd = null
                            loadInterstitial() // Load next ad
                        }
                        
                        override fun onAdFailedToShowFullScreenContent(error: AdError) {
                            Log.e(TAG, "Failed to show interstitial: ${error.message}")
                            interstitialAd = null
                            loadInterstitial()
                        }
                    }
                }
                
                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    Log.e(TAG, "Failed to load interstitial: ${loadAdError.message}")
                    interstitialAd = null
                }
            }
        )
    }
    
    /**
     * Load a rewarded ad
     */
    fun loadRewardedAd() {
        if (!isAdAvailable) return
        
        val adId = getAdId(true)
        
        RewardedAd.load(
            context,
            adId,
            AdRequest.Builder().build(),
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedAd) {
                    rewardedAd = ad
                    Log.d(TAG, "Rewarded ad loaded")
                    
                    ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                        override fun onAdDismissedFullScreenContent() {
                            rewardedAd = null
                            loadRewardedAd() // Load next ad
                        }
                        
                        override fun onAdFailedToShowFullScreenContent(error: AdError) {
                            Log.e(TAG, "Failed to show rewarded ad: ${error.message}")
                            rewardedAd = null
                            loadRewardedAd()
                        }
                    }
                }
                
                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    Log.e(TAG, "Failed to load rewarded ad: ${loadAdError.message}")
                    rewardedAd = null
                }
            }
        )
    }
    
    /**
     * Show interstitial ad if conditions are met
     */
    fun showInterstitial(activity: Activity?, force: Boolean = false) {
        if (activity == null || activity.isFinishing) return
        if (!force && !shouldShowInterstitial()) return
        if (interstitialAd == null) return
        
        try {
            interstitialAd?.show(activity)
            levelsSinceLastInterstitial = 0
            
            // Track for analytics if implemented
            logAdEvent("interstitial_shown")
        } catch (e: Exception) {
            Log.e(TAG, "Error showing interstitial: ${e.message}")
        }
    }
    
    /**
     * Show rewarded ad with callback
     */
    fun showRewardedAd(
        activity: Activity?,
        onRewardEarned: () -> Unit,
        onAdClosed: () -> Unit
    ) {
        if (activity == null || activity.isFinishing) {
            onAdClosed()
            return
        }
        
        if (rewardedAd == null) {
            Log.d(TAG, "Rewarded ad not ready")
            onAdClosed()
            return
        }
        
        try {
            rewardedAd?.show(activity) { rewardItem: RewardItem ->
                // User earned the reward
                Log.d(TAG, "Reward earned: ${rewardItem.amount} ${rewardItem.type}")
                onRewardEarned()
                logAdEvent("rewarded_ad_completed")
            }
            
            // Reset ad reference
            rewardedAd = null
            loadRewardedAd()
        } catch (e: Exception) {
            Log.e(TAG, "Error showing rewarded ad: ${e.message}")
            onAdClosed()
        }
    }
    
    /**
     * Check if we should show an interstitial based on frequency
     */
    private fun shouldShowInterstitial(): Boolean {
        val timeSinceLastLoad = System.currentTimeMillis() - interstitialLoadTime
        
        return levelsSinceLastInterstitial >= INTERSTITIAL_FREQUENCY &&
               timeSinceLastLoad >= MIN_TIME_BETWEEN_INTERSTITIALS
    }
    
    /**
     * Increment level counter for interstitial frequency
     */
    fun onLevelCompleted() {
        levelsSinceLastInterstitial++
    }
    
    /**
     * Get appropriate ad ID (test or production)
     */
    private fun getAdId(isRewarded: Boolean): String {
        // In debug builds, always use test IDs
        return if (BuildConfig.DEBUG) {
            if (isRewarded) TEST_REWARDED_ID else TEST_INTERSTITIAL_ID
        } else {
            if (isRewarded) PRODUCTION_REWARDED_ID else PRODUCTION_INTERSTITIAL_ID
        }
    }
    
    /**
     * Get banner ad ID
     */
    fun getBannerAdId(): String {
        return if (BuildConfig.DEBUG) {
            TEST_BANNER_ID
        } else {
            PRODUCTION_BANNER_ID
        }
    }
    
    /**
     * Check if network is available
     */
    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? android.net.ConnectivityManager
        return connectivityManager?.activeNetworkInfo?.isConnected == true
    }
    
    /**
     * Log ad event for analytics
     */
    private fun logAdEvent(eventType: String) {
        // Implement analytics logging here if needed
        Log.d(TAG, "Ad event: $eventType")
    }
    
    /**
     * Clean up resources
     */
    fun cleanup() {
        interstitialAd = null
        rewardedAd = null
    }
}
