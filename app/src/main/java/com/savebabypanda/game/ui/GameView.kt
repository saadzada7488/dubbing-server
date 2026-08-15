package com.savebabypanda.game.ui

import android.content.Context
import android.graphics.*
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import com.savebabypanda.game.model.Level
import com.savebabypanda.game.model.ObjectType
import kotlin.math.sqrt

/**
 * Custom Game View that renders and handles gameplay
 */
class GameView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {
    
    private var currentLevel: Level? = null
    private var callback: GameCallback? = null
    
    // Paint objects for rendering
    private val pandaPaint = Paint()
    private val objectPaint = Paint()
    private val textPaint = Paint()
    private val backgroundPaint = Paint()
    
    // Game state
    private var movesCount = 0
    private var levelStartTime = 0L
    private var isLevelComplete = false
    private var highlightedObjectId: Int? = null
    
    // Animation
    private val handler = Handler(Looper.getMainLooper())
    private var animationFrame = 0
    
    init {
        setupPaints()
    }
    
    private fun setupPaints() {
        // Panda paint (black and white)
        pandaPaint.color = Color.BLACK
        pandaPaint.isAntiAlias = true
        
        // Object paint
        objectPaint.isAntiAlias = true
        objectPaint.style = Paint.Style.FILL
        
        // Text paint
        textPaint.color = Color.WHITE
        textPaint.textSize = 48f
        textPaint.isFakeBoldText = true
        textPaint.textAlign = Paint.Align.CENTER
        
        // Background paint
        backgroundPaint.color = Color.parseColor("#F5F5DC")
    }
    
    /**
     * Load a level into the game view
     */
    fun loadLevel(level: Level, callback: GameCallback) {
        currentLevel = level
        this.callback = callback
        movesCount = 0
        levelStartTime = System.currentTimeMillis()
        isLevelComplete = false
        highlightedObjectId = null
        
        invalidate()
        
        // Show tutorial hint for first level
        if (level.id == 1) {
            showTutorialHint()
        }
    }
    
    private fun showTutorialHint() {
        Toast.makeText(context, "Tap the object to rescue Pip!", Toast.LENGTH_LONG).show()
    }
    
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        
        val level = currentLevel ?: return
        
        // Draw background
        canvas.drawPaint(backgroundPaint)
        
        // Draw world-themed background
        drawBackground(canvas, level)
        
        // Draw all objects
        for (obj in level.objects) {
            drawObject(canvas, obj)
        }
        
        // Draw UI elements
        drawUI(canvas, level)
        
        // Continue animation
        animationFrame++
        if (!isLevelComplete) {
            postInvalidateDelayed(16) // ~60 FPS
        }
    }
    
    private fun drawBackground(canvas: Canvas, level: Level) {
        val worldColor = when (level.worldId) {
            1 -> Color.parseColor("#4CAF50") // Forest
            2 -> Color.parseColor("#FFEB3B") // Meadow
            3 -> Color.parseColor("#795548") // Cave
            4 -> Color.parseColor("#03A9F4") // Snow
            5 -> Color.parseColor("#FF9800") // Beach
            else -> Color.parseColor("#F5F5DC")
        }
        
        // Draw sky/background gradient
        val gradient = LinearGradient(
            0f, 0f, 0f, height.toFloat(),
            worldColor,
            Color.WHITE,
            Shader.TileMode.CLAMP
        )
        backgroundPaint.shader = gradient
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), backgroundPaint)
        backgroundPaint.shader = null
    }
    
    private fun drawObject(canvas: Canvas, obj: com.savebabypanda.game.model.LevelObject) {
        val scaleX = width / 100f
        val scaleY = height / 100f
        
        val x = obj.x * scaleX
        val y = obj.y * scaleY
        val w = obj.width * scaleX
        val h = obj.height * scaleY
        
        when (obj.type) {
            ObjectType.PANDA -> drawPanda(canvas, x, y, w, h)
            ObjectType.ROPE -> drawRope(canvas, x, y, w, h)
            ObjectType.BLOCK -> drawBlock(canvas, x, y, w, h)
            ObjectType.BUTTON -> drawButton(canvas, x, y, w, h)
            ObjectType.GATE -> drawGate(canvas, x, y, w, h)
            ObjectType.SWITCH -> drawSwitch(canvas, x, y, w, h)
            ObjectType.KEY -> drawKey(canvas, x, y, w, h)
            ObjectType.DECORATION -> drawDecoration(canvas, x, y, w, h)
            else -> drawDefaultObject(canvas, x, y, w, h, obj.type)
        }
        
        // Draw highlight if this is the hinted object
        if (highlightedObjectId == obj.id) {
            objectPaint.color = Color.YELLOW
            objectPaint.alpha = 100
            canvas.drawCircle(x + w/2, y + h/2, maxOf(w, h), objectPaint)
            objectPaint.alpha = 255
        }
    }
    
    private fun drawPanda(canvas: Canvas, x: Float, y: Float, w: Float, h: Float) {
        // Body (white)
        objectPaint.color = Color.WHITE
        canvas.drawOval(x, y, x + w, y + h, objectPaint)
        
        // Ears (black)
        objectPaint.color = Color.BLACK
        val earSize = w * 0.3f
        canvas.drawCircle(x + w * 0.2f, y + h * 0.2f, earSize, objectPaint)
        canvas.drawCircle(x + w * 0.8f, y + h * 0.2f, earSize, objectPaint)
        
        // Eyes (black with white pupils)
        val eyeSize = w * 0.15f
        canvas.drawCircle(x + w * 0.35f, y + h * 0.4f, eyeSize, objectPaint)
        canvas.drawCircle(x + w * 0.65f, y + h * 0.4f, eyeSize, objectPaint)
        
        // Pupils (white)
        objectPaint.color = Color.WHITE
        canvas.drawCircle(x + w * 0.35f, y + h * 0.4f, eyeSize * 0.4f, objectPaint)
        canvas.drawCircle(x + w * 0.65f, y + h * 0.4f, eyeSize * 0.4f, objectPaint)
        
        // Nose (black)
        objectPaint.color = Color.BLACK
        canvas.drawCircle(x + w * 0.5f, y + h * 0.55f, w * 0.08f, objectPaint)
        
        // Happy mouth
        canvas.drawArc(
            x + w * 0.3f, y + h * 0.55f,
            x + w * 0.7f, y + h * 0.7f,
            180f, 180f, false, objectPaint
        )
    }
    
    private fun drawRope(canvas: Canvas, x: Float, y: Float, w: Float, h: Float) {
        objectPaint.color = Color.parseColor("#8D6E63")
        objectPaint.strokeWidth = 4f
        objectPaint.style = Paint.Style.STROKE
        
        // Draw rope as a line with some waviness
        val points = 10
        for (i in 0 until points) {
            val t = i.toFloat() / points
            val wave = Math.sin((animationFrame * 0.1 + i) * 0.5).toFloat() * 3f
            canvas.drawLine(
                x + w/2 + wave, y + i * (h / points),
                x + w/2 + wave, y + (i + 1) * (h / points),
                objectPaint
            )
        }
        
        objectPaint.style = Paint.Style.FILL
    }
    
    private fun drawBlock(canvas: Canvas, x: Float, y: Float, w: Float, h: Float) {
        objectPaint.color = Color.parseColor("#795548")
        canvas.drawRect(x, y, x + w, y + h, objectPaint)
        
        // Border
        objectPaint.color = Color.parseColor("#5D4037")
        objectPaint.style = Paint.Style.STROKE
        objectPaint.strokeWidth = 2f
        canvas.drawRect(x, y, x + w, y + h, objectPaint)
        objectPaint.style = Paint.Style.FILL
    }
    
    private fun drawButton(canvas: Canvas, x: Float, y: Float, w: Float, h: Float) {
        // Button base
        objectPaint.color = Color.parseColor("#2196F3")
        canvas.drawCircle(x + w/2, y + h/2, minOf(w, h) * 0.4f, objectPaint)
        
        // Shine effect
        objectPaint.color = Color.WHITE
        objectPaint.alpha = 150
        canvas.drawCircle(x + w/2 - w*0.1f, y + h/2 - h*0.1f, minOf(w, h) * 0.15f, objectPaint)
        objectPaint.alpha = 255
    }
    
    private fun drawGate(canvas: Canvas, x: Float, y: Float, w: Float, h: Float) {
        objectPaint.color = Color.parseColor("#5D4037")
        
        // Gate bars
        val barWidth = w * 0.1f
        val numBars = 5
        for (i in 0 until numBars) {
            val barX = x + i * (w / numBars)
            canvas.drawRect(barX, y, barX + barWidth, y + h, objectPaint)
        }
    }
    
    private fun drawSwitch(canvas: Canvas, x: Float, y: Float, w: Float, h: Float) {
        objectPaint.color = Color.parseColor("#4CAF50")
        canvas.drawRoundRect(x, y, x + w, y + h, 8f, 8f, objectPaint)
        
        // Switch indicator
        objectPaint.color = Color.WHITE
        canvas.drawCircle(x + w/2, y + h/2, minOf(w, h) * 0.2f, objectPaint)
    }
    
    private fun drawKey(canvas: Canvas, x: Float, y: Float, w: Float, h: Float) {
        objectPaint.color = Color.parseColor("#FFD700")
        
        // Key head
        canvas.drawCircle(x + w/2, y + h * 0.3f, w * 0.3f, objectPaint)
        
        // Key shaft
        canvas.drawRect(x + w/2 - w*0.1f, y + h * 0.3f, x + w/2 + w*0.1f, y + h * 0.8f, objectPaint)
        
        // Key teeth
        canvas.drawRect(x + w/2, y + h * 0.6f, x + w * 0.8f, y + h * 0.7f, objectPaint)
    }
    
    private fun drawDecoration(canvas: Canvas, x: Float, y: Float, w: Float, h: Float) {
        objectPaint.color = Color.parseColor("#8BC34A")
        objectPaint.alpha = 180
        canvas.drawCircle(x + w/2, y + h/2, minOf(w, h) * 0.4f, objectPaint)
        objectPaint.alpha = 255
    }
    
    private fun drawDefaultObject(canvas: Canvas, x: Float, y: Float, w: Float, h: Float, type: ObjectType) {
        objectPaint.color = Color.GRAY
        canvas.drawRect(x, y, x + w, y + h, objectPaint)
    }
    
    private fun drawUI(canvas: Canvas, level: Level) {
        // Draw level info at top
        val infoBg = Paint()
        infoBg.color = Color.parseColor("#AA000000")
        canvas.drawRoundRect(16f, 16f, width - 16f, 80f, 16f, 16f, infoBg)
        
        textPaint.color = Color.WHITE
        canvas.drawText("Level ${level.id}", width / 2f, 50f, textPaint)
        
        // Draw hint button at bottom right
        val hintBtnPaint = Paint()
        hintBtnPaint.color = Color.parseColor("#FF9800")
        hintBtnPaint.isAntiAlias = true
        val hintBtnRadius = 40f
        canvas.drawCircle(width - hintBtnRadius - 16f, height - hintBtnRadius - 16f, hintBtnRadius, hintBtnPaint)
        
        textPaint.textSize = 30f
        textPaint.color = Color.WHITE
        canvas.drawText("?", width - hintBtnRadius - 16f, height - hintBtnRadius - 16f + 10f, textPaint)
    }
    
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (isLevelComplete) return true
        
        val level = currentLevel ?: return super.onTouchEvent(event)
        
        if (event.action == MotionEvent.ACTION_DOWN) {
            val touchX = event.x
            val touchY = event.y
            
            // Check if touched any interactive object
            for (obj in level.objects) {
                if (obj.isInteractive && isTouchingObject(touchX, touchY, obj)) {
                    handleObjectTap(obj)
                    return true
                }
            }
            
            // Check if hint button tapped
            val hintBtnX = width - 56f
            val hintBtnY = height - 56f
            val dist = sqrt(((touchX - hintBtnX) * (touchX - hintBtnX) + (touchY - hintBtnY) * (touchY - hintBtnY)).toDouble()).toFloat()
            if (dist < 40f) {
                useHint()
                return true
            }
        }
        
        return super.onTouchEvent(event)
    }
    
    private fun isTouchingObject(touchX: Float, touchY: Float, obj: com.savebabypanda.game.model.LevelObject): Boolean {
        val scaleX = width / 100f
        val scaleY = height / 100f
        
        val x = obj.x * scaleX
        val y = obj.y * scaleY
        val w = obj.width * scaleX
        val h = obj.height * scaleY
        
        return touchX >= x - 20 && touchX <= x + w + 20 &&
               touchY >= y - 20 && touchY <= y + h + 20
    }
    
    private fun handleObjectTap(obj: com.savebabypanda.game.model.LevelObject) {
        movesCount++
        
        // Check if this is the correct object
        val level = currentLevel ?: return
        
        if (obj.id in level.solution) {
            // Correct! Complete the level
            completeLevel()
        } else {
            // Wrong object - show feedback
            showWrongFeedback()
        }
    }
    
    private fun showWrongFeedback() {
        // Vibrate if enabled
        // In real implementation, check settings
        performHapticFeedback(HAPTIC_FEEDBACK_REJECT)
    }
    
    private fun completeLevel() {
        isLevelComplete = true
        
        val timeTaken = System.currentTimeMillis() - levelStartTime
        val stars = currentLevel?.getStars(movesCount, timeTaken) ?: 1
        val coins = currentLevel?.rewardCoins ?: 10
        
        callback?.onLevelComplete(stars, coins)
    }
    
    private fun useHint() {
        val level = currentLevel ?: return
        
        // Find first solution object that hasn't been completed
        val solutionObj = level.solution.firstOrNull()
        if (solutionObj != null) {
            highlightedObjectId = solutionObj
            invalidate()
            
            // Remove highlight after 2 seconds
            handler.postDelayed({
                highlightedObjectId = null
                invalidate()
            }, 2000)
        }
    }
    
    interface GameCallback {
        fun onLevelComplete(stars: Int, coins: Int)
        fun onLevelFailed()
        fun onBackPressed()
    }
}
