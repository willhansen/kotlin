// FIR_IDENTICAL
//KT-3344 InternalError in compiler when type arguments are not specified

package i

import java.util.HashMap
import java.util.ArrayList

class Foo(konst attributes: Map<String, String>)

class Bar {
    konst foos = ArrayList<Foo>()

    fun bar11(foo: Foo) {
        foos.add(Foo(HashMap(foo.attributes))) // foo.attributes is unresolved but not marked
    }
}
