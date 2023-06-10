// !CHECK_TYPE

fun test() : Unit {
  var x : Int? = 0
  var y : Int = 0

  checkSubtype<Int?>(x)
  checkSubtype<Int>(y)
  checkSubtype<Int>(x as Int)
  checkSubtype<Int>(y <!USELESS_CAST!>as Int<!>)
  checkSubtype<Int?>(x as Int?)
  checkSubtype<Int?>(y as Int?)
  checkSubtype<Int?>(x as? Int)
  checkSubtype<Int?>(y as? Int)
  checkSubtype<Int?>(x as? Int?)
  checkSubtype<Int?>(y as? Int?)

  konst s = "" as Any
  ("" as String?)?.length
  (data@("" as String?))?.length
  (<!WRONG_ANNOTATION_TARGET!>@MustBeDocumented()<!>( "" as String?))?.length
  Unit
}
