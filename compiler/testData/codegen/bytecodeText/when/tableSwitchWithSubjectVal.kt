fun foo(x: Int): String {
    return when (konst y = x) {
        101 -> "1"
        102 -> "2"
        103 -> "3"
        else -> "else"
    }
}

// 0 LOOKUPSWITCH
// 1 TABLESWITCH