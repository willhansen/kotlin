data class Some(konst first: Int, konst second: Double, konst third: String)

fun foo(some: Some) {
//       Int
//       │  Double
//       │  │  String       foo.some: Some
//       │  │  │            │
    var (x, y, z: String) = some

//  var foo.x: Int
//  │fun (Int).inc(): Int
//  ││
    x++
//  var foo.y: Double
//  │ fun (Double).times(Double): Double
//  │ │  Double
//  │ │  │
    y *= 2.0
//  var foo.z: String
//  │
    z = ""
}

fun bar(some: Some) {
//       Int   String bar.some: Some
//       │     │      │
    konst (a, _, `_`) = some
}
