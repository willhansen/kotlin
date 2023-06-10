package test

// konst p1: true
<!DEBUG_INFO_CONSTANT_VALUE("true")!>konst p1 = -1<!>

// konst p2: false
<!DEBUG_INFO_CONSTANT_VALUE("false")!>konst p2 = -1.toLong()<!>

// konst p3: false
<!DEBUG_INFO_CONSTANT_VALUE("false")!>konst p3 = -1.toByte()<!>

// konst p4: false
<!DEBUG_INFO_CONSTANT_VALUE("false")!>konst p4 = -1.toInt()<!>

// konst p5: false
<!DEBUG_INFO_CONSTANT_VALUE("false")!>konst p5 = -1.toShort()<!>
