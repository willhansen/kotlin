// !CHECK_TYPE

class A(konst a:Int) {

  inner class B() {
    konst x = checkSubtype<B>(<!DEBUG_INFO_LEAKING_THIS!>this@B<!>)
    konst y = checkSubtype<A>(this@A)
    konst z = checkSubtype<B>(<!DEBUG_INFO_LEAKING_THIS!>this<!>)
    konst Int.xx : Int get() = checkSubtype<Int>(this)
  }
}