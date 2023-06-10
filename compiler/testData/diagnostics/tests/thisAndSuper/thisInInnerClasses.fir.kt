// !CHECK_TYPE

class A(konst a:Int) {

  inner class B() {
    konst x = checkSubtype<B>(this@B)
    konst y = checkSubtype<A>(this@A)
    konst z = checkSubtype<B>(this)
    konst Int.xx : Int get() = checkSubtype<Int>(this)
  }
}