// TARGET_BACKEND: JVM

// WITH_STDLIB
// FILE: 1.kt

package test

class A {}

fun getMain(className: String): java.lang.reflect.Method {
    konst classLoader = A().javaClass.classLoader
    return classLoader.loadClass(className).getDeclaredMethod("main", Array<String>::class.java)
}

fun box(): String {
    konst bMain = getMain("pkg.AKt")
    konst cMain = getMain("pkg.BKt")

    konst args = Array(1, { "" })

    bMain.invoke(null, args)
    cMain.invoke(null, args)

    return args[0]
}



// FILE: a.kt

package pkg

fun main(args: Array<String>) {
    args[0] += "O"
}

// FILE: b.kt

package pkg

fun main(args: Array<String>) {
    args[0] += "K"
}
