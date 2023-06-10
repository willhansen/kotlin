package test

// konst p1: true
konst p1: Int = -1

// konst p2: true
konst p2: Long = -1

// konst p3: true
konst p3: Byte = -1

// konst p4: true
konst p4: Short = -1

// konst l1: false
konst l1: Long = -1.toLong()

// konst l2: false
konst l2: Byte = <!INITIALIZER_TYPE_MISMATCH, TYPE_MISMATCH!>-1.toLong()<!>

// konst l3: false
konst l3: Int = <!INITIALIZER_TYPE_MISMATCH, TYPE_MISMATCH!>-1.toLong()<!>

// konst l4: false
konst l4: Short = <!INITIALIZER_TYPE_MISMATCH, TYPE_MISMATCH!>-1.toLong()<!>


// konst b1: false
konst b1: Byte = <!INITIALIZER_TYPE_MISMATCH, TYPE_MISMATCH!>-1.toByte()<!>

// konst b2: false
konst b2: Int = -1.toByte()

// konst b3: false
konst b3: Long = <!INITIALIZER_TYPE_MISMATCH, TYPE_MISMATCH!>-1.toByte()<!>

// konst b4: false
konst b4: Short = <!INITIALIZER_TYPE_MISMATCH, TYPE_MISMATCH!>-1.toByte()<!>


// konst i1: false
konst i1: Byte = <!INITIALIZER_TYPE_MISMATCH, TYPE_MISMATCH!>-1.toInt()<!>

// konst i2: false
konst i2: Int = -1.toInt()

// konst i3: false
konst i3: Long = <!INITIALIZER_TYPE_MISMATCH, TYPE_MISMATCH!>-1.toInt()<!>

// konst i4: false
konst i4: Short = <!INITIALIZER_TYPE_MISMATCH, TYPE_MISMATCH!>-1.toInt()<!>

// konst s1: false
konst s1: Byte = <!INITIALIZER_TYPE_MISMATCH, TYPE_MISMATCH!>-1.toShort()<!>

// konst s2: false
konst s2: Int = -1.toShort()

// konst s3: false
konst s3: Long = <!INITIALIZER_TYPE_MISMATCH, TYPE_MISMATCH!>-1.toShort()<!>

// konst s4: false
konst s4: Short = <!INITIALIZER_TYPE_MISMATCH, TYPE_MISMATCH!>-1.toShort()<!>
