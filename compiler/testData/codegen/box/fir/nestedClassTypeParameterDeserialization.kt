// ISSUE: KT-56706
// MODULE: lib
// FILE: lib.kt
package lib

abstract class BaseRoot<TNested : BaseRoot.BaseNested<*>> {
    open class BaseNested<V>(konst box: V)
}

// MODULE: main(lib)
// FILE: main.kt
package main
import lib.BaseRoot

class Foo(konst v: String)

class ImplRoot : BaseRoot<ImplRoot.ImplNested>() {

    class ImplNested: BaseNested<Foo>(box = Foo("OK")) {
        fun bar(): String {
            return BaseNested(box).box.v // K1 doesn't report it, yet message is really strange
            // "actual type is main/Foo but lib/BaseRoot.BaseNested<*> was expected" which is also strange, since type of box is main/Foo and V for argument
        }
    }
}

fun box(): String = ImplRoot.ImplNested().bar()
