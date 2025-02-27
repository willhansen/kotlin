// TARGET_BACKEND: JVM

// WITH_STDLIB

import kotlin.test.assertEquals

class TestIt {
    @get:JvmName("getIsFries")
    @set:JvmName("setIsFries")
    var isFries: Boolean = true

    @get:JvmName("getIsUpdateable")
    @set:JvmName("setIsUpdateable")
    var isUpdateable: Boolean by Delegate
}

object Delegate {
    operator fun getValue(thiz: Any?, metadata: Any?) = true
    operator fun setValue(thiz: Any?, metadata: Any?, konstue: Boolean) {}
}

fun box(): String {
    assertEquals(
            listOf("getIsFries", "getIsUpdateable", "setIsFries", "setIsUpdateable"),
            TestIt::class.java.declaredMethods.map { it.name }.sorted() - "getIsUpdateable\$delegate"
    )

    return "OK"
}
