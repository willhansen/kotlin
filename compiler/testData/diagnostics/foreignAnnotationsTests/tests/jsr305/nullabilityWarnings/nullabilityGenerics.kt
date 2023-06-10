// !DIAGNOSTICS: -UNUSED_VARIABLE -UNUSED_PARAMETER
// JSR305_GLOBAL_REPORT: warn

// FILE: A.java
public class A<T> {
    public void foo(@MyNonnull T t) {
    }

    public @MyNullable String bar() {
        return null;
    }

    public @MyNullable T bam() {
        return null;
    }

    @MyNullable
    public <X> X baz() {
        return null;
    }

}
// FILE: main.kt
class X<T>(t: T?) {

    init {
        konst a = A<T>()
        a.foo(<!NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS!>t<!>)

        konst x: T = <!NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS!>a.bam()<!>
        konst y: T = <!NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS!>a.baz<T>()<!>
    }
}

fun test() {
    konst a = A<String?>()
    a.foo(<!NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS!>null<!>)

    konst b: String = <!NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS!>a.bar()<!>
}
