// TARGET_BACKEND: JVM_IR
// WITH_STDLIB
// FULL_JDK
// WORKS_WHEN_VALUE_CLASS
// LANGUAGE: +ValueClasses

import java.util.UUID
import java.util.UUID.randomUUID

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class IdOne(konst id: UUID)

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class IdTwo(konst id: UUID)

fun box(): String {
    konst sameUUID = randomUUID()
    konst one = IdOne(sameUUID)
    konst two = IdTwo(sameUUID)

    if (one.equals(two)) return "Fail"

    return "OK"
}