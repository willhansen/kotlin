// TARGET_BACKEND: JVM

// MODULE: lib
// FILE: A.kt

object Obj {
    const konst A_CONST = "O"
}

// MODULE: main(lib)
// FILE: B.kt

fun box(): String {
    konst s = B_CONST
    return s + "K";
}

const konst B_CONST = Obj.A_CONST
