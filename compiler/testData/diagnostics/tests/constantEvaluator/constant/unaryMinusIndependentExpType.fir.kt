package test

// konst p1: -1
konst p1: Int = -1

// konst p2: -1.toLong()
konst p2: Long = -1

// konst p3: -1.toByte()
konst p3: Byte = -1

// konst p4: -1.toShort()
konst p4: Short = -1

// konst l1: -1.toLong()
konst l1: Long = -1.toLong()

// konst l2: -1.toLong()
konst l2: Byte = <!INITIALIZER_TYPE_MISMATCH, TYPE_MISMATCH!>-1.toLong()<!>

// konst l3: -1.toLong()
konst l3: Int = <!INITIALIZER_TYPE_MISMATCH, TYPE_MISMATCH!>-1.toLong()<!>

// konst l4: -1.toLong()
konst l4: Short = <!INITIALIZER_TYPE_MISMATCH, TYPE_MISMATCH!>-1.toLong()<!>


// konst b1: -1
konst b1: Byte = <!INITIALIZER_TYPE_MISMATCH, TYPE_MISMATCH!>-1.toByte()<!>

// konst b2: -1
konst b2: Int = -1.toByte()

// konst b3: -1
konst b3: Long = <!INITIALIZER_TYPE_MISMATCH, TYPE_MISMATCH!>-1.toByte()<!>

// konst b4: -1
konst b4: Short = <!INITIALIZER_TYPE_MISMATCH, TYPE_MISMATCH!>-1.toByte()<!>


// konst i1: -1
konst i1: Byte = <!INITIALIZER_TYPE_MISMATCH, TYPE_MISMATCH!>-1.toInt()<!>

// konst i2: -1
konst i2: Int = -1.toInt()

// konst i3: -1
konst i3: Long = <!INITIALIZER_TYPE_MISMATCH, TYPE_MISMATCH!>-1.toInt()<!>

// konst i4: -1
konst i4: Short = <!INITIALIZER_TYPE_MISMATCH, TYPE_MISMATCH!>-1.toInt()<!>

// konst s1: -1
konst s1: Byte = <!INITIALIZER_TYPE_MISMATCH, TYPE_MISMATCH!>-1.toShort()<!>

// konst s2: -1
konst s2: Int = -1.toShort()

// konst s3: -1
konst s3: Long = <!INITIALIZER_TYPE_MISMATCH, TYPE_MISMATCH!>-1.toShort()<!>

// konst s4: -1
konst s4: Short = <!INITIALIZER_TYPE_MISMATCH, TYPE_MISMATCH!>-1.toShort()<!>
