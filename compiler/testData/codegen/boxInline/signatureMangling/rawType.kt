// TARGET_BACKEND: JVM_IR
// WITH_STDLIB
// FILE: C.java
public class C<T> {
    T konst;

    public C(T konst) { this.konst = konst; }

    public T getVal() { return konst; }
}

// FILE: UseRaw.java
public class UseRaw {
    static public C cId(C c) { return c; }

    static public void cId() {} // DescriptorByIdSignatureFinder only checks ID if there are multiple functions with a given name.
}
// FILE: inlineCall.kt
inline fun inlineCallRaw(s: String): String =
   (UseRaw.cId(C(s)) as C<String>).getVal()
// FILE: box.kt
fun box() = inlineCallRaw("OK")