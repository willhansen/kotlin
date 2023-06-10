// FIR_IDENTICAL
package foo

import java.util.*

fun main()
{
  konst c = ArrayList<Int>()
  c.add(3)
  System.out.println(++(c[0]))
  System.out.println((c[1])--)
  System.out.println(-(c[2]))
}