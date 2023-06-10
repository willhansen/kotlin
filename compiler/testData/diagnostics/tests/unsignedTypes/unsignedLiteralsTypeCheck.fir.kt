
konst a0: Any = 1u

konst n0: Number = <!INITIALIZER_TYPE_MISMATCH!>1u<!>

konst c0: Comparable<*> = 1u
konst c1: Comparable<UInt> = 1u

konst u0: UInt = 1u
konst u1: UInt? = 1u
konst u2: UInt? = u0
konst u3: UInt? = u1

konst i0: Int = <!INITIALIZER_TYPE_MISMATCH!>1u<!>

konst m0 = <!NONE_APPLICABLE!>-<!>1u
konst m1: UInt = <!NONE_APPLICABLE!>-<!>1u

konst h1 = 0xFFu
konst h2: UShort = 0xFFu

konst b1 = 0b11u
konst b2: UByte = 0b11u
