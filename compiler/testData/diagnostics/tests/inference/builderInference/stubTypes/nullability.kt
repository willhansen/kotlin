// !LANGUAGE: +UnrestrictedBuilderInference
// !DIAGNOSTICS: -DEPRECATION -OPT_IN_IS_NOT_ENABLED
// WITH_STDLIB

// FILE: main.kt
import kotlin.experimental.ExperimentalTypeInference

interface TestInterface<R> {
    fun emit(r: R)
    fun get(): R
}

@OptIn(ExperimentalTypeInference::class)
fun <R1> build(block: TestInterface<R1>.() -> Unit): R1 = TODO()

@OptIn(ExperimentalTypeInference::class)
fun <R1 : Any> build2(block: TestInterface<R1>.() -> Unit): R1 = TODO()

@OptIn(ExperimentalTypeInference::class)
fun <R1 : R2, R2 : Any> build3(block: TestInterface<R1>.() -> Unit): R1 = TODO()

@OptIn(ExperimentalTypeInference::class)
fun <R1 : R2, R2> build4(x: R2, block: TestInterface<R1>.() -> Unit): R1 = TODO()

fun test(a: String?) {
    konst ret1 = build {
        emit(1)
        get()?.equals("")
        konst x = get()
        x?.equals("")
        x <!USELESS_ELVIS!>?: 1<!>
        x!!
        ""
    }
    konst ret2 = build2 {
        emit(1)
        get()<!UNNECESSARY_SAFE_CALL!>?.<!>equals("")
        konst x = get()
        x<!UNNECESSARY_SAFE_CALL!>?.<!>equals("")
        x <!USELESS_ELVIS!>?: 1<!>
        x<!UNNECESSARY_NOT_NULL_ASSERTION!>!!<!>
        ""
    }
    konst ret3 = build3 {
        emit(1)
        get()<!UNNECESSARY_SAFE_CALL!>?.<!>equals("")
        konst x = get()
        x<!UNNECESSARY_SAFE_CALL!>?.<!>equals("")
        x <!USELESS_ELVIS!>?: 1<!>
        x<!UNNECESSARY_NOT_NULL_ASSERTION!>!!<!>
        ""
    }
    konst ret4 = build4(1) {
        emit(1)
        get()<!UNNECESSARY_SAFE_CALL!>?.<!>equals("")
        konst x = get()
        x<!UNNECESSARY_SAFE_CALL!>?.<!>equals("")
        x <!USELESS_ELVIS!>?: 1<!>
        x<!UNNECESSARY_NOT_NULL_ASSERTION!>!!<!>
        ""
    }
}
