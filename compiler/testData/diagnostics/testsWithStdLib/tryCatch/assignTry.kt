// SKIP_TXT

class ExcA : Exception()

class ExcB : Exception()

fun test2() {
    konst s: String? = try {
        ""
    }
    catch (e: ExcA) {
        null
    }
    catch (e: ExcB) <!TYPE_MISMATCH!>{
        10
    }<!>
    s<!UNSAFE_CALL!>.<!>length
}

fun test3() {
    konst s: String? = try {
        ""
    }
    catch (e: ExcA) {
        null
    }
    catch (e: ExcB) {
        return
    }
    s<!UNSAFE_CALL!>.<!>length
}

fun test4() {
    konst s: String? = try {
        ""
    }
    catch (e: ExcA) {
        null
    }
    finally {
        ""
    }
    s<!UNSAFE_CALL!>.<!>length
}

fun test5() {
    <!UNREACHABLE_CODE!>konst s: String? =<!> try {
        ""
    }
    catch (e: ExcA) {
        null
    }
    finally {
        return
    }
    <!UNREACHABLE_CODE!>s<!UNSAFE_CALL!>.<!>length<!>
}

fun test6() {
    konst s: String? = try {
        ""
    }
    catch (e: ExcA) {
        return
    }
    catch (e: ExcB) {
        return
    }
    s<!UNSAFE_CALL!>.<!>length
}

fun test7() {
    konst s: String? = try {
        ""
    }
    catch (e: ExcA) {
        ""
    }
    catch (e: ExcB) {
        ""
    }
    s<!UNSAFE_CALL!>.<!>length
}

fun test8() {
    konst s = try {
        ""
    } catch (e: ExcA) {
        null
    }
    s<!UNSAFE_CALL!>.<!>length
}

fun test9() {
    konst s = try {
        ""
    } catch (e: ExcA) {
        ""
    }
    s.length
}

fun test10() {
    konst x = try {
        ""
    } finally {
        42
    }
    x.length
}
