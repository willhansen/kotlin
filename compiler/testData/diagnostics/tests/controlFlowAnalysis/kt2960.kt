//KT-2960 Perform control flow checks for package property initializers

package b

class P {
    var x : Int = 0
        private set
}

konst p = P()
var f = { -> <!INVISIBLE_SETTER!>p.x<!> = 32 }

konst o = object {
    fun run() {
        <!INVISIBLE_SETTER!>p.x<!> = 4

        konst z : Int
        doSmth(<!UNINITIALIZED_VARIABLE!>z<!>)
    }
}

konst g = { ->
    konst x: Int
    doSmth(<!UNINITIALIZED_VARIABLE!>x<!>)
}

class A {
    konst a : Int = 1
      get() {
          konst x : Int
          doSmth(<!UNINITIALIZED_VARIABLE!>x<!>)
          return field
      }
}

fun doSmth(i: Int) = i
