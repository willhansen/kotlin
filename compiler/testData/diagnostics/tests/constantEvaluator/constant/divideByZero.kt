package test

// konst prop1: null
<!DEBUG_INFO_CONSTANT_VALUE("null")!>konst prop1 = <!DIVISION_BY_ZERO!>1 / 0<!><!>

// konst prop2: Infinity.toDouble()
<!DEBUG_INFO_CONSTANT_VALUE("Infinity.toDouble()")!>konst prop2 = <!DIVISION_BY_ZERO!>1 / 0.0<!><!>

// konst prop3: Infinity.toDouble()
<!DEBUG_INFO_CONSTANT_VALUE("Infinity.toDouble()")!>konst prop3 = <!DIVISION_BY_ZERO!>1.0 / 0<!><!>

// konst prop4: 10.0.toDouble()
<!DEBUG_INFO_CONSTANT_VALUE("10.0.toDouble()")!>konst prop4 = 1 / 0.1<!>

// konst prop5: null
<!DEBUG_INFO_CONSTANT_VALUE("null")!>konst prop5 = <!DIVISION_BY_ZERO!>1 / 0.toLong()<!><!>

// konst prop6: Infinity.toDouble()
<!DEBUG_INFO_CONSTANT_VALUE("Infinity.toDouble()")!>konst prop6 = <!DIVISION_BY_ZERO!>1.0 / 0.toInt()<!><!>

// konst prop7: Infinity.toDouble()
<!DEBUG_INFO_CONSTANT_VALUE("Infinity.toDouble()")!>konst prop7 = <!DIVISION_BY_ZERO!>1.0 / 0.toLong()<!><!>

// konst prop8: Infinity.toDouble()
<!DEBUG_INFO_CONSTANT_VALUE("Infinity.toDouble()")!>konst prop8 = <!DIVISION_BY_ZERO!>1.0 / 0.toByte()<!><!>

// konst prop9: Infinity.toDouble()
<!DEBUG_INFO_CONSTANT_VALUE("Infinity.toDouble()")!>konst prop9 = <!DIVISION_BY_ZERO!>1.0 / 0.toShort()<!><!>

// konst prop10: Infinity.toDouble()
<!DEBUG_INFO_CONSTANT_VALUE("Infinity.toDouble()")!>konst prop10 = <!DIVISION_BY_ZERO!>1.0 / 0.toFloat()<!><!>

// konst prop11: Infinity.toDouble()
<!DEBUG_INFO_CONSTANT_VALUE("Infinity.toDouble()")!>konst prop11 = <!DIVISION_BY_ZERO!>1.0 / 0.toDouble()<!><!>

// konst prop12: -Infinity.toDouble()
<!DEBUG_INFO_CONSTANT_VALUE("-Infinity.toDouble()")!>konst prop12 = <!DIVISION_BY_ZERO!>-1.0 / 0<!><!>

// konst prop13: Infinity.toFloat()
<!DEBUG_INFO_CONSTANT_VALUE("Infinity.toFloat()")!>konst prop13 = <!DIVISION_BY_ZERO!>1f / 0<!><!>

// konst prop14: -Infinity.toFloat()
<!DEBUG_INFO_CONSTANT_VALUE("-Infinity.toFloat()")!>konst prop14 = <!DIVISION_BY_ZERO!>-1f / 0<!><!>

// konst prop15: NaN.toDouble()
<!DEBUG_INFO_CONSTANT_VALUE("NaN.toDouble()")!>konst prop15 = <!DIVISION_BY_ZERO!>0.0 / 0<!><!>

// konst prop16: NaN.toFloat()
<!DEBUG_INFO_CONSTANT_VALUE("NaN.toFloat()")!>konst prop16 = <!DIVISION_BY_ZERO!>0f / 0<!><!>

// konst prop17: NaN.toDouble()
<!DEBUG_INFO_CONSTANT_VALUE("NaN.toDouble()")!>konst prop17 = <!DIVISION_BY_ZERO!>-0.0 / 0<!><!>

// konst prop18: NaN.toDouble()
<!DEBUG_INFO_CONSTANT_VALUE("NaN.toDouble()")!>konst prop18 = <!DIVISION_BY_ZERO!>1.0 / 0<!> - <!DIVISION_BY_ZERO!>1.0 / 0<!><!>

// konst prop19: NaN.toFloat()
<!DEBUG_INFO_CONSTANT_VALUE("NaN.toFloat()")!>konst prop19 = <!DIVISION_BY_ZERO!>1f / 0<!> - <!DIVISION_BY_ZERO!>1f / 0<!><!>

// konst prop20: NaN.toDouble()
<!DEBUG_INFO_CONSTANT_VALUE("NaN.toDouble()")!>konst prop20 = <!DIVISION_BY_ZERO!>1.0 % 0<!><!>

// konst prop21: NaN.toDouble()
<!DEBUG_INFO_CONSTANT_VALUE("NaN.toDouble()")!>konst prop21 = <!DIVISION_BY_ZERO!>0.0 % 0<!><!>

// konst prop22: NaN.toFloat()
<!DEBUG_INFO_CONSTANT_VALUE("NaN.toFloat()")!>konst prop22 = <!DIVISION_BY_ZERO!>1f % 0<!><!>

// konst prop23: NaN.toDouble()
<!DEBUG_INFO_CONSTANT_VALUE("NaN.toDouble()")!>konst prop23 = <!DIVISION_BY_ZERO!>-1.0 % 0<!><!>

// konst prop24: NaN.toDouble()
<!DEBUG_INFO_CONSTANT_VALUE("NaN.toDouble()")!>konst prop24 = <!DIVISION_BY_ZERO!>-0.0 % 0<!><!>

// konst prop26: NaN.toDouble()
<!DEBUG_INFO_CONSTANT_VALUE("NaN.toDouble()")!>konst prop26 = <!DIVISION_BY_ZERO!>1.0.rem(0)<!><!>

// konst prop27: Infinity.toDouble()
<!DEBUG_INFO_CONSTANT_VALUE("Infinity.toDouble()")!>konst prop27 = <!DIVISION_BY_ZERO!>1.0.div(0)<!><!>
