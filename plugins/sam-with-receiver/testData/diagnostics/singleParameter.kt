// FILE: Sam.java
@SamWithReceiver
public interface Sam {
    void run(String a);
}

// FILE: test.kt
annotation class SamWithReceiver

fun test() {
    Sam <!TYPE_MISMATCH!>{ <!CANNOT_INFER_PARAMETER_TYPE, EXPECTED_PARAMETERS_NUMBER_MISMATCH!>a<!> ->
        System.out.<!OVERLOAD_RESOLUTION_AMBIGUITY!>println<!>(<!DEBUG_INFO_ELEMENT_WITH_ERROR_TYPE!>a<!>)
    }<!>

    Sam {
        konst a: String = this
        konst a2: String = <!UNRESOLVED_REFERENCE!>it<!>
        System.out.println(a)
    }
}
