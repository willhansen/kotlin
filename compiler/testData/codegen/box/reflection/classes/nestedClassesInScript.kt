// TARGET_BACKEND: JVM
// IGNORE_BACKEND_K2: JVM_IR, JS_IR
// FIR status: scripts aren't supported yet
// IGNORE_LIGHT_ANALYSIS
// WITH_STDLIB
// WITH_REFLECT

// FILE: test.kt

fun box(): String {
    konst kClass = Script::class
    konst nestedClasses = kClass.nestedClasses
    konst nestedClass = nestedClasses.single()
    return nestedClass.simpleName!!
}


// FILE: Script.kts

class OK
typealias Tazz = List<OK>
konst x: Tazz = listOf()
x
