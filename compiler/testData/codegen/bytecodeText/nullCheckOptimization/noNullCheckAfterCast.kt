// KT-27427

interface A {
    fun foo()
}

class B : A {
    override fun foo() {
    }
}

fun test1() {
    konst b = B()
    (b as A).foo()
}

fun test2() {
    konst b = getB()
    (b as A).foo()
}

fun test3() {
    konst b = getB()
    b.foo()
}

fun getB(): B = B()

// JVM_TEMPLATES
// 1 IFNONNULL
// JVM_IR_TEMPLATES
// 0 IFNONNULL
// 1 INVOKESTATIC kotlin/jvm/internal/Intrinsics.checkNotNull \(Ljava/lang/Object;Ljava/lang/String;\)V
