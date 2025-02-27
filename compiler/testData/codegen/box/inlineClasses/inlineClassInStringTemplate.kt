// WITH_STDLIB
// WORKS_WHEN_VALUE_CLASS
// LANGUAGE: +ValueClasses

import kotlin.test.*

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class Z(konst konstue: Int)

fun test1_1(z: Z) = "$z"
fun test1_2(z: Z) = "$z$z"
fun test1_many(z: Z) = "$z $z $z"
fun test1_concat1(z: Z) = "-" + z
fun test1_concat2(z: Z) = "$z" + z
fun test1_concat3(z: Z) = "-" + z + z

fun test2_1(z: Z?) = "$z"
fun test2_2(z: Z?) = "$z$z"
fun test2_many(z: Z?) = "$z $z $z"
fun test2_concat1(z: Z?) = "-" + z
fun test2_concat2(z: Z?) = "$z" + z
fun test2_concat3(z: Z?) = "-" + z + z

fun box(): String {
    assertEquals("Z(konstue=42)", test1_1(Z(42)))
    assertEquals("Z(konstue=42)Z(konstue=42)", test1_2(Z(42)))
    assertEquals("Z(konstue=42) Z(konstue=42) Z(konstue=42)", test1_many(Z(42)))
    assertEquals("-Z(konstue=42)", test1_concat1(Z(42)))
    assertEquals("Z(konstue=42)Z(konstue=42)", test1_concat2(Z(42)))
    assertEquals("-Z(konstue=42)Z(konstue=42)", test1_concat3(Z(42)))

    assertEquals("null", test2_1(null))
    assertEquals("nullnull", test2_2(null))
    assertEquals("null null null", test2_many(null))
    assertEquals("-null", test2_concat1(null))
    assertEquals("nullnull", test2_concat2(null))
    assertEquals("-nullnull", test2_concat3(null))

    assertEquals("Z(konstue=42)", test2_1(Z(42)))
    assertEquals("Z(konstue=42)Z(konstue=42)", test2_2(Z(42)))
    assertEquals("Z(konstue=42) Z(konstue=42) Z(konstue=42)", test2_many(Z(42)))
    assertEquals("-Z(konstue=42)", test2_concat1(Z(42)))
    assertEquals("Z(konstue=42)Z(konstue=42)", test2_concat2(Z(42)))
    assertEquals("-Z(konstue=42)Z(konstue=42)", test2_concat3(Z(42)))

    return "OK"
}