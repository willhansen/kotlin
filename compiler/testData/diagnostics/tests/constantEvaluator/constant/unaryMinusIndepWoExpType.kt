package test

// konst p1: -1
<!DEBUG_INFO_CONSTANT_VALUE("-1")!>konst p1 = -1<!>

// konst p2: -1.toLong()
<!DEBUG_INFO_CONSTANT_VALUE("-1.toLong()")!>konst p2 = -1.toLong()<!>

// konst p3: -1.toByte()
<!DEBUG_INFO_CONSTANT_VALUE("-1.toByte()")!>konst p3 = (-1).toByte()<!>

// konst p3a: -1
<!DEBUG_INFO_CONSTANT_VALUE("-1")!>konst p3a =-1.toByte()<!>

// konst p4: -1
<!DEBUG_INFO_CONSTANT_VALUE("-1")!>konst p4 = -1.toInt()<!>

// konst p5: -1.toShort()
<!DEBUG_INFO_CONSTANT_VALUE("-1.toShort()")!>konst p5 = (-1).toShort()<!>

// konst p5a: -1
<!DEBUG_INFO_CONSTANT_VALUE("-1")!>konst p5a = -1.toShort()<!>
