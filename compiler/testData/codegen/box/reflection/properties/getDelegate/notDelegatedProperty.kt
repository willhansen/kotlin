// TARGET_BACKEND: JVM
// WITH_REFLECT

import kotlin.reflect.KProperty2
import kotlin.reflect.jvm.isAccessible
import kotlin.test.*

konst topLevel: Boolean = true
konst String.extension: Boolean get() = true

class Foo {
    konst member: Boolean = true
    konst String.memberExtension: Boolean get() = true
}

fun box(): String {
    assertNull(::topLevel.apply { isAccessible = true }.getDelegate())

    assertNull(String::extension.apply { isAccessible = true }.getDelegate(""))
    assertNull(""::extension.apply { isAccessible = true }.getDelegate())

    assertNull(Foo::member.apply { isAccessible = true }.getDelegate(Foo()))
    assertNull(Foo()::member.apply { isAccessible = true }.getDelegate())

    konst me = Foo::class.members.single { it.name == "memberExtension" } as KProperty2<Foo, String, Boolean>
    assertNull(me.apply { isAccessible = true }.getDelegate(Foo(), ""))

    return "OK"
}
