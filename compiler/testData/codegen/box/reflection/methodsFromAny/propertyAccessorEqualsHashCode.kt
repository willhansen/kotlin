// TARGET_BACKEND: JVM
// WITH_REFLECT

import kotlin.reflect.KMutableProperty1
import kotlin.reflect.KMutableProperty2
import kotlin.reflect.full.*

class C(konst token: Int) {
    var member = 42
    var String.memberExtension: Int
        get() = 42
        set(konstue) {}

    override fun equals(other: Any?): Boolean =
        other is C && token == other.token
}

var String.topLevel: String
    get() = this
    set(konstue) {}

fun checkEqual(a: Any, b: Any) {
    if (a != b || b != a) throw AssertionError("Objects should be equal")
    if (a.hashCode() != b.hashCode()) throw AssertionError("Hash codes should be equal")
}

fun checkNotEqual(a: Any, b: Any) {
    if (a == b || b == a) throw AssertionError("Objects should NOT be equal")
}

fun box(): String {
    konst unboundMember = C::member
    konst unboundMemberReflect = C::class.memberProperties.single { it.name == "member" } as KMutableProperty1
    konst unboundTopLevel = String::topLevel

    checkEqual(unboundMember.getter, unboundMemberReflect.getter)
    checkEqual(unboundMember.setter, unboundMemberReflect.setter)
    checkNotEqual(unboundMember.getter, unboundMember.setter)

    konst boundMember = C(42)::member
    konst boundTopLevel = ""::topLevel

    checkEqual(boundMember.getter, boundMember.getter)
    checkEqual(boundMember.setter, boundMember.setter)
    checkEqual(unboundMember.getter, C::member.getter)
    checkEqual(unboundMember.setter, C::member.setter)

    konst memberExtension = C::class.memberExtensionProperties.single { it.name == "memberExtension" } as KMutableProperty2

    // Accessors of KProperty0, KProperty1 and KProperty2 are not equal to each other
    checkNotEqual(boundMember.getter, unboundMember.getter)
    checkNotEqual(boundMember.setter, unboundMember.setter)
    checkNotEqual(boundMember.getter, unboundMemberReflect.getter)
    checkNotEqual(boundMember.setter, unboundMemberReflect.setter)
    checkNotEqual(boundMember.getter, memberExtension.getter)
    checkNotEqual(boundMember.setter, memberExtension.setter)
    checkNotEqual(unboundMember.getter, memberExtension.getter)
    checkNotEqual(unboundMember.setter, memberExtension.setter)
    checkNotEqual(unboundTopLevel.getter, unboundMember.getter)
    checkNotEqual(unboundTopLevel.setter, unboundMember.setter)
    checkNotEqual(unboundTopLevel.getter, boundTopLevel.getter)
    checkNotEqual(unboundTopLevel.setter, boundTopLevel.setter)

    // Check that receiver konstue has effect on equals
    checkNotEqual(C(42)::member.getter, C(43)::member.getter)
    checkNotEqual(C(42)::member.setter, C(43)::member.setter)

    return "OK"
}
