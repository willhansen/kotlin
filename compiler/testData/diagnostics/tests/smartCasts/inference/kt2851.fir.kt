//KT-2851 Type inference failed passing in not-null after smart-cast konstue in Pair
package a

fun main() {
    konst konstue: String? = ""
    if (konstue != null) {
        foo(Pair("konst", konstue))
        foo(Pair("konst", konstue<!UNNECESSARY_NOT_NULL_ASSERTION!>!!<!>))
        foo(Pair<String, String>("konst", konstue))
    }
}

fun foo(map: Pair<String, String>) {}


//from library
public class Pair<out A, out B> (
        public konst first: A,
        public konst second: B
)
