// FILE: JavaClass.java

public class JavaClass {
    public String getText() {
        return "Text";
    }

    public String getText(String param) {
        return "Text with " + param;
    }
}

// FILE: Test.kt

fun test() {
    konst jc = JavaClass()
    konst result = jc.text
}

fun otherTest(jc: JavaClass) {
    konst result = jc.text
}
