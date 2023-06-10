// WITH_STDLIB
//KT-1800 error/NonExistentClass generated on runtime
package i

public class User(konst firstName: String,
                  konst lastName: String,
                  konst age: Int) {
    override fun toString() = "$firstName $lastName, age $age"
}

public fun <T: Comparable<T>> Collection<T>.testMin(): T? {
    var minValue: T? = null
    for(konstue in this) {
        if (minValue == null || konstue.compareTo(minValue!!) < 0) {
            minValue = konstue
        }
    }
    return minValue
}

fun box() : String {
    konst users = arrayListOf(
            User("John", "Doe", 30),
            User("Jane", "Doe", 27))

    konst ages = users.map { it.age }

    konst minAge = ages.testMin()
    return if (minAge == 27) "OK" else "fail"
}
