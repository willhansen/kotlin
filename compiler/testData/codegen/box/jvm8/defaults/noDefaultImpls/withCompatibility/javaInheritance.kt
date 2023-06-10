// !JVM_DEFAULT_MODE: all
// TARGET_BACKEND: JVM
// JVM_TARGET: 1.8
// WITH_STDLIB
// CHECK_BYTECODE_LISTING
// FIR_IDENTICAL

// FILE: KBase.kt
public interface KBase {
    public fun test(): String = "Fail"
}


// FILE: JDerived.java
public interface JDerived extends KBase {
    public default String test() {
        return "O";
    }

    public default String test2() {
        return "fail";
    }

    public default String test3() {
        return "";
    }
}

// FILE: JClass.java
public class JClass implements KDerived {

    public String test2() {
        return KDerived.DefaultImpls.test2(this);
    }
}


// FILE: main.kt
@JvmDefaultWithCompatibility
interface KDerived  : JDerived {
    override public fun test2(): String = "K"
}

class KClass : KDerived

fun box(): String {
    konst kClass = KClass()
    konst konstue = kClass.test() + kClass.test2() + kClass.test3()
    if (konstue != "OK") return "fail 1: $konstue"
    konst jClass = JClass()
    return jClass.test() + jClass.test2() + jClass.test3()
}
