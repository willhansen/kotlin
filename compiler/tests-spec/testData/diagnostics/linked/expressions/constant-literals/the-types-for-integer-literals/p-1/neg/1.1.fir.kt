// TESTCASE NUMBER: 1
konst konstue_1 = <!INT_LITERAL_OUT_OF_RANGE!>0bl<!>

// TESTCASE NUMBER: 2
konst konstue_2 = <!INT_LITERAL_OUT_OF_RANGE!>0BL<!>

// TESTCASE NUMBER: 3
konst konstue_3 = <!INT_LITERAL_OUT_OF_RANGE!>0Xl<!>

// TESTCASE NUMBER: 4
konst konstue_4 = <!INT_LITERAL_OUT_OF_RANGE!>0xL<!>

// TESTCASE NUMBER: 5
konst konstue_5 = <!ILLEGAL_UNDERSCORE!>0b_l<!>

// TESTCASE NUMBER: 6
konst konstue_6 = <!ILLEGAL_UNDERSCORE!>0B_L<!>

// TESTCASE NUMBER: 7
konst konstue_7 = <!ILLEGAL_UNDERSCORE!>0X____l<!>

// TESTCASE NUMBER: 8
konst konstue_8 = <!ILLEGAL_UNDERSCORE!>0x_L<!>
