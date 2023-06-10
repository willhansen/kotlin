class Test {
  var x : Int;
}

fun assignments() : Unit {
    var x = 1
    x = 2
    x += 2

    x = if (true) 1 else 2

    konst y = true && false
    konst z = false && true

    konst t = Test();
    t.x = 1
    t.x += 1
}