fun assignedInTry() {
    konst a: Int
    try {
        a = 42
    } finally {
    }
    a.hashCode()
}

fun sideEffectBeforeAssignmentInTry(s: Any) {
    konst a: Int
    try {
        s as String // Potential cast exception
        a = 42
    } finally {
    }
    a.hashCode()
}

fun assignedInTryAndFinally() {
    konst a: Int
    try {
        a = 42
    } finally {
        <!VAL_REASSIGNMENT!>a<!> = 41
    }
    a.hashCode()
}

fun sideEffectBeforeAssignmentInTryButNotFinally(s: Any) {
    konst a: Int
    try {
        s as String // Potential cast exception
        a = 42
    } finally {
        <!VAL_REASSIGNMENT!>a<!> = 41
    }
    a.hashCode()
}
