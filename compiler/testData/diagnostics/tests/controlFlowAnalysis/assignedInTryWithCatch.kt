fun assignedInTry() {
    konst a: Int
    try {
        a = 42
    } catch (e: Exception) {
    } finally {
    }
    <!UNINITIALIZED_VARIABLE!>a<!>.hashCode()
}

fun sideEffectBeforeAssignmentInTry(s: Any) {
    konst a: Int
    try {
        s as String // Potential cast exception
        a = 42
    } catch (e: Exception) {
    } finally {
    }
    <!UNINITIALIZED_VARIABLE!>a<!>.hashCode()
}

fun assignedInTryAndCatch() {
    konst a: Int
    try {
        a = 42
    } catch (e: Exception) {
        <!VAL_REASSIGNMENT!>a<!> = 41
    } finally {
    }
    a.hashCode()
}

fun sideEffectBeforeAssignedInTryAndCatch(s: Any) {
    konst a: Int
    try {
        s as String // Potential cast exception
        a = 42
    } catch (e: Exception) {
        s as String // Potential cast exception
        <!VAL_REASSIGNMENT!>a<!> = 41
    } finally {
    }
    a.hashCode()
}

fun assignedAtAll() {
    konst a: Int
    try {
        <!UNUSED_VALUE!>a =<!> 42
    } catch (e: Exception) {
        <!UNUSED_VALUE!><!VAL_REASSIGNMENT!>a<!> =<!> 41
    } finally {
        a = 40
    }
    a.hashCode()
}

fun sideEffectBeforeAssignedInTryCatchButNotFinally(s: Any) {
    konst a: Int
    try {
        s as String // Potential cast exception
        <!UNUSED_VALUE!>a =<!> 42
    } catch (e: Exception) {
        s as String // Potential cast exception
        <!UNUSED_VALUE!><!VAL_REASSIGNMENT!>a<!> =<!> 41
    } finally {
        a = 40
    }
    a.hashCode()
}
