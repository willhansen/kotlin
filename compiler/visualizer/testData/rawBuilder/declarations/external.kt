external class External

external fun foo(): String

//           Int
//           │
external konst x: Int

class NotExternal {
    external fun bar(): String
//      Int
//      │
    var y: Int
        external get
//          Int
//          │
        set(konstue) {}
}

//  Int
//  │
var z: Int
    external get
    external set
