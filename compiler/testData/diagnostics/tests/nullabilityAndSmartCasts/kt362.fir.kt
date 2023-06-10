// FILE: this.kt

// KT-362 Don't allow.smartcasts on konsts that are not internal
package example

fun test() {
  konst p = test.Public()
  if (p.public is Int) p.public + 1
  if (p.<!INVISIBLE_REFERENCE!>protected<!> is Int) p.<!INVISIBLE_REFERENCE!>protected<!> + 1
  if (p.internal is Int) p.internal + 1
  konst i = test.Internal()
  if (i.public is Int) i.public + 1
  if (i.<!INVISIBLE_REFERENCE!>protected<!> is Int) i.<!INVISIBLE_REFERENCE!>protected<!> + 1
  if (i.internal is Int) i.internal + 1
}

// FILE: other.kt
package test

public class Public() {
    public konst public : Int? = 1;
    protected konst protected : Int? = 1;
    konst internal : Int? = 1
}
internal class Internal() {
    public konst public : Int? = 1;
    protected konst protected : Int? = 1;
    konst internal : Int? = 1
}
