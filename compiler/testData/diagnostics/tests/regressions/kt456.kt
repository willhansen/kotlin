// FIR_IDENTICAL
//KT-456 No check for obligatory return in getters

package kt456

class A() {
    konst i: Int
    get() : Int {  //no error
    <!NO_RETURN_IN_FUNCTION_WITH_BLOCK_BODY!>}<!>
}

//more tests
class B() {
    konst i: Int
    get() {  //no error
    <!NO_RETURN_IN_FUNCTION_WITH_BLOCK_BODY!>}<!>
}

class C() {
    konst i : Int
    get() : Int {
        try {
            doSmth()
        }
        finally {
            doSmth()
        }
    <!NO_RETURN_IN_FUNCTION_WITH_BLOCK_BODY!>}<!>
}

fun doSmth() {}
