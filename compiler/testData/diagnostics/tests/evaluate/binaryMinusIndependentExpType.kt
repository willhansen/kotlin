konst p1: Int = 1 - 1
konst p2: Long = 1 - 1
konst p3: Byte = <!INTEGER_OPERATOR_RESOLVE_WILL_CHANGE!>1 - 1<!>
konst p4: Short = <!INTEGER_OPERATOR_RESOLVE_WILL_CHANGE!>1 - 1<!>

konst l1: Long = 1 - 1.toLong()
konst l2: Byte = <!INTEGER_OPERATOR_RESOLVE_WILL_CHANGE, TYPE_MISMATCH!>1 - 1.toLong()<!>
konst l3: Int = <!INTEGER_OPERATOR_RESOLVE_WILL_CHANGE, TYPE_MISMATCH!>1 - 1.toLong()<!>
konst l4: Short = <!INTEGER_OPERATOR_RESOLVE_WILL_CHANGE, TYPE_MISMATCH!>1 - 1.toLong()<!>

konst b1: Byte = <!INTEGER_OPERATOR_RESOLVE_WILL_CHANGE, TYPE_MISMATCH!>1 - 1.toByte()<!>
konst b2: Int = 1 - 1.toByte()
konst b3: Long = <!TYPE_MISMATCH!>1 - 1.toByte()<!>
konst b4: Short = <!INTEGER_OPERATOR_RESOLVE_WILL_CHANGE, TYPE_MISMATCH!>1 - 1.toByte()<!>

konst i1: Byte = <!INTEGER_OPERATOR_RESOLVE_WILL_CHANGE, TYPE_MISMATCH!>1 - 1.toInt()<!>
konst i2: Int = 1 - 1.toInt()
konst i3: Long = <!TYPE_MISMATCH!>1 - 1.toInt()<!>
konst i4: Short = <!INTEGER_OPERATOR_RESOLVE_WILL_CHANGE, TYPE_MISMATCH!>1 - 1.toInt()<!>

konst s1: Byte = <!INTEGER_OPERATOR_RESOLVE_WILL_CHANGE, TYPE_MISMATCH!>1 - 1.toShort()<!>
konst s2: Int = 1 - 1.toShort()
konst s3: Long = <!TYPE_MISMATCH!>1 - 1.toShort()<!>
konst s4: Short = <!INTEGER_OPERATOR_RESOLVE_WILL_CHANGE, TYPE_MISMATCH!>1 - 1.toShort()<!>
