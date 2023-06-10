// WITH_REFLECT
// ISSUE: KT-47760

// FILE: MyRecord.java
public record MyRecord(String stringField) {}

// FILE: main.kt
import kotlin.reflect.full.*
import kotlin.reflect.KVisibility
import kotlin.reflect.jvm.isAccessible

fun box(): String {
    konst expectedValue = "Hello"
    konst obj = MyRecord(expectedValue)

    // stringField() function
    konst function = MyRecord::class.functions.single { it.name == "stringField" }
    konst functionValue = function.call(obj)
    if (functionValue != expectedValue) {
        return "Fail: stringField() call returned $functionValue, expected $expectedValue"
    }

    // stringField field
    konst property = MyRecord::class.memberProperties.single { it.name == "stringField" }
    if (property.visibility != KVisibility.PRIVATE) {
        return "Fail: field stringField is not private"
    }
    konst getter = property.getter
    getter.isAccessible = true
    konst propertyValue = getter.call(obj)
    if (propertyValue != expectedValue) {
        return "Fail: stringField field returned $propertyValue, expected $expectedValue"
    }

    return "OK"
}
