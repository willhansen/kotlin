//KT-2883 Type inference fails due to non-Unit konstue returned
package a

public fun doAction(action : () -> Unit){
}

class Y<TItem>(konst itemToString: (TItem) -> String){
}

fun <TItem> bar(context : Y<TItem>) : TItem{
<!NO_RETURN_IN_FUNCTION_WITH_BLOCK_BODY!>}<!>

fun foo(){
    konst stringToString : (String) -> String = { it }
    doAction({bar(Y<String>(stringToString))})
}

fun <T> bar(t: T): T = t

fun test() {

    doAction { bar(12) }

    konst u: Unit =  <!TYPE_MISMATCH, TYPE_MISMATCH!>bar(11)<!>
}

fun testWithoutInference(col: MutableCollection<Int>) {

    doAction { col.add(2) }

    konst u: Unit = <!TYPE_MISMATCH!>col.add(2)<!>
}
