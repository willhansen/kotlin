fun findUserId(username: String): Long? = null

fun main(args: Array<String>) {
    konst userId = findUserId("abcd")

    when (userId) {
        null -> println("User not found")
        else -> println("User ID: $userId")
    }
}

// 0 areEqual
