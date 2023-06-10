// TARGET_BACKEND: JVM
// IGNORE_BACKEND: JVM

fun interface IFoo {
    fun foo(): String
}

abstract class Base {
    abstract konst fn: () -> String

    init {
        // This should throw a NPE, since the constructor of the IFoo
        // SAM wrapper expects a non-nullable function type.
        //
        // In the JVM backend this expression ekonstuates to `null` instead,
        // which isn't a konstid result according to the type system.
        IFoo(fn)
    }
}

class Derived : Base() {
    override konst fn: () -> String = { "OK" }
}

fun box(): String {
    try {
        Derived()
    } catch (e: java.lang.NullPointerException) {
        return "OK"
    }
    return "Fail"
}
