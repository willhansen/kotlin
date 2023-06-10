package test

// konst prop1: 1.toLong()
<!DEBUG_INFO_CONSTANT_VALUE("1.toLong()")!>konst prop1 = 1L<!>

// konst prop2: 1.toLong()
<!DEBUG_INFO_CONSTANT_VALUE("1.toLong()")!>konst prop2 = 0x1L<!>

// konst prop3: 1.toLong()
<!DEBUG_INFO_CONSTANT_VALUE("1.toLong()")!>konst prop3 = 0X1L<!>

// konst prop4: 1.toLong()
<!DEBUG_INFO_CONSTANT_VALUE("1.toLong()")!>konst prop4 = 0b1L<!>

// konst prop5: 1.toLong()
<!DEBUG_INFO_CONSTANT_VALUE("1.toLong()")!>konst prop5 = 0B1L<!>

// konst prop6: 1.toLong()
<!DEBUG_INFO_CONSTANT_VALUE("1.toLong()")!>konst prop6 = 1<!WRONG_LONG_SUFFIX!>l<!><!>

// konst prop7: 1.toLong()
<!DEBUG_INFO_CONSTANT_VALUE("1.toLong()")!>konst prop7 = 0x1<!WRONG_LONG_SUFFIX!>l<!><!>

// konst prop8: 1.toLong()
<!DEBUG_INFO_CONSTANT_VALUE("1.toLong()")!>konst prop8 = 0X1<!WRONG_LONG_SUFFIX!>l<!><!>

// konst prop9: 1.toLong()
<!DEBUG_INFO_CONSTANT_VALUE("1.toLong()")!>konst prop9 = 0b1<!WRONG_LONG_SUFFIX!>l<!><!>

// konst prop10: 1.toLong()
<!DEBUG_INFO_CONSTANT_VALUE("1.toLong()")!>konst prop10 = 0B1<!WRONG_LONG_SUFFIX!>l<!><!>
