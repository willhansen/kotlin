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
        <!BUILDER_INFERENCE_STUB_RECEIVER!>get()<!>?.test()
        <!BUILDER_INFERENCE_STUB_RECEIVER!>get()<!>?.test2()
        <!BUILDER_INFERENCE_STUB_RECEIVER!>get()<!>.test2()
        get()?.hashCode()
        get()?.equals(1)
        // there is `String?.equals` extension
        <!TYPE_MISMATCH("Any; Nothing?")!>get()<!>.equals("")
    }
    konst ret2 = build {
        emit(1)
        emit(null)
        <!BUILDER_INFERENCE_STUB_RECEIVER!>get()<!>?.test()
        <!BUILDER_INFERENCE_STUB_RECEIVER!>get()<!>?.test2()
        <!BUILDER_INFERENCE_STUB_RECEIVER!>get()<!>.test2()
        get()?.hashCode()
        get()?.equals(1)
        konst x = get()
        x?.hashCode()
        x?.equals(1)
        <!TYPE_MISMATCH("Any; Nothing?")!>x<!>.equals("")
    }
    konst ret3 = build {
        emit(1)
        emit(null)
        <!BUILDER_INFERENCE_STUB_RECEIVER!>get()<!>?.test()
        <!BUILDER_INFERENCE_STUB_RECEIVER!>get()<!>?.test2()
        <!BUILDER_INFERENCE_STUB_RECEIVER!>get()<!>.test2()
        get()?.hashCode()
        get()?.equals(1)
        konst x = get()
        x?.hashCode()
        x?.equals(1)

        if (get() == null) {}
        if (get() === null) {}

        if (x != null) {
            x<!UNNECESSARY_SAFE_CALL!>?.<!>hashCode()
            x<!UNNECESSARY_SAFE_CALL!>?.<!>equals(1)
            x.equals("")
            x.hashCode()
            x.toString()
            <!BUILDER_INFERENCE_STUB_RECEIVER!>x<!>.test()
            <!BUILDER_INFERENCE_STUB_RECEIVER!>x<!><!UNNECESSARY_SAFE_CALL!>?.<!>test2()
            <!BUILDER_INFERENCE_STUB_RECEIVER!>x<!>.test2()
        }

        ""
    }
    konst ret4 = build {
        emit(1)
        emit(null)
        konst x = get()
        if (x == null) {
            <!TYPE_MISMATCH("Any; Nothing?")!>x<!>.hashCode()
        }

        ""
    }
    konst ret401 = build {
        emit(1)
        emit(null)
        konst x = get()
        if (x == null) {
            <!TYPE_MISMATCH("Any; Nothing?")!>x<!>.equals("")
        }

        ""
    }
    konst ret402 = build {
        emit(1)
        emit(null)
        konst x = get()
        if (x == null) {
            x.<!DEBUG_INFO_ELEMENT_WITH_ERROR_TYPE, NONE_APPLICABLE!>toString<!>("")
        }

        ""
    }
    konst ret403 = build {
        emit(1)
        emit(null)
        konst x = get()
        if (x == null) {
            <!BUILDER_INFERENCE_STUB_RECEIVER, TYPE_MISMATCH("Any; Nothing?")!>x<!>.test()
        }

        ""
    }
    konst ret404 = build {
        emit(1)
        emit(null)
        konst x = get()
        if (x === null) {
            <!TYPE_MISMATCH("Any; Nothing?")!>x<!>.hashCode()
        }

        ""
    }
    konst ret405 = build {
        emit(1)
        emit(null)
        konst x = get()
        if (x === null) {
            <!TYPE_MISMATCH("Any; Nothing?")!>x<!>.equals("")
        }

        ""
    }
    konst ret406 = build {
        emit(1)
        emit(null)
        konst x = get()
        if (x === null) {
            x.<!DEBUG_INFO_ELEMENT_WITH_ERROR_TYPE, NONE_APPLICABLE!>toString<!>("")
        }

        ""
    }
    konst ret407 = build {
        emit(1)
        emit(null)
        konst x = get()
        if (x === null) {
            <!BUILDER_INFERENCE_STUB_RECEIVER, TYPE_MISMATCH("Any; Nothing?")!>x<!>.test()
        }

        ""
    }
    konst ret408 = build {
        emit(1)
        emit(null)
        konst x = get()
        <!BUILDER_INFERENCE_STUB_RECEIVER, TYPE_MISMATCH("Any; Nothing?")!>x<!>.test()

        ""
    }
    konst ret41 = build {
        emit(1)
        emit(null)
        <!BUILDER_INFERENCE_STUB_RECEIVER!>get()<!>?.test()
        <!BUILDER_INFERENCE_STUB_RECEIVER!>get()<!>?.test2()
        <!BUILDER_INFERENCE_STUB_RECEIVER!>get()<!>.test2()
        get()?.hashCode()
        get()?.equals(1)
        konst x = get()
        x?.hashCode()
        x?.equals(1)

        if (get() == null) {}
        if (get() === null) {}

        if (x == null) {
            <!DEBUG_INFO_CONSTANT!>x<!>?.hashCode()
        }

        if (x == null) {
            <!DEBUG_INFO_CONSTANT!>x<!>?.equals(1)
        }

        if (x == null) {
            <!BUILDER_INFERENCE_STUB_RECEIVER, DEBUG_INFO_CONSTANT!>x<!>?.test2()
        }

        if (x == null) {
            <!BUILDER_INFERENCE_STUB_RECEIVER!>x<!>.test2()
        }

        if (x === null) {
            <!DEBUG_INFO_CONSTANT!>x<!>?.hashCode()
        }

        if (x === null) {
            <!DEBUG_INFO_CONSTANT!>x<!>?.equals(1)
        }

        if (x === null) {
            <!BUILDER_INFERENCE_STUB_RECEIVER, DEBUG_INFO_CONSTANT!>x<!>?.test2()
        }

        if (x === null) {
            <!BUILDER_INFERENCE_STUB_RECEIVER!>x<!>.test2()
        }

        ""
    }
    konst ret5 = build {
        emit(1)
        emit(null)
        konst x = get()
        if (x == null) {
            <!TYPE_MISMATCH("Any; Nothing?")!>x<!>.equals("")
        }

        ""
    }
    konst ret501 = build {
        emit(1)
        emit(null)
        konst x = get()
        if (x == null) {
            <!TYPE_MISMATCH("Any; Nothing?")!>x<!>.hashCode()
        }
        ""
    }
    konst ret502 = build {
        emit(1)
        emit(null)
        konst x = get()
        if (x == null) {
            <!TYPE_MISMATCH("Any; Nothing?")!>x<!>.toString()
        }
        ""
    }
    konst ret503 = build {
        emit(1)
        emit(null)
        konst x = get()
        if (x == null) {
            <!BUILDER_INFERENCE_STUB_RECEIVER, TYPE_MISMATCH("Any; Nothing?")!>x<!>.test()
        }
        ""
    }
    konst ret504 = build {
        emit(1)
        emit(null)
        konst x = get()
        if (x === null) {
            <!TYPE_MISMATCH("Any; Nothing?")!>x<!>.equals("")
        }

        ""
    }
    konst ret505 = build {
        emit(1)
        emit(null)
        konst x = get()
        if (x === null) {
            <!TYPE_MISMATCH("Any; Nothing?")!>x<!>.hashCode()
        }
        ""
    }
    konst ret506 = build {
        emit(1)
        emit(null)
        konst x = get()
        if (x === null) {
            <!TYPE_MISMATCH("Any; Nothing?")!>x<!>.toString()
        }
        ""
    }
    konst ret507 = build {
        emit(1)
        emit(null)
        konst x = get()
        if (x === null) {
            <!BUILDER_INFERENCE_STUB_RECEIVER, TYPE_MISMATCH("Any; Nothing?")!>x<!>.test()
        }
        ""
    }
    konst ret508 = build {
        emit(1)
        emit(null)
        konst x = get()
        <!BUILDER_INFERENCE_STUB_RECEIVER, TYPE_MISMATCH("Any; Nothing?")!>x<!>.test()
        ""
    }
    konst ret51 = build {
        emit(1)
        emit(null)
        <!BUILDER_INFERENCE_STUB_RECEIVER!>get()<!>?.test()
        <!BUILDER_INFERENCE_STUB_RECEIVER!>get()<!>?.test2()
        <!BUILDER_INFERENCE_STUB_RECEIVER!>get()<!>.test2()
        get()?.hashCode()
        get()?.equals(1)
        konst x = get()
        x?.hashCode()
        x?.equals(1)

        if (get() == null) {}
        if (get() === null) {}

        if (x == null) {
            <!DEBUG_INFO_CONSTANT!>x<!>?.hashCode()
            <!DEBUG_INFO_CONSTANT!>x<!>?.equals(1)
            <!BUILDER_INFERENCE_STUB_RECEIVER, DEBUG_INFO_CONSTANT!>x<!>?.test2()
            <!BUILDER_INFERENCE_STUB_RECEIVER!>x<!>.test2()
        }

        ""
    }
}
