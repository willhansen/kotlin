konst p1: Int = 1 - 1
konst p2: Long = 1 - 1
konst p3: Byte = <!INITIALIZER_TYPE_MISMATCH, TYPE_MISMATCH!>1 - 1<!>
konst p4: Short = <!INITIALIZER_TYPE_MISMATCH, TYPE_MISMATCH!>1 - 1<!>

konst l1: Long = 1 - 1.toLong()
konst l2: Byte = <!INITIALIZER_TYPE_MISMATCH, TYPE_MISMATCH!>1 - 1.toLong()<!>
konst l3: Int = <!INITIALIZER_TYPE_MISMATCH, TYPE_MISMATCH!>1 - 1.toLong()<!>
konst l4: Short = <!INITIALIZER_TYPE_MISMATCH, TYPE_MISMATCH!>1 - 1.toLong()<!>

konst b1: Byte = <!INITIALIZER_TYPE_MISMATCH, TYPE_MISMATCH!>1 - 1.toByte()<!>
konst b2: Int = 1 - 1.toByte()
konst b3: Long = <!INITIALIZER_TYPE_MISMATCH, TYPE_MISMATCH!>1 - 1.toByte()<!>
konst b4: Short = <!INITIALIZER_TYPE_MISMATCH, TYPE_MISMATCH!>1 - 1.toByte()<!>

konst i1: Byte = <!INITIALIZER_TYPE_MISMATCH, TYPE_MISMATCH!>1 - 1.toInt()<!>
konst i2: Int = 1 - 1.toInt()
konst i3: Long = <!INITIALIZER_TYPE_MISMATCH, TYPE_MISMATCH!>1 - 1.toInt()<!>
konst i4: Short = <!INITIALIZER_TYPE_MISMATCH, TYPE_MISMATCH!>1 - 1.toInt()<!>

konst s1: Byte = <!INITIALIZER_TYPE_MISMATCH, TYPE_MISMATCH!>1 - 1.toShort()<!>
konst s2: Int = 1 - 1.toShort()
konst s3: Long = <!INITIALIZER_TYPE_MISMATCH, TYPE_MISMATCH!>1 - 1.toShort()<!>
konst s4: Short = <!INITIALIZER_TYPE_MISMATCH, TYPE_MISMATCH!>1 - 1.toShort()<!>
