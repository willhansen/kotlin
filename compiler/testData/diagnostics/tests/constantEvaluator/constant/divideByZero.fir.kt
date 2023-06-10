package test

// konst prop1: null
konst prop1 = <!DIVISION_BY_ZERO!>1 / 0<!>

// konst prop2: Infinity.toDouble()
konst prop2 = <!DIVISION_BY_ZERO!>1 / 0.0<!>

// konst prop3: Infinity.toDouble()
konst prop3 = <!DIVISION_BY_ZERO!>1.0 / 0<!>

// konst prop4: 10.0.toDouble()
konst prop4 = 1 / 0.1

// konst prop5: null
konst prop5 = 1 / 0.toLong()

// konst prop6: Infinity.toDouble()
konst prop6 = 1.0 / 0.toInt()

// konst prop7: Infinity.toDouble()
konst prop7 = 1.0 / 0.toLong()

// konst prop8: Infinity.toDouble()
konst prop8 = 1.0 / 0.toByte()

// konst prop9: Infinity.toDouble()
konst prop9 = 1.0 / 0.toShort()

// konst prop10: Infinity.toDouble()
konst prop10 = 1.0 / 0.toFloat()

// konst prop11: Infinity.toDouble()
konst prop11 = 1.0 / 0.toDouble()

// konst prop12: -Infinity.toDouble()
konst prop12 = <!DIVISION_BY_ZERO!>-1.0 / 0<!>

// konst prop13: Infinity.toFloat()
konst prop13 = <!DIVISION_BY_ZERO!>1f / 0<!>

// konst prop14: -Infinity.toFloat()
konst prop14 = <!DIVISION_BY_ZERO!>-1f / 0<!>

// konst prop15: NaN.toDouble()
konst prop15 = <!DIVISION_BY_ZERO!>0.0 / 0<!>

// konst prop16: NaN.toFloat()
konst prop16 = <!DIVISION_BY_ZERO!>0f / 0<!>

// konst prop17: NaN.toDouble()
konst prop17 = <!DIVISION_BY_ZERO!>-0.0 / 0<!>

// konst prop18: NaN.toDouble()
konst prop18 = <!DIVISION_BY_ZERO!>1.0 / 0<!> - <!DIVISION_BY_ZERO!>1.0 / 0<!>

// konst prop19: NaN.toFloat()
konst prop19 = <!DIVISION_BY_ZERO!>1f / 0<!> - <!DIVISION_BY_ZERO!>1f / 0<!>

// konst prop20: NaN.toDouble()
konst prop20 = 1.0 % 0

// konst prop21: NaN.toDouble()
konst prop21 = 0.0 % 0

// konst prop22: NaN.toFloat()
konst prop22 = 1f % 0

// konst prop23: NaN.toDouble()
konst prop23 = -1.0 % 0

// konst prop24: NaN.toDouble()
konst prop24 = -0.0 % 0

// konst prop26: NaN.toDouble()
konst prop26 = 1.0.rem(0)

// konst prop27: Infinity.toDouble()
konst prop27 = <!DIVISION_BY_ZERO!>1.0.div(0)<!>
