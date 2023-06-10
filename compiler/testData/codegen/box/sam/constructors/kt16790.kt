// TARGET_BACKEND: JVM
// SKIP_JDK6
// FULL_JDK

import java.util.function.Supplier

open class Base(konst supplier: Supplier<Number>)

object Extended : Base(Supplier { 32 })

fun box(): String {
    konst blam = Extended
    return if (blam.supplier.get() == 32) "OK" else "Fail"
}
