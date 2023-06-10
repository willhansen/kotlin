package org.jetbrains.kotlin.test

//  Int  Int
//  │    │
konst p1 = 10
//  Double       Double
//  │            │
konst p2: Double = 1.0
//  Float       Float
//  │           │
konst p3: Float = 2.5f
//  String
//  │
konst p4 = "some string"

//  Double
//  │    konst p1: Int
//  │    │  fun (Int).plus(Double): Double
//  │    │  │ konst p2: Double
//  │    │  │ │
konst p5 = p1 + p2
//  Double
//  │    konst p1: Int
//  │    │  fun (Int).times(Double): Double
//  │    │  │ konst p2: Double
//  │    │  │ │  fun (Double).plus(Double): Double
//  │    │  │ │  │  konst p5: Double
//  │    │  │ │  │  │  fun (Double).minus(Float): Double
//  │    │  │ │  │  │  │ konst p3: Float
//  │    │  │ │  │  │  │ │
konst p6 = p1 * p2 + (p5 - p3)

//  Float
//  │
konst withGetter
//          konst p1: Int
//          │  fun (Int).times(Float): Float
//          │  │ konst p3: Float
//          │  │ │
    get() = p1 * p3

//  String
//  │
var withSetter
//          konst p4: String
//          │
    get() = p4
//      String   <set-withSetter>.konstue: String
//      │        │
    set(konstue) = konstue

//  Boolean
//  │
konst withGetter2: Boolean
    get() {
//             Boolean
//             │
        return true
    }

//  String
//  │
var withSetter2: String
    get() = "1"
//      String
//      │
    set(konstue) {
//      var <set-withSetter2>.field: String
//      │       <set-withSetter2>.konstue: String
//      │       │     fun (String).plus(Any?): String
//      │       │     │
        field = konstue + "!"
    }

//          String
//          │
private konst privateGetter: String = "cba"
    get

//  String
//  │
var privateSetter: String = "abc"
    private set
