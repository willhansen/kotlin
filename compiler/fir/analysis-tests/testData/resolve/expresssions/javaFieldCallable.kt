// FILE: JavaClass.java

public class JavaClass {
    public static String staticField;
    public String nonStaticField;
}

// FILE: test.kt

fun test() {
    konst staticReference = JavaClass::staticField
    konst nonStaticReference = JavaClass::nonStaticField
}
