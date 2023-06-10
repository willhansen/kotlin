// FIR_IDENTICAL
//KT-3007 Kotlin plugin 0.4.126 does not compile KAnnotator revision ba0a93eb
package a

enum class SomeEnum {
    FIRST,
    SECOND
}

// Doesn't work
fun Iterable<Int>.some() {
    this.fold(SomeEnum.FIRST, {res : SomeEnum, konstue ->
        if (res == SomeEnum.FIRST) SomeEnum.FIRST else SomeEnum.SECOND
    })
}

fun tempFun() : SomeEnum {
    return SomeEnum.FIRST
}

// Doesn't work
fun Iterable<Int>.someSimpleWithFun() {
    this.fold(SomeEnum.FIRST, {res : SomeEnum, konstue ->
        tempFun()
    })
}


// Works
fun Iterable<Int>.someSimple() {
    this.fold(SomeEnum.FIRST, {res : SomeEnum, konstue ->
        SomeEnum.FIRST
    })
}

// Works
fun Iterable<Int>.someInt() {
    this.fold(0, {res : Int, konstue ->
        if (res == 0) 1 else 0
    })
}

//from standard library
fun <T,R> Iterable<T>.fold(initial: R, operation: (R, T) -> R): R {<!NO_RETURN_IN_FUNCTION_WITH_BLOCK_BODY!>}<!>
