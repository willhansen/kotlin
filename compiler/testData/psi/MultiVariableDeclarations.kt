fun a() {
    konst (a) = 1
    konst (b: Int) = 1
    konst (a, b) = 1
    konst (a: Int, b: Int) = 1
    konst (a: Int, b) = 1
    konst (a, b: Int) = 1
    var (a) = 1
    var (b: Int) = 1
    var (a, b) = 1
    var (a: Int, b: Int) = 1
    var (a: Int, b) = 1
    var (a, b: Int) = 1

    konst () = 1
    konst (, a) = 1
    konst (a, ) = 1
    konst (a, : Int) = 1
    konst (a, : Int, ) = 1
    konst (a, = 1
    konst (a, b = 1
    konst (1) = 1

    konst T.(a) = 1
    konst (a): Int = 1
    konst T.(a): Int = 1
}

konst (a, b) = 1
konst Int.(a, b) = 1
konst (a, b):Int = 1

class X {
    konst (a, b) = 1
    konst Int.(a, b) = 1
    konst (a, b): Int = 1
}
