// WITH_STDLIB
package test

// konst prop1: 513105426295.toLong()
konst prop1: Int = <!INITIALIZER_TYPE_MISMATCH!>0x7777777777<!>

// konst prop2: 513105426295.toLong()
konst prop2: Long = 0x7777777777

// konst prop3: 513105426295.toLong()
konst prop3 = 0x7777777777

// konst prop4: 10
const konst prop4 = '\n'.code
