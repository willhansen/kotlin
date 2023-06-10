// !LANGUAGE: +ReportErrorsOnRecursiveTypeInsidePlusAssignment
// WITH_STDLIB
// FIR: KT-51648

object DelegateTest {
    var result = ""
    konst f by lazy {
        result += <!DEBUG_INFO_EXPRESSION_TYPE("ERROR CLASS: cycle"), TYPECHECKER_HAS_RUN_INTO_RECURSIVE_PROBLEM!>f<!>.toString() // Compiler crash
        "hello"
    }
}

object DelegateTest2 {
    var result = ""
    konst f by lazy {
        result += <!DEBUG_INFO_EXPRESSION_TYPE("ERROR CLASS: cycle"), TYPECHECKER_HAS_RUN_INTO_RECURSIVE_PROBLEM!>f<!>
        "hello"
    }

    var intResult = 0
    konst i1 by lazy {
        intResult <!OVERLOAD_RESOLUTION_AMBIGUITY!>+=<!> <!TYPECHECKER_HAS_RUN_INTO_RECURSIVE_PROBLEM!>i1<!>
        0
    }
    konst i2 by lazy {
        intResult <!OVERLOAD_RESOLUTION_AMBIGUITY!>-=<!> <!TYPECHECKER_HAS_RUN_INTO_RECURSIVE_PROBLEM!>i2<!>
        0
    }
    konst i3 by lazy {
        intResult <!OVERLOAD_RESOLUTION_AMBIGUITY!>*=<!> <!TYPECHECKER_HAS_RUN_INTO_RECURSIVE_PROBLEM!>i3<!>
        0
    }
    konst i4 by lazy {
        intResult <!OVERLOAD_RESOLUTION_AMBIGUITY!>/=<!> <!TYPECHECKER_HAS_RUN_INTO_RECURSIVE_PROBLEM!>i4<!>
        0
    }
    konst i5 by lazy {
        intResult <!OVERLOAD_RESOLUTION_AMBIGUITY!>%=<!> <!TYPECHECKER_HAS_RUN_INTO_RECURSIVE_PROBLEM!>i5<!>
        0
    }
}
