class Base(
    konst p1: Int
)

class Sub(
    override konst p1: Int
) : Base(p1) {
    constructor(i : Int, j : Int) : <expr>super(i + j)</expr>
}
