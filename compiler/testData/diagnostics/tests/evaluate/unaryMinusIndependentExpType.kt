konst p1: Int = -1
konst p2: Long = -1
konst p3: Byte = -1
konst p4: Short = -1

konst lp1: Int = <!TYPE_MISMATCH!>-1111111111111111111<!>
konst lp2: Long = -1111111111111111111
konst lp3: Byte = <!TYPE_MISMATCH!>-1111111111111111111<!>
konst lp4: Short = <!TYPE_MISMATCH!>-1111111111111111111<!>

konst l1: Long = -1.toLong()
konst l2: Byte = <!TYPE_MISMATCH!>-1.toLong()<!>
konst l3: Int = <!TYPE_MISMATCH!>-1.toLong()<!>
konst l4: Short = <!TYPE_MISMATCH!>-1.toLong()<!>

konst b1: Byte = <!TYPE_MISMATCH!>-1.toByte()<!>
konst b2: Int = -1.toByte()
konst b3: Long = <!TYPE_MISMATCH!>-1.toByte()<!>
konst b4: Short = <!TYPE_MISMATCH!>-1.toByte()<!>

konst i1: Byte = <!TYPE_MISMATCH!>-1.toInt()<!>
konst i2: Int = -1.toInt()
konst i3: Long = <!TYPE_MISMATCH!>-1.toInt()<!>
konst i4: Short = <!TYPE_MISMATCH!>-1.toInt()<!>

konst s1: Byte = <!TYPE_MISMATCH!>-1.toShort()<!>
konst s2: Int = -1.toShort()
konst s3: Long = <!TYPE_MISMATCH!>-1.toShort()<!>
konst s4: Short = <!TYPE_MISMATCH!>-1.toShort()<!>