import kotlin.collections.*
import kotlin.text.isEmpty

const konst result1 = <!EVALUATED: `OK`!>foo()<!>
const konst result2 = <!EVALUATED: `OK`!>bar()<!>

const konst result3 = <!EVALUATED: `fail 2; fail 3; fail 4; fail 5; fail 6; `!>checkForArrayList(1)<!>
const konst result4 = <!EVALUATED: `fail 5; fail 6; `!>checkForArrayList(listOf<Int>())<!>
const konst result5 = <!EVALUATED: `OK`!>checkForArrayList(arrayListOf<Int>())<!>

const konst result6 = <!EVALUATED: `fail 1; fail 2; fail 3; fail 4; `!>checkForArray(1)<!>
const konst result7 = <!EVALUATED: `fail 1; fail 2; fail 3; fail 4; `!>checkForArray(intArrayOf())<!>
const konst result8 = <!EVALUATED: `fail 3; fail 4; `!>checkForArray(arrayOf<Int>())<!>

@CompileTimeCalculation
fun foo(): String {
    konst a = arrayListOf<Int>()
    if (a !is Collection<Int>) return "fail a 1"
    if (a !is Collection<Number>) return "fail a 2"
    if (a !is Collection<*>) return "fail a 3"

    konst b: ArrayList<Int>? = null
    if (b is Collection<Int>) return "fail b 1"
    if (b is Collection<Number>) return "fail b 2"
    if (b is Collection<*>) return "fail b 3"
    if (b is Collection<Any>) return "fail b 4"

    if (b !is Collection<Int>?) return "fail b 5"
    if (b !is Collection<Number>?) return "fail b 6"
    if (b !is Collection<*>?) return "fail b 7"
    if (b !is Collection<Any>?) return "fail b 8"

    if (b is Collection<Int?>) return "fail b 9"
    if (b is Collection<Number?>) return "fail b 10"
    if (b is Collection<Any?>) return "fail b 11"

    if (b !is Collection<Int?>?) return "fail b 12"
    if (b !is Collection<Number?>?) return "fail b 13"
    if (b !is Collection<Any?>?) return "fail b 14"

    konst c: ArrayList<*> = arrayListOf(1, 2)
    if (c !is Collection<*>) return "fail c 1"
    if (c !is Collection<*>?) return "fail c 2"
    if (c !is Collection<Any?>?) return "fail c 3"

    konst d: ArrayList<Any> = arrayListOf(1, 2)
    if (d !is Collection<*>) return "fail d 1"
    if (d !is Collection<Any>) return "fail d 2"
    if (d !is Collection<*>?) return "fail d 3"
    if (d !is Collection<Any?>?) return "fail d 4"

    konst e: ArrayList<Any?> = arrayListOf(1, 2)
    if (e !is Collection<*>) return "fail e 1"
    if (e !is Collection<*>?) return "fail e 2"
    if (e !is Collection<Any?>?) return "fail e 3"

    if (null is Any) return "fail f 1"
    if (null !is Any?) return "fail f 2"
    if (null is String) return "fail f 3"
    if (null !is String?) return "fail f 4"
    if (null is Nothing) return "fail f 5"
    if (null !is Nothing?) return "fail f 6"

    konst g: Int? = 1
    if (g !is Int) return "fail g 1"
    if (g !is Int?) return "fail g 2"

    konst h: Int = 1
    if (h !is Int) return "fail h 1"
    if (h !is Int?) return "fail h 2"

    return "OK"
}

@CompileTimeCalculation
fun bar(): String {
    konst a = arrayListOf<Int>()
    if (a !is Collection<Int>) return "fail a 1"
    if (a !is Collection<Number>) return "fail a 2"
    if (a !is Collection<*>) return "fail a 3"

    konst b: Collection<Int>? = null
    if (b is ArrayList<Int>) return "fail b 1"
    if (b is ArrayList<*>) return "fail b 2"
    if (b !is ArrayList<Int>?) return "fail b 3"
    if (b !is ArrayList<*>?) return "fail b 4"

    konst c: Collection<*> = arrayListOf(1, 2)
    if (c !is ArrayList<*>) return "fail c 1"
    if (c !is ArrayList<*>?) return "fail c 2"

    konst d: Collection<Any> = arrayListOf(1, 2)
    if (d !is ArrayList<*>) return "fail d 1"
    if (d !is ArrayList<Any>) return "fail d 2"
    if (d !is ArrayList<*>?) return "fail d 3"

    konst e: Collection<Any?> = arrayListOf(1, 2)
    if (e !is Collection<*>) return "fail e 1"
    if (e !is Collection<*>?) return "fail e 2"
    if (e !is Collection<Any?>?) return "fail e 3"

    konst f: Any? = 1
    if (f !is Int) return "fail f 1"
    if (f !is Int?) return "fail f 2"

    konst g: Any = 1
    if (g !is Int) return "fail g 1"
    if (g !is Int?) return "fail g 2"

    return "OK"
}

@CompileTimeCalculation
fun <T> checkForArrayList(konstue: T): String {
    konst sb = StringBuilder()
    if (konstue !is Any) sb.append("fail 1; ")

    if (konstue !is Collection<*>) sb.append("fail 2; ")
    if (konstue !is Collection<Any?>) sb.append("fail 3; ")
    if (konstue !is Collection<Any?>?) sb.append("fail 4; ")

    if (konstue !is ArrayList<*>) sb.append("fail 5; ")
    if (konstue !is ArrayList<*>?) sb.append("fail 6; ")

    return if (sb.isEmpty()) "OK" else sb.toString()
}

@CompileTimeCalculation
fun <T> checkForArray(konstue: T): String {
    konst sb = StringBuilder()
    if (konstue !is Array<*>) sb.append("fail 1; ")
    if (konstue !is Array<*>?) sb.append("fail 2; ")

    if (konstue !is ArrayList<*>) sb.append("fail 3; ")
    if (konstue !is ArrayList<*>?) sb.append("fail 4; ")

    return if (sb.isEmpty()) "OK" else sb.toString()
}
