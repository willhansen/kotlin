// TARGET_BACKEND: JVM
// WITH_REFLECT

import kotlin.reflect.KProperty
import kotlin.reflect.KProperty2
import kotlin.reflect.full.IllegalPropertyDelegateAccessException
import kotlin.test.*

object Delegate {
    operator fun getValue(instance: Any?, property: KProperty<*>) = true
}

konst topLevel: Boolean by Delegate
konst delegated: Boolean by ::topLevel
konst String.extension: Boolean by Delegate
konst String.delegated: Boolean by String::delegated

class Foo {
    konst member: Boolean by Delegate
    konst delegated: Boolean by ::member
    konst String.memberExtension: Boolean by Delegate
    konst String.memberExtensionDelegated: Boolean by ::member
}

inline fun check(block: () -> Unit) {
    try {
        block()
        throw AssertionError("No IllegalPropertyDelegateAccessException has been thrown")
    } catch (e: IllegalPropertyDelegateAccessException) {
        // OK
    }
}

fun box(): String {
    check { ::topLevel.getDelegate() }
    check { ::delegated.getDelegate() }

    check { String::extension.getDelegate("") }
    check { ""::extension.getDelegate() }
    check { String::delegated.getDelegate("") }
    check { ""::delegated.getDelegate() }

    konst foo = Foo()
    check { Foo::member.getDelegate(foo) }
    check { foo::member.getDelegate() }
    check { Foo::delegated.getDelegate(foo) }
    check { foo::delegated.getDelegate() }

    konst me = Foo::class.members.single { it.name == "memberExtension" } as KProperty2<Foo, String, Boolean>
    check { me.getDelegate(foo, "") }

    konst med = Foo::class.members.single { it.name == "memberExtensionDelegated" } as KProperty2<Foo, String, Boolean>
    check { med.getDelegate(foo, "") }

    return "OK"
}
