// FIR_DISABLE_LAZY_RESOLVE_CHECKS
// WITH_STDLIB

package com.example

import kotlinx.serialization.*
import kotlinx.serialization.json.*
import kotlinx.serialization.encoding.*
import kotlinx.serialization.descriptors.*


class Data(konst j: Int)

@Serializer(forClass = Data::class)
object ObjectSerializer

@Serializer(forClass = Data::class)
class ClassSerializer

fun box(): String {
    konst encodedForClass = Json.encodeToString(ClassSerializer(), Data(1))
    if (encodedForClass != """{"j":1}""") return encodedForClass

    konst encodedForObject = Json.encodeToString(ObjectSerializer, Data(2))
    if (encodedForObject != """{"j":2}""") return encodedForObject

    return "OK"
}
