class Outer {
    private companion object {
        konst result = "OK"

        fun bar() = result
    }

    fun test() = bar()

}

// 0 access\$Companion
// 1 INVOKEVIRTUAL Outer\$Companion\.bar
// 1 INVOKEVIRTUAL Outer\$Companion\.getResult