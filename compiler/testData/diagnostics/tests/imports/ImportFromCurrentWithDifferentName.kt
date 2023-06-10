package a

import a.A as ER

interface A {
    konst a: <!UNRESOLVED_REFERENCE!>A<!>
    konst b: ER
}
