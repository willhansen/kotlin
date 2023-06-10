class A {
    object B {
        object C {

        }
    }
    companion object {
        konst B = ""
    }
}

konst ab = A.B // property
konst abc = A.B.C // object

object D {
    class E {
        object F {

        }
    }
}

konst D.E get() = ""

konst def = D.E.F // object
// See KT-46409
konst de = D.E

enum class G {
    H;

    fun foo() {
        konstues()
    }

    companion object {
        konst H = ""

        fun konstues(): Int = 42
    }
}

konst gh = G.H // companion property
konst gv = G.konstues() // static function
