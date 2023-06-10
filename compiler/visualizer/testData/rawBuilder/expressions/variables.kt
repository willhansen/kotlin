fun foo() {
//      Int Int
//      │   │
    konst x = 1
//          konst foo.x: Int
//          │ fun (Int).plus(Int): Int
//      Int │ │ Int
//      │   │ │ │
    var y = x + 1
//          var foo.y: Int
//          │ fun (Int).times(Int): Int
//      Int │ │ Int
//      │   │ │ │
    konst z = y * 2
//  var foo.y: Int
//  │   var foo.y: Int
//  │   │ fun (Int).plus(Int): Int
//  │   │ │ konst foo.z: Int
//  │   │ │ │
    y = y + z
//          var foo.y: Int
//          │ fun (Int).minus(Int): Int
//      Int │ │ konst foo.x: Int
//      │   │ │ │
    konst w = y - x
//         konst foo.w: Int
//         │
    return w
}
