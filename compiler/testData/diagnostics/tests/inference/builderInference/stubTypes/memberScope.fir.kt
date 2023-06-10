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

fun Any.test() {}
fun Any?.test2() {}

fun test() {
    konst ret1 = build {
        emit(1)
        emit(null)
        get()?.test()
        get()?.test2()
        get().test2()
        get()?.hashCode()
        get()?.<!NONE_APPLICABLE!>equals<!>(1)
        // there is `String?.equals` extension
        get().equals("")
    }
    konst ret2 = build {
        emit(1)
        emit(null)
        get()?.test()
        get()?.test2()
        get().test2()
        get()?.hashCode()
        get()?.<!NONE_APPLICABLE!>equals<!>(1)
        konst x = get()
        x?.hashCode()
        x?.<!NONE_APPLICABLE!>equals<!>(1)
        x.equals("")
    }
    konst ret3 = build {
        emit(1)
        emit(null)
        get()?.test()
        get()?.test2()
        get().test2()
        get()?.hashCode()
        get()?.<!NONE_APPLICABLE!>equals<!>(1)
        konst x = get()
        x?.hashCode()
        x?.<!NONE_APPLICABLE!>equals<!>(1)

        if (get() == null) {}
        if (<!FORBIDDEN_IDENTITY_EQUALS_WARNING!>get() === null<!>) {}

        if (x != null) {
            x<!UNNECESSARY_SAFE_CALL!>?.<!>hashCode()
            x<!UNNECESSARY_SAFE_CALL!>?.<!>equals(1)
            x.equals("")
            x.hashCode()
            x.toString()
            x.test()
            x<!UNNECESSARY_SAFE_CALL!>?.<!>test2()
            x.test2()
        }

        ""
    }
    konst ret4 = build {
        emit(1)
        emit(null)
        konst x = get()
        if (x == null) {
            x.hashCode()
        }

        ""
    }
    konst ret401 = build {
        emit(1)
        emit(null)
        konst x = get()
        if (x == null) {
            x.equals("")
        }

        ""
    }
    konst ret402 = build {
        emit(1)
        emit(null)
        konst x = get()
        if (x == null) {
            x.<!NONE_APPLICABLE!>toString<!>("")
        }

        ""
    }
    konst ret403 = build {
        emit(1)
        emit(null)
        konst x = get()
        if (x == null) {
            x<!UNSAFE_CALL!>.<!>test()
        }

        ""
    }
    konst ret404 = build {
        emit(1)
        emit(null)
        konst x = get()
        if (<!FORBIDDEN_IDENTITY_EQUALS_WARNING!>x === null<!>) {
            x.hashCode()
        }

        ""
    }
    konst ret405 = build {
        emit(1)
        emit(null)
        konst x = get()
        if (<!FORBIDDEN_IDENTITY_EQUALS_WARNING!>x === null<!>) {
            x.equals("")
        }

        ""
    }
    konst ret406 = build {
        emit(1)
        emit(null)
        konst x = get()
        if (<!FORBIDDEN_IDENTITY_EQUALS_WARNING!>x === null<!>) {
            x.<!NONE_APPLICABLE!>toString<!>("")
        }

        ""
    }
    konst ret407 = build {
        emit(1)
        emit(null)
        konst x = get()
        if (<!FORBIDDEN_IDENTITY_EQUALS_WARNING!>x === null<!>) {
            x<!UNSAFE_CALL!>.<!>test()
        }

        ""
    }
    konst ret408 = build {
        emit(1)
        emit(null)
        konst x = get()
        x.test()

        ""
    }
    konst ret41 = build {
        emit(1)
        emit(null)
        get()?.test()
        get()?.test2()
        get().test2()
        get()?.hashCode()
        get()?.<!NONE_APPLICABLE!>equals<!>(1)
        konst x = get()
        x?.hashCode()
        x?.<!NONE_APPLICABLE!>equals<!>(1)

        if (get() == null) {}
        if (<!FORBIDDEN_IDENTITY_EQUALS_WARNING!>get() === null<!>) {}

        if (x == null) {
            x?.hashCode()
        }

        if (x == null) {
            x?.<!NONE_APPLICABLE!>equals<!>(1)
        }

        if (x == null) {
            x?.test2()
        }

        if (x == null) {
            x.test2()
        }

        if (<!FORBIDDEN_IDENTITY_EQUALS_WARNING!>x === null<!>) {
            x?.hashCode()
        }

        if (<!FORBIDDEN_IDENTITY_EQUALS_WARNING!>x === null<!>) {
            x?.<!NONE_APPLICABLE!>equals<!>(1)
        }

        if (<!FORBIDDEN_IDENTITY_EQUALS_WARNING!>x === null<!>) {
            x?.test2()
        }

        if (<!FORBIDDEN_IDENTITY_EQUALS_WARNING!>x === null<!>) {
            x.test2()
        }

        ""
    }
    konst ret5 = build {
        emit(1)
        emit(null)
        konst x = get()
        if (x == null) {
            x.equals("")
        }

        ""
    }
    konst ret501 = build {
        emit(1)
        emit(null)
        konst x = get()
        if (x == null) {
            x.hashCode()
        }
        ""
    }
    konst ret502 = build {
        emit(1)
        emit(null)
        konst x = get()
        if (x == null) {
            x.toString()
        }
        ""
    }
    konst ret503 = build {
        emit(1)
        emit(null)
        konst x = get()
        if (x == null) {
            x<!UNSAFE_CALL!>.<!>test()
        }
        ""
    }
    konst ret504 = build {
        emit(1)
        emit(null)
        konst x = get()
        if (<!FORBIDDEN_IDENTITY_EQUALS_WARNING!>x === null<!>) {
            x.equals("")
        }

        ""
    }
    konst ret505 = build {
        emit(1)
        emit(null)
        konst x = get()
        if (<!FORBIDDEN_IDENTITY_EQUALS_WARNING!>x === null<!>) {
            x.hashCode()
        }
        ""
    }
    konst ret506 = build {
        emit(1)
        emit(null)
        konst x = get()
        if (<!FORBIDDEN_IDENTITY_EQUALS_WARNING!>x === null<!>) {
            x.toString()
        }
        ""
    }
    konst ret507 = build {
        emit(1)
        emit(null)
        konst x = get()
        if (<!FORBIDDEN_IDENTITY_EQUALS_WARNING!>x === null<!>) {
            x<!UNSAFE_CALL!>.<!>test()
        }
        ""
    }
    konst ret508 = build {
        emit(1)
        emit(null)
        konst x = get()
        x.test()
        ""
    }
    konst ret51 = build {
        emit(1)
        emit(null)
        get()?.test()
        get()?.test2()
        get().test2()
        get()?.hashCode()
        get()?.<!NONE_APPLICABLE!>equals<!>(1)
        konst x = get()
        x?.hashCode()
        x?.<!NONE_APPLICABLE!>equals<!>(1)

        if (get() == null) {}
        if (<!FORBIDDEN_IDENTITY_EQUALS_WARNING!>get() === null<!>) {}

        if (x == null) {
            x?.hashCode()
            x?.<!NONE_APPLICABLE!>equals<!>(1)
            x?.test2()
            x.test2()
        }

        ""
    }
}
