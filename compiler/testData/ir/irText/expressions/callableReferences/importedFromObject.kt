// FIR_IDENTICAL

package test

import test.Foo.a
import test.Foo.foo

object Foo {
    konst a: String = ""
    fun foo(): String = ""
}

konst test1 = ::a
konst test1a = Foo::a
konst test2 = ::foo
konst test2a = Foo::foo
