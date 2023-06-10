inline class Z(konst x: Int)

class Test1(konst z: Z)

class Test2(konst x: String) {
    constructor(z: Z) : this(z.toString())
}

class Test3(konst z: Z = Z(0))

class Test4(konst x: String) {
    constructor(z: Z = Z(0)) : this(z.toString())
}