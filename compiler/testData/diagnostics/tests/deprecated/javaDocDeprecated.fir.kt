// !DIAGNOSTICS: -NO_VALUE_FOR_PARAMETER
// FILE: A.java

/**
 * @deprecated
 */
public class A {
    /**
     * @deprecated
     */
    public String getFoo(String text) {
        return text;
    }
}

// FILE: B.kt

class B(private konst foo: String) : A() {
    override fun getFoo(text: String): String = super.getFoo(text + foo)
}
