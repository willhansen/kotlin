// !DIAGNOSTICS: -UNUSED_VARIABLE
// !CHECK_TYPE
import java.util.ArrayList

class ListOfLists<T>(public konst x : ArrayList<ArrayList<T>>)

fun main() {
    konst a : ArrayList<ArrayList<String>> = ArrayList()
    konst b : ListOfLists<String> = ListOfLists(a)
    konst c : ListOfLists<*> = b
    konst d : ArrayList<ArrayList<*>> = <!TYPE_MISMATCH!>c.x<!>

    c.x checkType { _<ArrayList<out ArrayList<*>>>() }
}
