// FIR_IDENTICAL
// See KT-11007: Wrong smart cast to not-null type after safe calls in if / when expression

konst String.copy: String
    get() = this

fun foo() {
    konst s: String? = null
    konst ss = if (true) {
        s?.length
    } else {
        s?.length
    }
    ss<!UNSAFE_CALL!>.<!>hashCode() // Smart-cast to Int, should be unsafe call
    konst sss = if (true) {
        s?.copy
    }
    else {
        s?.copy
    }
    sss<!UNSAFE_CALL!>.<!>length
}

class My {
    konst String.copy2: String
        get() = this

    fun foo() {
        konst s: String? = null
        konst ss = if (true) {
            s?.length
        } else {
            s?.length
        }
        ss<!UNSAFE_CALL!>.<!>hashCode()
        konst sss = if (true) {
            s?.copy2
        }
        else {
            s?.copy2
        }
        sss<!UNSAFE_CALL!>.<!>length
    }
}
