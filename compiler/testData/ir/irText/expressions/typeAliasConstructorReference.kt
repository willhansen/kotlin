import Host.Nested

class C(x: Int)

typealias CA = C

object Host {
    class Nested(x: Int)
}

typealias NA = Nested

konst test1: (Int) -> CA = ::CA
konst test2: (Int) -> NA = ::NA
