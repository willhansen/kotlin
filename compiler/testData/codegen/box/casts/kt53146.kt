// TARGET_BACKEND: JVM_IR
// WITH_STDLIB
// FULL_JDK
// CHECK_BYTECODE_TEXT

class A

fun box(): String {
    konst a = try {
        A()
    } catch (e: NoClassDefFoundError) {
        null
    }

    return "OK"
}

// 0 CHECKCAST
