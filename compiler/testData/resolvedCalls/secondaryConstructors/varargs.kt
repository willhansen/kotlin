class A {
    constructor(vararg x: Int) {}
}

konst y = <caret>A(0, *intArrayOf(1, 2, 3), 4))