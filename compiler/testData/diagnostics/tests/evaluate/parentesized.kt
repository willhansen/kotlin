konst p1: Byte = <!INTEGER_OPERATOR_RESOLVE_WILL_CHANGE!>(1 + 2) * 2<!>
konst p2: Short = <!INTEGER_OPERATOR_RESOLVE_WILL_CHANGE!>(1 + 2) * 2<!>
konst p3: Int = (1 + 2) * 2
konst p4: Long = (1 + 2) * 2

konst b1: Byte = <!INTEGER_OPERATOR_RESOLVE_WILL_CHANGE, TYPE_MISMATCH!>(1.toByte() + 2) * 2<!>
konst b2: Short = <!INTEGER_OPERATOR_RESOLVE_WILL_CHANGE, TYPE_MISMATCH!>(1.toShort() + 2) * 2<!>
konst b3: Int = (1.toInt() + 2) * 2
konst b4: Long = (1.toLong() + 2) * 2

konst i1: Int = (1.toByte() + 2) * 2
konst i2: Int = (1.toShort() + 2) * 2
