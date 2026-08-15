package com.savebabypanda.game.engine

import android.graphics.Color
import com.savebabypanda.game.model.*
import kotlin.math.abs
import kotlin.random.Random

/**
 * Procedural level generator for 20,000 levels
 * Uses deterministic generation based on level ID as seed
 */
object LevelGenerator {
    
    private val puzzleTemplates = mapOf(
        PuzzleType.TAP_OBJECT to ::generateTapObjectLevel,
        PuzzleType.CUT_ROPE to ::generateCutRopeLevel,
        PuzzleType.MOVE_BLOCK to ::generateMoveBlockLevel,
        PuzzleType.OPEN_GATE to ::generateOpenGateLevel,
        PuzzleType.FIND_SWITCH to ::generateFindSwitchLevel,
        PuzzleType.MATCH_COLORS to ::generateMatchColorsLevel,
        PuzzleType.FOLLOW_SEQUENCE to ::generateFollowSequenceLevel,
        PuzzleType.MEMORY_PATTERN to ::generateMemoryPatternLevel,
        PuzzleType.ROTATE_OBJECTS to ::generateRotateObjectsLevel,
        PuzzleType.DRAG_KEY_TO_LOCK to ::generateKeyToLockLevel,
        PuzzleType.TIMING_PUZZLE to ::generateTimingPuzzleLevel,
        PuzzleType.LIGHT_PUZZLE to ::generateLightPuzzleLevel,
        PuzzleType.WATER_FLOW to ::generateWaterFlowLevel,
        PuzzleType.ICE_PUZZLE to ::generateIcePuzzleLevel,
        PuzzleType.GRAVITY_PUZZLE to ::generateGravityPuzzleLevel,
        PuzzleType.CONVEYOR_PUZZLE to ::generateConveyorPuzzleLevel,
        PuzzleType.GEAR_PUZZLE to ::generateGearPuzzleLevel,
        PuzzleType.PHYSICS_PUZZLE to ::generatePhysicsPuzzleLevel,
        PuzzleType.HIDDEN_OBJECT to ::generateHiddenObjectLevel
    )
    
    /**
     * Generate a level based on level ID
     */
    fun generateLevel(levelId: Int): Level {
        // Use level ID as seed for deterministic generation
        val random = Random(levelId.toLong())
        
        // Get world for this level
        val world = World.getWorldForLevel(levelId)
        
        // Calculate difficulty (1-100)
        val difficulty = calculateDifficulty(levelId)
        
        // Select puzzle type based on world and difficulty
        val puzzleType = selectPuzzleType(world, difficulty, random)
        
        // Generate the level
        val generator = puzzleTemplates[puzzleType] ?: puzzleTemplates[PuzzleType.TAP_OBJECT]!!
        val level = generator(levelId, world, difficulty, random)
        
        // Validate the level
        if (!validateLevel(level)) {
            // Regenerate with different seed if invalid
            return generateLevel(levelId + 1000)
        }
        
        return level
    }
    
    private fun calculateDifficulty(levelId: Int): Int {
        // Difficulty increases gradually from 1 to 100
        val baseDifficulty = (levelId.toFloat() / 20000f * 95f).toInt() + 5
        
        // Add some variation but keep overall progression
        val variation = (levelId % 100) % 10 - 5
        
        return (baseDifficulty + variation).coerceIn(1, 100)
    }
    
    private fun selectPuzzleType(world: World, difficulty: Int, random: Random): PuzzleType {
        val availableTypes = world.mechanics
        
        // For higher difficulties, use more complex puzzle types
        return when {
            difficulty < 20 -> availableTypes.firstOrNull() ?: PuzzleType.TAP_OBJECT
            difficulty < 40 -> availableTypes[random.nextInt(availableTypes.size)]
            difficulty < 60 -> availableTypes.filter { it != PuzzleType.TAP_OBJECT }
                .randomOrNull(random) ?: availableTypes.first()
            else -> availableTypes.random(random)
        }
    }
    
    private fun validateLevel(level: Level): Boolean {
        // Check that panda exists
        val hasPanda = level.objects.any { it.type == ObjectType.PANDA }
        if (!hasPanda) return false
        
        // Check that objects are within screen bounds
        for (obj in level.objects) {
            if (obj.x < 0 || obj.y < 0 || obj.x + obj.width > 100 || obj.y + obj.height > 100) {
                return false
            }
        }
        
        // Check that solution exists and is valid
        if (level.solution.isEmpty()) return false
        
        return true
    }
    
    // Level Generation Functions
    
    private fun generateTapObjectLevel(
        levelId: Int,
        world: World,
        difficulty: Int,
        random: Random
    ): Level {
        val objects = mutableListOf<LevelObject>()
        val solution = mutableListOf<Int>()
        
        // Add panda
        val pandaId = 1
        objects.add(LevelObject(
            id = pandaId,
            type = ObjectType.PANDA,
            x = 40f + random.nextFloat() * 20f,
            y = 60f + random.nextFloat() * 20f,
            width = 15f,
            height = 15f
        ))
        
        // Add obstacle(s) based on difficulty
        val numObstacles = 1 + (difficulty / 10)
        for (i in 0 until numObstacles) {
            val objectId = pandaId + i + 1
            objects.add(LevelObject(
                id = objectId,
                type = if (i == 0) ObjectType.BUTTON else ObjectType.DECORATION,
                x = random.nextFloat() * 70f,
                y = random.nextFloat() * 50f,
                width = 10f + random.nextFloat() * 10f,
                height = 10f + random.nextFloat() * 10f
            ))
            
            if (i == 0) {
                solution.add(objectId)
            }
        }
        
        return Level(
            id = levelId,
            worldId = world.id,
            difficulty = difficulty,
            puzzleType = PuzzleType.TAP_OBJECT,
            objects = objects,
            solution = solution,
            timeTarget = 30_000L + (difficulty * 500L),
            starRequirements = StarRequirements(
                perfectMoves = 1,
                goodMoves = 3,
                timeBonus = 10_000L
            ),
            rewardCoins = 10 + difficulty
        )
    }
    
    private fun generateCutRopeLevel(
        levelId: Int,
        world: World,
        difficulty: Int,
        random: Random
    ): Level {
        val objects = mutableListOf<LevelObject>()
        val solution = mutableListOf<Int>()
        
        // Add panda at bottom
        objects.add(LevelObject(
            id = 1,
            type = ObjectType.PANDA,
            x = 45f,
            y = 75f,
            width = 12f,
            height = 12f
        ))
        
        // Add rope(s) to cut
        val numRopes = 1 + (difficulty / 15)
        for (i in 0 until numRopes) {
            val ropeId = 2 + i
            objects.add(LevelObject(
                id = ropeId,
                type = ObjectType.ROPE,
                x = 30f + random.nextFloat() * 40f,
                y = 20f + random.nextFloat() * 30f,
                width = 8f,
                height = 25f
            ))
            
            if (i == 0) {
                solution.add(ropeId)
            }
        }
        
        // Add decoy ropes for higher difficulty
        if (difficulty > 30) {
            for (i in 0 until (difficulty / 20)) {
                val decoyId = 2 + numRopes + i
                objects.add(LevelObject(
                    id = decoyId,
                    type = ObjectType.ROPE,
                    x = random.nextFloat() * 80f,
                    y = random.nextFloat() * 40f,
                    width = 6f,
                    height = 20f,
                    properties = mapOf("isDecoy" to true)
                ))
            }
        }
        
        return Level(
            id = levelId,
            worldId = world.id,
            difficulty = difficulty,
            puzzleType = PuzzleType.CUT_ROPE,
            objects = objects,
            solution = solution,
            timeTarget = 40_000L + (difficulty * 600L),
            starRequirements = StarRequirements(
                perfectMoves = numRopes,
                goodMoves = numRopes + 2,
                timeBonus = 15_000L
            ),
            rewardCoins = 15 + difficulty
        )
    }
    
    private fun generateMoveBlockLevel(
        levelId: Int,
        world: World,
        difficulty: Int,
        random: Random
    ): Level {
        val objects = mutableListOf<LevelObject>()
        val solution = mutableListOf<Int>()
        
        // Add panda
        objects.add(LevelObject(
            id = 1,
            type = ObjectType.PANDA,
            x = 20f,
            y = 50f,
            width = 12f,
            height = 12f
        ))
        
        // Add blocks to move
        val numBlocks = 1 + (difficulty / 12)
        for (i in 0 until numBlocks) {
            val blockId = 2 + i
            objects.add(LevelObject(
                id = blockId,
                type = ObjectType.BLOCK,
                x = 40f + i * 15f,
                y = 45f + random.nextFloat() * 10f,
                width = 12f,
                height = 12f
            ))
            solution.add(blockId)
        }
        
        // Add goal platform
        objects.add(LevelObject(
            id = 2 + numBlocks,
            type = ObjectType.PLATFORM,
            x = 70f,
            y = 50f,
            width = 20f,
            height = 8f,
            isInteractive = false
        ))
        
        return Level(
            id = levelId,
            worldId = world.id,
            difficulty = difficulty,
            puzzleType = PuzzleType.MOVE_BLOCK,
            objects = objects,
            solution = solution,
            timeTarget = 45_000L + (difficulty * 700L),
            starRequirements = StarRequirements(
                perfectMoves = numBlocks,
                goodMoves = numBlocks + 3,
                timeBonus = 20_000L
            ),
            rewardCoins = 15 + difficulty
        )
    }
    
    private fun generateOpenGateLevel(
        levelId: Int, world: World, difficulty: Int, random: Random
    ): Level = generateBasicPuzzle(levelId, world, difficulty, random, PuzzleType.OPEN_GATE, ObjectType.GATE)
    
    private fun generateFindSwitchLevel(
        levelId: Int, world: World, difficulty: Int, random: Random
    ): Level = generateBasicPuzzle(levelId, world, difficulty, random, PuzzleType.FIND_SWITCH, ObjectType.SWITCH)
    
    private fun generateMatchColorsLevel(
        levelId: Int, world: World, difficulty: Int, random: Random
    ): Level = generateBasicPuzzle(levelId, world, difficulty, random, PuzzleType.MATCH_COLORS, ObjectType.BUTTON)
    
    private fun generateFollowSequenceLevel(
        levelId: Int, world: World, difficulty: Int, random: Random
    ): Level = generateBasicPuzzle(levelId, world, difficulty, random, PuzzleType.FOLLOW_SEQUENCE, ObjectType.BUTTON)
    
    private fun generateMemoryPatternLevel(
        levelId: Int, world: World, difficulty: Int, random: Random
    ): Level = generateBasicPuzzle(levelId, world, difficulty, random, PuzzleType.MEMORY_PATTERN, ObjectType.BUTTON)
    
    private fun generateRotateObjectsLevel(
        levelId: Int, world: World, difficulty: Int, random: Random
    ): Level = generateBasicPuzzle(levelId, world, difficulty, random, PuzzleType.ROTATE_OBJECTS, ObjectType.GEAR)
    
    private fun generateKeyToLockLevel(
        levelId: Int, world: World, difficulty: Int, random: Random
    ): Level = generateBasicPuzzle(levelId, world, difficulty, random, PuzzleType.DRAG_KEY_TO_LOCK, ObjectType.KEY)
    
    private fun generateTimingPuzzleLevel(
        levelId: Int, world: World, difficulty: Int, random: Random
    ): Level = generateBasicPuzzle(levelId, world, difficulty, random, PuzzleType.TIMING_PUZZLE, ObjectType.BUTTON)
    
    private fun generateLightPuzzleLevel(
        levelId: Int, world: World, difficulty: Int, random: Random
    ): Level = generateBasicPuzzle(levelId, world, difficulty, random, PuzzleType.LIGHT_PUZZLE, ObjectType.SWITCH)
    
    private fun generateWaterFlowLevel(
        levelId: Int, world: World, difficulty: Int, random: Random
    ): Level = generateBasicPuzzle(levelId, world, difficulty, random, PuzzleType.WATER_FLOW, ObjectType.WATER)
    
    private fun generateIcePuzzleLevel(
        levelId: Int, world: World, difficulty: Int, random: Random
    ): Level = generateBasicPuzzle(levelId, world, difficulty, random, PuzzleType.ICE_PUZZLE, ObjectType.ICE)
    
    private fun generateGravityPuzzleLevel(
        levelId: Int, world: World, difficulty: Int, random: Random
    ): Level = generateBasicPuzzle(levelId, world, difficulty, random, PuzzleType.GRAVITY_PUZZLE, ObjectType.PLATFORM)
    
    private fun generateConveyorPuzzleLevel(
        levelId: Int, world: World, difficulty: Int, random: Random
    ): Level = generateBasicPuzzle(levelId, world, difficulty, random, PuzzleType.CONVEYOR_PUZZLE, ObjectType.CONVEYOR)
    
    private fun generateGearPuzzleLevel(
        levelId: Int, world: World, difficulty: Int, random: Random
    ): Level = generateBasicPuzzle(levelId, world, difficulty, random, PuzzleType.GEAR_PUZZLE, ObjectType.GEAR)
    
    private fun generatePhysicsPuzzleLevel(
        levelId: Int, world: World, difficulty: Int, random: Random
    ): Level = generateBasicPuzzle(levelId, world, difficulty, random, PuzzleType.PHYSICS_PUZZLE, ObjectType.BLOCK)
    
    private fun generateHiddenObjectLevel(
        levelId: Int, world: World, difficulty: Int, random: Random
    ): Level = generateBasicPuzzle(levelId, world, difficulty, random, PuzzleType.HIDDEN_OBJECT, ObjectType.DECORATION)
    
    private fun generateBasicPuzzle(
        levelId: Int,
        world: World,
        difficulty: Int,
        random: Random,
        puzzleType: PuzzleType,
        mainObjectType: ObjectType
    ): Level {
        val objects = mutableListOf<LevelObject>()
        val solution = mutableListOf<Int>()
        
        // Add panda
        objects.add(LevelObject(
            id = 1,
            type = ObjectType.PANDA,
            x = 40f + random.nextFloat() * 20f,
            y = 60f + random.nextFloat() * 20f,
            width = 12f,
            height = 12f
        ))
        
        // Add interactive objects
        val numObjects = 2 + (difficulty / 8)
        for (i in 0 until numObjects) {
            val objectId = 2 + i
            objects.add(LevelObject(
                id = objectId,
                type = if (i == 0) mainObjectType else ObjectType.DECORATION,
                x = random.nextFloat() * 70f,
                y = random.nextFloat() * 50f,
                width = 10f + random.nextFloat() * 8f,
                height = 10f + random.nextFloat() * 8f
            ))
            
            if (i == 0) {
                solution.add(objectId)
            }
        }
        
        return Level(
            id = levelId,
            worldId = world.id,
            difficulty = difficulty,
            puzzleType = puzzleType,
            objects = objects,
            solution = solution,
            timeTarget = 35_000L + (difficulty * 600L),
            starRequirements = StarRequirements(
                perfectMoves = 1,
                goodMoves = 3,
                timeBonus = 15_000L
            ),
            rewardCoins = 12 + difficulty
        )
    }
}
