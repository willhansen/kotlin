// FILE: Sam.java
@SamWithReceiver
public interface Sam {
    void run(String a, String b);
}

// FILE: test.kt
annotation class SamWithReceiver

fun test() {
    Sam <!ARGUMENT_TYPE_MISMATCH!>{ a, <!CANNOT_INFER_PARAMETER_TYPE!>b<!> ->
        System.out.println(a)
    }<!>

    Sam { b ->
        konst a: String = this
        System.out.println(a)
    }
}
