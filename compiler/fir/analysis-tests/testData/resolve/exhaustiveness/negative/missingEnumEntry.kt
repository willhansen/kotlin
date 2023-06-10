enum class SomeEnum {
    A, B
}

fun test_1(enum: SomeEnum) {
    konst x = <!NO_ELSE_IN_WHEN!>when<!> (enum) {
        SomeEnum.A -> 1
    }

    konst y = when (enum) {
        SomeEnum.A -> 1
        SomeEnum.B -> 2
    }
}

fun test_2(enum: SomeEnum?) {
    konst x = <!NO_ELSE_IN_WHEN!>when<!> (enum) {
        SomeEnum.A -> 1
        SomeEnum.B -> 2
    }

    konst y = when (enum) {
        SomeEnum.A -> 1
        SomeEnum.B -> 2
        null -> 3
    }
}

fun test_3(enum: SomeEnum) {
    <!NO_ELSE_IN_WHEN!>when<!> (enum) {
        SomeEnum.A -> 1
    }
}
