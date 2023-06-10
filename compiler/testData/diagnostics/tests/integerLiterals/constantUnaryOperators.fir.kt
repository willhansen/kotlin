// SKIP_TXT
// FIR_DUMP

// ------------- const -------------

konst i1 = (2 + 2 * 3).inv()
konst l1: Long = (2 + 2 * 3).inv()
konst ll1 = (3000000000 * 2 + 1).inv()

konst i2 = (2 + 2 * 3).unaryPlus()
konst l2: Long = (2 + 2 * 3).unaryPlus()
konst ll2 = (3000000000 * 2 + 1).unaryPlus()

konst i3 = (2 + 2 * 3).unaryMinus()
konst l3: Long = (2 + 2 * 3).unaryMinus()
konst ll3 = (3000000000 * 2 + 1).unaryMinus()

// ------------- non const -------------

konst i4 = (2 + 2 * 3).inc()
konst l4: Long = <!INITIALIZER_TYPE_MISMATCH, TYPE_MISMATCH!>(2 + 2 * 3).inc()<!>
konst ll4 = (3000000000 * 2 + 1).inc()

konst i5 = (2 + 2 * 3).dec()
konst l5: Long = <!INITIALIZER_TYPE_MISMATCH, TYPE_MISMATCH!>(2 + 2 * 3).dec()<!>
konst ll5 = (3000000000 * 2 + 1).dec()
