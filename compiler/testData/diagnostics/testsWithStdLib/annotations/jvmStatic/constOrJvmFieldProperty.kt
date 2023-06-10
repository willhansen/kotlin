// FIR_IDENTICAL
class A {

    companion object {

        <!JVM_STATIC_ON_CONST_OR_JVM_FIELD!>@JvmStatic const konst z<!> = 1;

        <!JVM_STATIC_ON_CONST_OR_JVM_FIELD!>@JvmStatic @JvmField konst x<!> = 1;
    }

}


object B {

    <!JVM_STATIC_ON_CONST_OR_JVM_FIELD!>@JvmStatic const konst z<!> = 1;

    <!JVM_STATIC_ON_CONST_OR_JVM_FIELD!>@JvmStatic @JvmField konst x<!> = 1;
}
