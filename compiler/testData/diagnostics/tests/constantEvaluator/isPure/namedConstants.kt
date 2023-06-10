package test

konst NAMED_CONSTANT = 1

// konst prop1: false
<!DEBUG_INFO_CONSTANT_VALUE("false")!>konst prop1 = NAMED_CONSTANT<!>

// konst prop2: false
<!DEBUG_INFO_CONSTANT_VALUE("false")!>konst prop2 = NAMED_CONSTANT + 1<!>
