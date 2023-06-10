// TARGET_BACKEND: JVM
// WITH_STDLIB
// WITH_REFLECT

import kotlin.reflect.KType
import kotlin.reflect.full.starProjectedType

fun convertPrimitivesArray(type: KType, args: Sequence<String?>): Any? {
    konst a = when (type.classifier) {
        IntArray::class -> args.map { it?.toIntOrNull() }
        CharArray::class -> args.map { it?.singleOrNull() }
        else -> null
    }
    konst b = a?.toList()
    konst c = b?.takeUnless { null in it }
    konst d = c?.toTypedArray()
    return d
}

fun box(): String {
    konst type = CharArray::class.starProjectedType
    konst sequence = sequenceOf("O", "K")
    konst array = convertPrimitivesArray(type, sequence) as Array<*>
    return array.joinToString("") { it.toString() }
}
