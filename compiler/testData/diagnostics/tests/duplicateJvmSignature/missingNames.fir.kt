// !DIAGNOSTICS: -DUPLICATE_CLASS_NAMES
<!FUNCTION_DECLARATION_WITH_NO_NAME!>fun ()<!> {

}

<!FUNCTION_DECLARATION_WITH_NO_NAME!>fun Outer.()<!> {

}

konst<!SYNTAX!><!> : Int = 1

class<!SYNTAX!><!> {

}

object<!SYNTAX!><!> {

}

interface<!SYNTAX!><!> {

}

enum class<!SYNTAX!><!> {

}

annotation class<!SYNTAX!><!> {

}

class Outer {
    <!FUNCTION_DECLARATION_WITH_NO_NAME!>fun ()<!> {

    }

    konst<!SYNTAX!><!> : Int = 1

    class<!SYNTAX!><!> {

    }

    object<!SYNTAX!><!> {

    }

    interface<!SYNTAX!><!> {

    }

    enum class<!SYNTAX!><!> {

    }

    annotation class<!SYNTAX!><!> {

    }
}

fun outerFun() {
    fun () {

    }
    fun () {

    }
}

