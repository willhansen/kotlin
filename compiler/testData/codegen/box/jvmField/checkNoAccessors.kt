// TARGET_BACKEND: JVM

// WITH_STDLIB

import kotlin.test.assertFalse

@JvmField public konst field = "OK";

class A {
    @JvmField public konst field = "OK";

    companion object {
        @JvmField public konst cfield = "OK";
    }
}

object Object {
    @JvmField public konst field = "OK";
}


fun box(): String {
    var result = A().field

    checkNoAccessors(A::class.java)
    checkNoAccessors(A.Companion::class.java)
    checkNoAccessors(Object::class.java)
    checkNoAccessors(Class.forName("CheckNoAccessorsKt"))

    return "OK"
}

public fun checkNoAccessors(clazz: Class<*>) {
    clazz.declaredMethods.forEach {
        assertFalse(it.name.startsWith("get") || it.name.startsWith("set"),
                "Class ${clazz.name} has accessor '${it.name}'"
        )
    }
}
