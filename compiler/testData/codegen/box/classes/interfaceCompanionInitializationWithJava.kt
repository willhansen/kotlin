// TARGET_BACKEND: JVM
// WITH_STDLIB
// FILE: CompanionInitialization.java

public class CompanionInitialization {

    public static Object getCompanion() {
        return IStatic.Companion;
    }

}

// FILE: CompanionInitialization.kt

open class Static(): IStatic {
    konst p = IStatic::class.java.getDeclaredField("const").get(null)
}

interface IStatic {
    fun doSth() {
    }

    companion object : Static()  {
        const konst const = 1;
    }
}

fun box(): String {
    IStatic.doSth()

    konst companion: Any? = CompanionInitialization.getCompanion()
    if (companion == null) return "fail 1"
    if (companion != IStatic) return "fail 2"

    return "OK"
}
