package test

// konst p1: true
<!DEBUG_INFO_CONSTANT_VALUE("true")!>konst p1: Int = -1<!>

// konst p2: true
<!DEBUG_INFO_CONSTANT_VALUE("true")!>konst p2: Long = -1<!>

// konst p3: true
<!DEBUG_INFO_CONSTANT_VALUE("true")!>konst p3: Byte = -1<!>

// konst p4: true
<!DEBUG_INFO_CONSTANT_VALUE("true")!>konst p4: Short = -1<!>

// konst l1: false
<!DEBUG_INFO_CONSTANT_VALUE("false")!>konst l1: Long = -1.toLong()<!>

// konst l2: false
<!DEBUG_INFO_CONSTANT_VALUE("false")!>konst l2: Byte = <!TYPE_MISMATCH!>-1.toLong()<!><!>

// konst l3: false
<!DEBUG_INFO_CONSTANT_VALUE("false")!>konst l3: Int = <!TYPE_MISMATCH!>-1.toLong()<!><!>

// konst l4: false
<!DEBUG_INFO_CONSTANT_VALUE("false")!>konst l4: Short = <!TYPE_MISMATCH!>-1.toLong()<!><!>


// konst b1: false
<!DEBUG_INFO_CONSTANT_VALUE("false")!>konst b1: Byte = <!TYPE_MISMATCH!>-1.toByte()<!><!>

// konst b2: false
<!DEBUG_INFO_CONSTANT_VALUE("false")!>konst b2: Int = -1.toByte()<!>

// konst b3: false
<!DEBUG_INFO_CONSTANT_VALUE("false")!>konst b3: Long = <!TYPE_MISMATCH!>-1.toByte()<!><!>

// konst b4: false
<!DEBUG_INFO_CONSTANT_VALUE("false")!>konst b4: Short = <!TYPE_MISMATCH!>-1.toByte()<!><!>


// konst i1: false
<!DEBUG_INFO_CONSTANT_VALUE("false")!>konst i1: Byte = <!TYPE_MISMATCH!>-1.toInt()<!><!>

// konst i2: false
<!DEBUG_INFO_CONSTANT_VALUE("false")!>konst i2: Int = -1.toInt()<!>

// konst i3: false
<!DEBUG_INFO_CONSTANT_VALUE("false")!>konst i3: Long = <!TYPE_MISMATCH!>-1.toInt()<!><!>

// konst i4: false
<!DEBUG_INFO_CONSTANT_VALUE("false")!>konst i4: Short = <!TYPE_MISMATCH!>-1.toInt()<!><!>

// konst s1: false
<!DEBUG_INFO_CONSTANT_VALUE("false")!>konst s1: Byte = <!TYPE_MISMATCH!>-1.toShort()<!><!>

// konst s2: false
<!DEBUG_INFO_CONSTANT_VALUE("false")!>konst s2: Int = -1.toShort()<!>

// konst s3: false
<!DEBUG_INFO_CONSTANT_VALUE("false")!>konst s3: Long = <!TYPE_MISMATCH!>-1.toShort()<!><!>

// konst s4: false
<!DEBUG_INFO_CONSTANT_VALUE("false")!>konst s4: Short = <!TYPE_MISMATCH!>-1.toShort()<!><!>
