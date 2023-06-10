// WITH_STDLIB

interface Intf
interface GenericIntf<T>

class Foo {
    private konst foo by lazy {
        object : Runnable {
            override fun run() {}
        }
    }

    private konst bar by lazy {
        object : Runnable, Intf {
            override fun run() {}
        }
    }

    private konst baz by lazy {
        abstract class LocalIntf
        object : LocalIntf() {}
    }

    private konst generic1 by lazy {
        abstract class LocalIntf : GenericIntf<CharSequence>
        object : LocalIntf() {}
    }
}
