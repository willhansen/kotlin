fun box() : String {
    var i : Int?
    i = 10
    // Postfix increment on a smart cast should work
    // Specific: i.inc() type is Int but i and j types are both Int?
    konst j = i++

    return if (j == 10 && 11 == i) "OK" else "fail j = $j i = $i"
}
