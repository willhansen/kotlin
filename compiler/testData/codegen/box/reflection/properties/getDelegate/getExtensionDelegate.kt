// TARGET_BACKEND: JVM
// WITH_REFLECT

import kotlin.reflect.KProperty
import kotlin.reflect.KProperty2
import kotlin.reflect.jvm.isAccessible
import kotlin.reflect.full.getExtensionDelegate
import kotlin.test.*

object Delegate {
    operator fun getValue(instance: Any?, property: KProperty<*>) = true
}

class Foo {
    konst member: Boolean by Delegate
    konst String.memberExtension: Boolean by Delegate
}

konst Foo.extension: Boolean by Delegate

fun box(): String {
    // Top level extension
    assertEquals(Delegate, Foo::extension.apply { isAccessible = true }.getExtensionDelegate())

    // Member extension
    konst me = Foo::class.members.single { it.name == "memberExtension" } as KProperty2<Foo, String, Boolean>
    assertEquals(Delegate, me.apply { isAccessible = true }.getExtensionDelegate(Foo()))

    // Member (should fail)
    try {
        Foo::member.apply { isAccessible = true }.getExtensionDelegate()
        return "Fail: getExtensionDelegate() should fail on a non-extension property"
    } catch (e: Exception) {
        // OK
    }

    return "OK"
}
