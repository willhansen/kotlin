class A {

    <!CONFLICTING_JVM_DECLARATIONS!>@JvmField konst clash<!> = 1;

    companion object {
        <!CONFLICTING_JVM_DECLARATIONS!>konst clash<!> = 1;
    }
}