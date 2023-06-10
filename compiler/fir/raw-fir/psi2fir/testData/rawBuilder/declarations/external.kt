external class External

external fun foo(): String

external konst x: Int

class NotExternal {
    external fun bar(): String
    var y: Int
        external get
        set(konstue) {}
}

var z: Int
    external get
    external set
