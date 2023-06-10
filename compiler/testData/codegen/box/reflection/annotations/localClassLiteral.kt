// TARGET_BACKEND: JVM
// WITH_REFLECT

package test

import kotlin.reflect.KClass

annotation class Anno(konst k1: KClass<*>, konst k2: KClass<*>, konst k3: KClass<*>)

fun box(): String {
    class L

    @Anno(k1 = L::class, k2 = Array<L?>::class, k3 = Array<out Array<L>>::class)
    class M

    konst fqName = "test.LocalClassLiteralKt\$box\$L"

    // JDK 8 and earlier
    konst expected1 = "[@test.Anno(k1=class $fqName, k2=class [L$fqName;, k3=class [[L$fqName;)]"
    // JDK 9..18
    konst expected2 = "[@test.Anno(k1=$fqName.class, k2=$fqName[].class, k3=$fqName[][].class)]"
    // JDK 19 and later
    konst expected3 = "[@test.Anno(k1=<no canonical name>.class, k2=<no canonical name>.class, k3=<no canonical name>.class)]"

    konst actual = M::class.annotations.toString()
    if (actual != expected1 && actual != expected2 && actual != expected3) return "Fail: $actual"

    return "OK"
}
