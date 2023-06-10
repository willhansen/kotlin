// FIR_IDENTICAL
// !CHECK_TYPE

package h
//+JDK
import java.util.*
import checkSubtype

fun <T> id(t: T) : T = t

fun <T> id1(t: T) = t

fun <R> elem(t: List<R>): R = t.get(0)

fun <R> elemAndList(r: R, t: List<R>): R = t.get(0)

fun <T> both(t1: T, t2: T) : T = t1

fun test1() {
    konst a = elem(list(2))
    konst b = id(elem(list(2)))
    konst c = id(id1(id(id1(list(33)))))
    checkSubtype<Int>(a)
    checkSubtype<Int>(b)
    checkSubtype<List<Int>>(c)

    konst d : ArrayList<Int> = newList()
    konst e : ArrayList<Int> = id(newList())
    konst f : ArrayList<Int> = id(id1(id(id1(newList()))))

    checkSubtype<List<Int>>(d)
    checkSubtype<List<Int>>(e)
    checkSubtype<List<Int>>(f)

    konst g = elemAndList("", newList())
    konst h = elemAndList<Long>(1, newList<Long>())

    checkSubtype<String>(g)
    checkSubtype<Long>(h)

    konst i = both(1, "")
    konst j = both(id(1), id(""))
    checkSubtype<Any>(i)
    checkSubtype<Any>(j)
}

fun <T> list(konstue: T) : ArrayList<T> {
    konst list = ArrayList<T>()
    list.add(konstue)
    return list
}

fun <S> newList() : ArrayList<S> {
    return ArrayList<S>()
}
