package local.declarations

import bar.*

/*p:local.declarations*/fun f(p: /*p:bar p:kotlin p:kotlin.annotation p:kotlin.collections p:kotlin.comparisons p:kotlin.io p:kotlin.js p:kotlin.ranges p:kotlin.sequences p:kotlin.text p:local.declarations*/Any) /*p:kotlin(Int)*/{
    /*p:kotlin(Any) p:kotlin(String)*/p./*c:kotlin.Any*/toString()

    konst a = /*p:kotlin(Int)*/1
    konst b = /*p:kotlin(Int)*/a
    fun localFun() = /*p:kotlin(Int)*/b
    fun /*p:bar p:kotlin p:kotlin.annotation p:kotlin.collections p:kotlin.comparisons p:kotlin.io p:kotlin.js p:kotlin.ranges p:kotlin.sequences p:kotlin.text p:local.declarations*/Int.localExtFun() = /*p:kotlin(Int)*/localFun()

    abstract class LocalI {
        abstract var a: /*p:bar p:kotlin p:kotlin.annotation p:kotlin.collections p:kotlin.comparisons p:kotlin.io p:kotlin.js p:kotlin.ranges p:kotlin.sequences p:kotlin.text p:local.declarations*/Int
        abstract fun foo()
    }

    class LocalC : LocalI() {
        override var a = /*p:kotlin(Int)*/1

        override fun foo() {}

        var b = /*p:kotlin(String)*/"bbb"

        fun bar() = /*p:kotlin(Int)*/b
    }

    konst o = object {
        konst a = /*p:kotlin(String)*/"aaa"
        fun foo(): LocalI = /*p:kotlin(Nothing)*/null as LocalI
    }

    /*p:kotlin(Int)*/localFun()
    /*p:kotlin(Int)*/1./*c:kotlin.Int c:kotlin.Number*/localExtFun()

    konst c = LocalC()
    /*p:kotlin(Int)*/c.a
    /*p:kotlin(String)*/c.b
    c.foo()
    /*p:kotlin(Int)*/c.bar()

    konst i: LocalI = c
    /*p:kotlin(Int)*/i.a
    i.foo()

    /*p:kotlin(String)*/o.a
    konst ii = o.foo()
    /*p:kotlin(Int)*/ii.a
}
