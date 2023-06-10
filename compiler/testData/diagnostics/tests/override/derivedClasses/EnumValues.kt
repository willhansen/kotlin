enum class Direction {
    NORTH, EAST, SOUTH, WEST
}

fun usage() {
    Direction.<!DEBUG_INFO_CALLABLE_OWNER("Direction.konstues in Direction")!>konstues()<!>
}