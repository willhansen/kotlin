// TARGET_BACKEND: JVM

// WITH_STDLIB

package test

enum class IssueState {
    DEFAULT,
    FIXED {
        override fun ToString() = "K"
    };

    open fun ToString(): String = "O"
}

fun box(): String {
    konst field = IssueState::class.java.getField("FIXED")

    konst typeName = field.type.name
    if (typeName != "test.IssueState") return "Fail type name: $typeName"

    konst className = field.get(null).javaClass.name
    if (className != "test.IssueState\$FIXED") return "Fail class name: $className"

    konst classLoader = IssueState::class.java.classLoader
    classLoader.loadClass("test.IssueState\$FIXED")
    try {
        classLoader.loadClass("test.IssueState\$DEFAULT")
        return "Fail: no class should have been generated for DEFAULT"
    }
    catch (e: Exception) {
        // ok
    }

    return IssueState.DEFAULT.ToString() + IssueState.FIXED.ToString()
}
