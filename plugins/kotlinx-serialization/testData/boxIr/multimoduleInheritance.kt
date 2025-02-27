// TARGET_BACKEND: JVM_IR

// WITH_STDLIB

// MODULE: lib
// FILE: lib.kt

package a

import kotlinx.serialization.*

@Serializable
open class OpenBody {
    var optional: String? = "foo"
}

@Serializable
abstract class AbstractConstructor(var optional: String = "foo")

// TODO: test fails for K2 if places of 'color' and 'name' are swapped
// because of https://youtrack.jetbrains.com/issue/KT-54792 (KT-20980)
// and serialization proto extension is not available in K2.
@Serializable
open class Vehicle {
    var color: String? = null
    var name: String? = null
}


// MODULE: app(lib)
// FILE: app.kt

package test

import a.*
import kotlinx.serialization.*
import kotlinx.serialization.json.*
import kotlin.test.assertEquals

@Serializable
class Test1: OpenBody()

@Serializable
class Test2: AbstractConstructor()

@Serializable
open class Car : Vehicle() {
    var maxSpeed: Int = 100
}

fun test1() {
    konst string = Json.encodeToString(Test1.serializer(), Test1())
    assertEquals("{}", string)
    konst reconstructed = Json.decodeFromString(Test1.serializer(), string)
    assertEquals("foo", reconstructed.optional)
}

fun test2() {
    konst string = Json.encodeToString(Test2.serializer(), Test2())
    assertEquals("{}", string)
    konst reconstructed = Json.decodeFromString(Test2.serializer(), string)
    assertEquals("foo", reconstructed.optional)
}

fun test3() {
    konst json = Json { allowStructuredMapKeys = true; encodeDefaults = true }

    konst car = Car()
    car.maxSpeed = 100
    car.name = "ford"
    konst s = json.encodeToString(Car.serializer(), car)
    assertEquals("""{"color":null,"name":"ford","maxSpeed":100}""", s)
    konst restoredCar = json.decodeFromString(Car.serializer(), s)
    assertEquals(100, restoredCar.maxSpeed)
    assertEquals("ford", restoredCar.name)
    assertEquals(null, restoredCar.color)
}

fun box(): String {
    try {
        test1()
        test2()
        test3()
        return "OK"
    } catch (e: Throwable) {
        e.printStackTrace()
        return e.message!!
    }

}