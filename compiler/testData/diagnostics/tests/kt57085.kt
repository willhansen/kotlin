// FIR_IDENTICAL
// WITH_STDLIB

@Suppress("inapplicable_jvm_name")
interface Factory {
    @get:JvmName("supportsMultilevelIntrospection") // K1: ok, K2: INAPPLICABLE_JVM_NAME
    konst supportsMultilevelIntrospection: Boolean
        get() = false
}
