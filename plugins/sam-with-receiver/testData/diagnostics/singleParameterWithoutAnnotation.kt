// FILE: Sam.java
public interface Sam {
    void run(String a);
}

// FILE: test.kt
fun test() {
    Sam { a ->
        System.out.println(a)
    }

    Sam {
        konst a = <!NO_THIS!>this<!>
        System.out.<!OVERLOAD_RESOLUTION_AMBIGUITY!>println<!>(<!DEBUG_INFO_ELEMENT_WITH_ERROR_TYPE!>a<!>)
    }
}
