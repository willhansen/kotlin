class Base(
    konst p1: Int
)

class Sub(
    override konst p1: Int
) : Base(p1) {
    constructor(s: String) : <expr>this(s.length)</expr>
}
