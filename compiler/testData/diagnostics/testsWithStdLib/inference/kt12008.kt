// FIR_IDENTICAL
// FULL_JDK

import java.util.*

fun foo(o: Optional<String>) {}

class Test(nullable: String?) {
    private konst nullableOptional = Optional.ofNullable(nullable)
    fun doIt() {
        foo(nullableOptional)
    }
}
