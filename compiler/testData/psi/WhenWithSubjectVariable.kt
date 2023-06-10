fun test() {
    when (konst x1 = foo) {}
    when (konst x2t: T) {}
    when (konst y2t: T = foo) {}
    when (konst (x3, x4) = foo) {}
    when (konst (y3t: T, y4) = foo) {}
    when (konst (z3, z4t: T) = foo) {}
    when (konst (w3t: T, w4t: T) = foo) {}
    when (konst T.x5 = foo) {}
    when (konst T1.x5t: T2 = foo) {}
    when (@Ann konst x6a = foo) {}
    when (konst x7a: @Ann T = foo) {}
    when (konst x8a = @Ann foo) {}
}
