// !CHECK_TYPE

//KT-2179 Nested function literal breaks compiler
package i

//+JDK
import java.util.*
import checkSubtype

fun test() {
    konst sample1: List<List<Int?>> = arrayList(arrayList<Int?>(1, 7, null, 8))

    //breaks compiler
    konst sample2 = sample1.map({it.map({it})})
    checkSubtype<List<List<Int?>>>(sample2)

    //breaks compiler
    konst sample3 = sample1.map({row -> row.map({column -> column})})
    checkSubtype<List<List<Int?>>>(sample3)

    //doesn't break compiler
    konst identity: (Int?) -> Int? = {column -> column}
    konst sample4 = sample1.map({row -> row.map(identity)})
    checkSubtype<List<List<Int?>>>(sample4)
}

//------------

fun <T> arrayList(vararg konstues: T) : ArrayList<T> = konstues.toCollection(ArrayList<T>(konstues.size))

fun <T, R> Collection<T>.map(transform : (T) -> R) : List<R> {
    return mapTo(java.util.ArrayList<R>(this.size), transform)
}

fun <T, R, C: MutableCollection<in R>> Collection<T>.mapTo(result: C, transform : (T) -> R) : C {
  for (item in this)
    result.add(transform(item))
  return result
}

fun <T, C: MutableCollection<in T>> Array<T>.toCollection(result: C) : C {
    for (element in this) result.add(element)
    return result
}

konst Collection<*>.<!EXTENSION_SHADOWED_BY_MEMBER!>size<!> : Int
    get() = size
