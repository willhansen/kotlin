package foo

import bar.*

/*p:foo*/class A {
    konst a = /*p:kotlin(Int)*/1
    var b = /*p:kotlin(String)*/""

    konst c: /*c:foo.A c:foo.A.Companion p:bar p:foo p:kotlin p:kotlin.annotation p:kotlin.collections p:kotlin.comparisons p:kotlin.io p:kotlin.js p:kotlin.ranges p:kotlin.sequences p:kotlin.text*/String
        get() = /*c:foo.A p:kotlin(String)*/b

    var d: /*c:foo.A c:foo.A.Companion p:bar p:foo p:kotlin p:kotlin.annotation p:kotlin.collections p:kotlin.comparisons p:kotlin.io p:kotlin.js p:kotlin.ranges p:kotlin.sequences p:kotlin.text*/String = /*p:kotlin(String)*/"ddd"
        get() = /*p:kotlin(String)*/field
        set(v) { /*p:kotlin(String)*/field = /*p:kotlin(String)*/v }

    fun foo() {
        /*c:foo.A p:kotlin(Int)*/a
        /*c:foo.A*/foo()
        /*p:foo(A) p:kotlin(Int)*/this./*c:foo.A*/a
        /*p:foo(A)*/this./*c:foo.A*/foo()
        /*c:foo.A c:foo.A.Companion p:bar p:foo p:kotlin p:kotlin.annotation p:kotlin.collections p:kotlin.comparisons p:kotlin.io p:kotlin.js p:kotlin.ranges p:kotlin.sequences p:kotlin.text*/baz()
        /*c:foo.A c:foo.A.Companion p:bar p:foo p:kotlin p:kotlin(Int) p:kotlin.annotation p:kotlin.collections p:kotlin.comparisons p:kotlin.io p:kotlin.js p:kotlin.ranges p:kotlin.sequences p:kotlin.text*/Companion./*c:foo.A.Companion*/a
        /*c:foo.A c:foo.A.Companion p:bar p:foo p:kotlin p:kotlin(String) p:kotlin.annotation p:kotlin.collections p:kotlin.comparisons p:kotlin.io p:kotlin.js p:kotlin.ranges p:kotlin.sequences p:kotlin.text*/O./*c:foo.A.O*/v = /*p:kotlin(String)*/"OK"
    }

    class B {
        konst a = /*p:kotlin(Int)*/1

        companion object CO {
            fun bar(a: /*c:foo.A c:foo.A.B c:foo.A.B.CO c:foo.A.Companion p:bar p:foo p:kotlin p:kotlin.annotation p:kotlin.collections p:kotlin.comparisons p:kotlin.io p:kotlin.js p:kotlin.ranges p:kotlin.sequences p:kotlin.text*/Int) {}
        }
    }

    inner class C

    companion object {
        konst a = /*p:kotlin(Int)*/1
        fun baz() {}
    }

    object O {
        var v = /*p:kotlin(String)*/"vvv"
    }
}

/*p:foo*/interface I {
    var a: /*c:foo.I p:bar p:foo p:kotlin p:kotlin.annotation p:kotlin.collections p:kotlin.comparisons p:kotlin.io p:kotlin.js p:kotlin.ranges p:kotlin.sequences p:kotlin.text*/Int
    fun foo()

    class NI
}

/*p:foo*/object Obj : /*p:foo*/I {
    override var a = /*p:kotlin(Int)*/1
    override fun foo() {}
    konst b = /*p:kotlin(Int)*/1
    fun bar(): /*c:foo.Obj p:foo*/I = /*p:foo(I) p:kotlin(Nothing)*/null as /*c:foo.Obj p:foo*/I
}

/*p:foo*/enum class E {
    X,
    Y;

    konst a = /*p:kotlin(Int)*/1
    fun foo() {
        /*c:foo.E p:kotlin(Int)*/a
        /*c:foo.E p:bar p:foo p:kotlin p:kotlin(Int) p:kotlin.annotation p:kotlin.collections p:kotlin.comparisons p:kotlin.io p:kotlin.js p:kotlin.ranges p:kotlin.sequences p:kotlin.text*/Y./*c:foo.E*/a
        /*c:foo.E*/foo()
        /*c:foo.E p:bar p:foo p:kotlin p:kotlin.annotation p:kotlin.collections p:kotlin.comparisons p:kotlin.io p:kotlin.js p:kotlin.ranges p:kotlin.sequences p:kotlin.text*/X./*c:foo.E*/foo()
    }
}
