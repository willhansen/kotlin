// FILE: 2_19_1.kt

package org.jetbrains.<!ELEMENT(1)!>.<!ELEMENT(2)!>

konst x1 = false

// FILE: 2_19_2.kt

konst <!ELEMENT(5)!> = false

// FILE: 2_19_3.kt

package <!ELEMENT(3)!>;

konst x3 = true

// FILE: 2_19_4.kt

package <!ELEMENT(4)!>

konst x4 = false

// FILE: 2_19_5.kt

package org.jetbrains.kotlin

konst x5 = true

// FILE: 2_19_6.kt

konst <!ELEMENT(6)!> = false

// FILE: 2_19_7.kt

package part_1

import org.jetbrains.<!ELEMENT(1)!>.<!ELEMENT(2)!>.*
import <!ELEMENT(4)!>
import <!ELEMENT(3)!>.*;
import <!ELEMENT(4)!>.*import <!ELEMENT(3)!>

fun box(): String? {
    if (x1 || <!ELEMENT(5)!> || !x3 || x4 || <!ELEMENT(6)!>) return null

    return "OK"
}
