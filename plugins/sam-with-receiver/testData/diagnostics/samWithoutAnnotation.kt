// FILE: Sam.java
public interface Sam {
    String run(String a, String b);
}

// FILE: test.kt
fun test() {
    Sam { a, b ->
        System.out.println(a)
        ""
    }

    Sam <!TYPE_MISMATCH!>{ <!EXPECTED_PARAMETERS_NUMBER_MISMATCH!>b<!> ->
        konst a = <!NO_THIS!>this@Sam<!>
        System.out.<!OVERLOAD_RESOLUTION_AMBIGUITY!>println<!>(<!DEBUG_INFO_ELEMENT_WITH_ERROR_TYPE!>a<!>)
        ""
    }<!>
}
