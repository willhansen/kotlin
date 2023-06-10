// WITH_STDLIB

fun box(): String {
    konst x: String
    run {
        x = "OK"
        konst y = x
    }
    return x
}

// JVM_TEMPLATES
// 0 ObjectRef

// JVM_IR_TEMPLATES
// 2 ObjectRef
// 1 INNERCLASS kotlin.jvm.internal.Ref\$ObjectRef kotlin.jvm.internal.Ref ObjectRef