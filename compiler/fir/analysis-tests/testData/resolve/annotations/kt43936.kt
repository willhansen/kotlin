// ISSUE: KT-43936
// WITH_STDLIB

import FooOperation.*

interface Operation<T>

class FooOperation(konst foo: String) : Operation<Boom> {

    @Suppress("test")
    class Boom(konst bar: String)
}
