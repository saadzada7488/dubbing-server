package com.savebabypanda.game.model

/**
 * Represents a single level in the game
 */
data class Level(
    val id: Int,
    val worldId: Int,
    val difficulty: Int,
    val puzzleType: PuzzleType,
    val objects: List<LevelObject>,
    val solution: List<Int>,
    val timeTarget: Long,
    val starRequirements: StarRequirements,
    val rewardCoins: Int
) {
    val worldName: String
        get() = World.getById(worldId).name
    
    fun getStars(moves: Int, timeTaken: Long): Int {
        return when {
            moves <= starRequirements.perfectMoves && timeTaken <= timeTarget -> 3
            moves <= starRequirements.goodMoves * 1.5 && timeTaken <= timeTarget * 1.5 -> 2
            else -> 1
        }
    }
}

/**
 * Different types of puzzles available in the game
 */
enum class PuzzleType {
    TAP_OBJECT,
    CUT_ROPE,
    MOVE_BLOCK,
    OPEN_GATE,
    FIND_SWITCH,
    MATCH_COLORS,
    MATCH_SYMBOLS,
    FOLLOW_SEQUENCE,
    MEMORY_PATTERN,
    ROTATE_OBJECTS,
    CONNECT_OBJECTS,
    DRAG_KEY_TO_LOCK,
    MOVE_BRIDGE,
    PUSH_OBJECT,
    PULL_OBJECT,
    BALANCE_OBJECTS,
    AVOID_OBSTACLE,
    TIMING_PUZZLE,
    LIGHT_PUZZLE,
    MIRROR_PUZZLE,
    WATER_FLOW,
    WIND_PUZZLE,
    ICE_PUZZLE,
    GRAVITY_PUZZLE,
    CONVEYOR_PUZZLE,
    GEAR_PUZZLE,
    COMBINATION_PUZZLE,
    MULTI_STAGE_RESCUE,
    PHYSICS_PUZZLE,
    HIDDEN_OBJECT
}

/**
 * An object in the level that player can interact with
 */
data class LevelObject(
    val id: Int,
    val type: ObjectType,
    val x: Float,
    val y: Float,
    val width: Float,
    val height: Float,
    val isInteractive: Boolean = true,
    val initialState: ObjectState = ObjectState.NORMAL,
    val properties: Map<String, Any> = emptyMap()
)

enum class ObjectType {
    PANDA,
    ROPE,
    BLOCK,
    GATE,
    SWITCH,
    KEY,
    LOCK,
    BRIDGE,
    OBSTACLE,
    PLATFORM,
    CONVEYOR,
    GEAR,
    WATER,
    WIND,
    ICE,
    MAGNET,
    SPRING,
    BUTTON,
    DECORATION
}

enum class ObjectState {
    NORMAL,
    ACTIVATED,
    DEACTIVATED,
    BROKEN,
    HIDDEN,
    MOVING_LEFT,
    MOVING_RIGHT,
    MOVING_UP,
    MOVING_DOWN,
    ROTATING_CLOCKWISE,
    ROTATING_COUNTER_CLOCKWISE
}

/**
 * Requirements for earning stars
 */
data class StarRequirements(
    val perfectMoves: Int,
    val goodMoves: Int,
    val timeBonus: Long
)

/**
 * Represents one of the 20 worlds in the game
 */
data class World(
    val id: Int,
    val name: String,
    val levels: IntRange,
    val backgroundColor: Int,
    val themeColor: Int,
    val mechanics: List<PuzzleType>
) {
    companion object {
        private val worlds = listOf(
            World(1, "Green Bamboo Forest", 1..1000, 0xFF4CAF50.toInt(), 0xFF8BC34A.toInt(), 
                listOf(PuzzleType.TAP_OBJECT, PuzzleType.CUT_ROPE, PuzzleType.MOVE_BLOCK)),
            World(2, "Sunny Meadow", 1001..2000, 0xFFFFEB3B.toInt(), 0xFFFF9800.toInt(),
                listOf(PuzzleType.TAP_OBJECT, PuzzleType.MOVE_BLOCK, PuzzleType.TIMING_PUZZLE)),
            World(3, "Mystery Cave", 2001..3000, 0xFF795548.toInt(), 0xFF5D4037.toInt(),
                listOf(PuzzleType.FIND_SWITCH, PuzzleType.MEMORY_PATTERN, PuzzleType.LIGHT_PUZZLE)),
            World(4, "Snowy Mountain", 3001..4000, 0xFF03A9F4.toInt(), 0xFF81D4FA.toInt(),
                listOf(PuzzleType.ICE_PUZZLE, PuzzleType.TIMING_PUZZLE, PuzzleType.SLIDE_PUZZLE)),
            World(5, "Beach Island", 4001..5000, 0xFFFF9800.toInt(), 0xFFFFCC80.toInt(),
                listOf(PuzzleType.WATER_FLOW, PuzzleType.TIMING_PUZZLE, PuzzleType.PHYSICS_PUZZLE)),
            World(6, "Bamboo Village", 5001..6000, 0xFF8BC34A.toInt(), 0xFFDCEDC8.toInt(),
                listOf(PuzzleType.COMBINATION_PUZZLE, PuzzleType.MULTI_STAGE_RESCUE)),
            World(7, "Jungle Temple", 6001..7000, 0xFF5D4037.toInt(), 0xFF8D6E63.toInt(),
                listOf(PuzzleType.MATCH_SYMBOLS, PuzzleType.FOLLOW_SEQUENCE, PuzzleType.FIND_SWITCH)),
            World(8, "Cloud Kingdom", 7001..8000, 0xFFE1BEE7.toInt(), 0xFFF3E5F5.toInt(),
                listOf(PuzzleType.GRAVITY_PUZZLE, PuzzleType.WIND_PUZZLE, PuzzleType.FLOATING_OBJECTS)),
            World(9, "Candy Valley", 8001..9000, 0xFFF06292.toInt(), 0xFFF48FB1.toInt(),
                listOf(PuzzleType.SPRING_PUZZLE, PuzzleType.CHAIN_REACTION, PuzzleType.BOUNCING)),
            World(10, "Robot Factory", 9001..10000, 0xFF9E9E9E.toInt(), 0xFFBDBDBD.toInt(),
                listOf(PuzzleType.CONVEYOR_PUZZLE, PuzzleType.GEAR_PUZZLE, PuzzleType.TIMING_PUZZLE)),
            World(11, "Desert Oasis", 10001..11000, 0xFFFFCA28.toInt(), 0xFFFFECB3.toInt(),
                listOf(PuzzleType.WATER_FLOW, PuzzleType.SAND_MOVEMENT, PuzzleType.HEAT_COOLING)),
            World(12, "Magic Garden", 11001..12000, 0xFFAB47BC.toInt(), 0xFFCE93D8.toInt(),
                listOf(PuzzleType.GROWING_PLANTS, PuzzleType.COLOR_COMBINATIONS, PuzzleType.MAGICAL_SWITCHES)),
            World(13, "Underwater World", 12001..13000, 0xFF0288D1.toInt(), 0xFF4FC3F7.toInt(),
                listOf(PuzzleType.BUBBLES, PuzzleType.WATER_CURRENTS, PuzzleType.FLOATING_OBJECTS)),
            World(14, "Sky Train", 13001..14000, 0xFF5C6BC0.toInt(), 0xFF7986CB.toInt(),
                listOf(PuzzleType.MOVING_TRAINS, PuzzleType.TRACK_SWITCHING, PuzzleType.TIMING_PUZZLE)),
            World(15, "Moon World", 14001..15000, 0xFF90A4AE.toInt(), 0xFFCFD8DC.toInt(),
                listOf(PuzzleType.LOW_GRAVITY, PuzzleType.FLOATING_OBJECTS, PuzzleType.SPACE_PUZZLE)),
            World(16, "Volcano Land", 15001..16000, 0xFFD84315.toInt(), 0xFFFF8A65.toInt(),
                listOf(PuzzleType.MOVING_PLATFORMS, PuzzleType.COOLING_ROCKS, PuzzleType.WATER_INTERACTIONS)),
            World(17, "Fairy Forest", 16001..17000, 0xFFEC407A.toInt(), 0xFFF48FB1.toInt(),
                listOf(PuzzleType.MAGICAL_LIGHTS, PuzzleType.INVISIBLE_PATHS, PuzzleType.PATTERN_PUZZLE)),
            World(18, "Giant Toy World", 17001..18000, 0xFFFF5722.toInt(), 0xFFFF8A65.toInt(),
                listOf(PuzzleType.TOY_MECHANISMS, PuzzleType.SPRINGS, PuzzleType.ROTATING_OBJECTS)),
            World(19, "Panda Dream World", 18001..19000, 0xFFBA68C8.toInt(), 0xFFE1BEE7.toInt(),
                listOf(PuzzleType.COMBINATION_PUZZLE, PuzzleType.MULTI_STAGE_RESCUE, PuzzleType.MASTER_CHALLENGE)),
            World(20, "Panda Master World", 19001..20000, 0xFF673AB7.toInt(), 0xFF9575CD.toInt(),
                listOf(PuzzleType.MASTER_CHALLENGE, PuzzleType.COMBINATION_PUZZLE, PuzzleType.ALL_MECHANICS))
        )
        
        fun getById(id: Int): World {
            return worlds.find { it.id == id } ?: worlds.first()
        }
        
        fun getAll(): List<World> = worlds
        
        fun getWorldForLevel(levelId: Int): World {
            return worlds.find { levelId in it.levels } ?: worlds.last()
        }
    }
}

// Add missing puzzle types
enum class PuzzleTypeExtended {
    SLIDE_PUZZLE,
    FLOATING_OBJECTS,
    CHAIN_REACTION,
    BOUNCING,
    SAND_MOVEMENT,
    HEAT_COOLING,
    GROWING_PLANTS,
    COLOR_COMBINATIONS,
    MAGICAL_SWITCHES,
    BUBBLES,
    WATER_CURRENTS,
    MOVING_TRAINS,
    TRACK_SWITCHING,
    LOW_GRAVITY,
    SPACE_PUZZLE,
    MOVING_PLATFORMS,
    COOLING_ROCKS,
    WATER_INTERACTIONS,
    MAGICAL_LIGHTS,
    INVISIBLE_PATHS,
    TOY_MECHANISMS,
    ALL_MECHANICS,
    MASTER_CHALLENGE
}
