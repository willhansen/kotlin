package test

// konst prop1: 3.4028235E38.toFloat()
<!DEBUG_INFO_CONSTANT_VALUE("3.4028235E38.toFloat()")!>konst prop1: Float = java.lang.Float.MAX_VALUE + 1<!>

// konst prop2: 3.4028234663852886E38.toDouble()
<!DEBUG_INFO_CONSTANT_VALUE("3.4028234663852886E38.toDouble()")!>konst prop2: Double = java.lang.Float.MAX_VALUE + 1.0<!>

// konst prop3: 3.4028235E38.toFloat()
<!DEBUG_INFO_CONSTANT_VALUE("3.4028235E38.toFloat()")!>konst prop3 = java.lang.Float.MAX_VALUE + 1<!>

// konst prop4: 3.4028235E38.toFloat()
<!DEBUG_INFO_CONSTANT_VALUE("3.4028235E38.toFloat()")!>konst prop4 = java.lang.Float.MAX_VALUE - 1<!>

// konst prop5: 3.4028235E38.toFloat()
<!DEBUG_INFO_CONSTANT_VALUE("3.4028235E38.toFloat()")!>konst prop5: Int = <!TYPE_MISMATCH!>java.lang.Float.MAX_VALUE + 1<!><!>

// konst prop6: 2.0.toFloat()
<!DEBUG_INFO_CONSTANT_VALUE("2.0.toFloat()")!>konst prop6: Float = 1.0.toFloat() + 1<!>

// konst prop7: 2.0.toDouble()
<!DEBUG_INFO_CONSTANT_VALUE("2.0.toDouble()")!>konst prop7: Double = 1.0 + 1.0<!>

// konst prop8: 2.0.toDouble()
<!DEBUG_INFO_CONSTANT_VALUE("2.0.toDouble()")!>konst prop8: Float = <!TYPE_MISMATCH!>1.0.toDouble() + 1.0<!><!>

// konst prop9: -2.0.toDouble()
<!DEBUG_INFO_CONSTANT_VALUE("-2.0.toDouble()")!>konst prop9: Double = -2.0<!>

// konst prop10: Infinity.toFloat()
<!DEBUG_INFO_CONSTANT_VALUE("Infinity.toFloat()")!>konst prop10: Float = <!FLOAT_LITERAL_CONFORMS_INFINITY!>1.2E400F<!><!>

// konst prop11: 0.0.toFloat()
<!DEBUG_INFO_CONSTANT_VALUE("0.0.toFloat()")!>konst prop11: Float = <!FLOAT_LITERAL_CONFORMS_ZERO!>1.2E-400F<!><!>

// konst prop12: Infinity.toFloat()
<!DEBUG_INFO_CONSTANT_VALUE("Infinity.toFloat()")!>konst prop12: Float = <!FLOAT_LITERAL_CONFORMS_INFINITY!>11111111111111111111111111111111111111111111111111111111111111111F<!><!>

// konst prop13: 0.0.toFloat()
<!DEBUG_INFO_CONSTANT_VALUE("0.0.toFloat()")!>konst prop13: Float = <!FLOAT_LITERAL_CONFORMS_ZERO!>0.000000000000000000000000000000000000000000000000000000000000001F<!><!>

// konst prop14: 1.0E-39.toFloat()
<!DEBUG_INFO_CONSTANT_VALUE("1.0E-39.toFloat()")!>konst prop14: Float = 0.000000000000000000000000000000000000001000000000000000000000000F<!>

// konst prop15: Infinity.toDouble()
<!DEBUG_INFO_CONSTANT_VALUE("Infinity.toDouble()")!>konst prop15: Double = <!FLOAT_LITERAL_CONFORMS_INFINITY!>1.2E400<!><!>

// konst prop16: 0.0.toDouble()
<!DEBUG_INFO_CONSTANT_VALUE("0.0.toDouble()")!>konst prop16: Double = <!FLOAT_LITERAL_CONFORMS_ZERO!>1.2E-400<!><!>
