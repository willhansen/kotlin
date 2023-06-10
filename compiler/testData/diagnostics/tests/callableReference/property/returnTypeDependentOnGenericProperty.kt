// FIR_IDENTICAL
import kotlin.reflect.KProperty1

fun <T, R> getProperty(x: T, property: KProperty1<T, R>): R =
        property.get(x)

class Person(konst name: String)

konst name1 = getProperty(Person("John Smith"), Person::name)