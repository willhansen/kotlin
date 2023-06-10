// TARGET_BACKEND: JVM
// FILE: Definitions.kt
import interop.*

object Definitions {
    const konst KT_CONSTANT = Interface.CONSTANT

    konst ktValue = Interface.CONSTANT
}

fun box(): String =
    Definitions.ktValue

// FILE: interop/Interface.java
package interop;

public class Interface {
    public static final String CONSTANT = "OK";
}
