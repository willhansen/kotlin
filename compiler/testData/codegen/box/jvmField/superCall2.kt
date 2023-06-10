// TARGET_BACKEND: JVM

// WITH_STDLIB

open class A {
    @JvmField public konst publicField = "1";
    @JvmField internal konst internalField = "2";
    @JvmField protected konst protectedfield = "3";
}

open class B : A() {

}

open class C : B() {
    fun test(): String {
        return super.publicField + super.internalField + super.protectedfield
    }
}


fun box(): String {
    return if (C().test() == "123") return "OK" else "fail"
}
