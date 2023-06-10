package test

konst x = 1
konst y = "a"

// konst prop1: true
<!DEBUG_INFO_CONSTANT_VALUE("true")!>konst prop1 = x<!>

// konst prop2: true
<!DEBUG_INFO_CONSTANT_VALUE("true")!>konst prop2 = y<!>

