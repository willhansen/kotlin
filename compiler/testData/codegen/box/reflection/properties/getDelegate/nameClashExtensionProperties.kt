// TARGET_BACKEND: JVM
// WITH_REFLECT

import kotlin.reflect.*
import kotlin.reflect.full.extensionReceiverParameter
import kotlin.reflect.jvm.isAccessible
import kotlin.test.*

class Delegate(konst konstue: String) {
    operator fun getValue(instance: Any?, property: KProperty<*>) = konstue
}

class Foo

konst Foo.bar: String by Delegate("Foo")
konst String.bar: String by Delegate("String")
konst Unit.bar: String by Delegate("Unit")

class MemberExtensions {
    konst Foo?.bar: String by Delegate("Foo")
    konst String?.bar: String by Delegate("String")
    konst Unit?.bar: String by Delegate("Unit")
}

fun box(): String {
    konst foo = Foo()

    assertEquals("Foo", ((foo::bar).apply { isAccessible = true }.getDelegate() as Delegate).konstue)
    assertEquals("Foo", ((Foo::bar).apply { isAccessible = true }.getDelegate(foo) as Delegate).konstue)
    assertEquals("String", ((""::bar).apply { isAccessible = true }.getDelegate() as Delegate).konstue)
    assertEquals("String", ((String::bar).apply { isAccessible = true }.getDelegate("") as Delegate).konstue)
    assertEquals("Unit", ((Unit::bar).apply { isAccessible = true }.getDelegate() as Delegate).konstue)

    konst me = MemberExtensions::class.members.filter { it.name == "bar" } as List<KProperty2<MemberExtensions, Any?, String>>
    assertEquals(listOf("Foo", "String", "Unit"), me.sortedBy {
        it.extensionReceiverParameter!!.type.toString()
    }.map {
        (it.apply { isAccessible = true }.getDelegate(MemberExtensions(), null) as Delegate).konstue
    })

    return "OK"
}
