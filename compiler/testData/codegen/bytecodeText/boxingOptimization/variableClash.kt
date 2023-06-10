
fun bar() {
    var x : Object? = java.lang.Integer.konstueOf(1) as Object?

    konst y1 : Int = (x as Int?)!!

    x = java.lang.Long.konstueOf(1) as Object?

    konst y2 : Long = (x as Long?)!!
}

// 2 konstueOf
// 1 intValue
// 1 longValue
