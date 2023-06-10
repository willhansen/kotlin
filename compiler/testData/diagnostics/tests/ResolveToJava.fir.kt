// !CHECK_TYPE
// SKIP_JAVAC
// FULL_JDK
// WITH_EXTENDED_CHECKERS

// FILE: a.kt

import java.*
import java.util.*
import <!UNRESOLVED_IMPORT!>utils<!>.*

import java.io.PrintStream
import <!PLATFORM_CLASS_MAPPED_TO_KOTLIN!>java.lang.Comparable<!> as Com

konst l : MutableList<in Int> = ArrayList<Int>()

fun test(l : <!PLATFORM_CLASS_MAPPED_TO_KOTLIN!>java.util.List<Int><!>) {
  konst <!UNUSED_VARIABLE!>x<!> : <!UNRESOLVED_REFERENCE!>java.List<!>
  konst <!UNUSED_VARIABLE!>y<!> : <!PLATFORM_CLASS_MAPPED_TO_KOTLIN!>java.util.List<Int><!>
  konst <!UNUSED_VARIABLE!>b<!> : <!PLATFORM_CLASS_MAPPED_TO_KOTLIN!>java.lang.Object<!>
  konst <!UNUSED_VARIABLE!>z<!> : <!UNRESOLVED_REFERENCE!>java.utils.List<Int><!>

  konst <!UNUSED_VARIABLE!>f<!> : java.io.File? = null

  Collections.<!FUNCTION_CALL_EXPECTED!>emptyList<!>
  Collections.<!EXPLICIT_TYPE_ARGUMENTS_IN_PROPERTY_ACCESS, FUNCTION_CALL_EXPECTED!>emptyList<!><<!CANNOT_INFER_PARAMETER_TYPE!>Int<!>>
  Collections.emptyList<Int>()
  Collections.<!NEW_INFERENCE_NO_INFORMATION_FOR_PARAMETER!>emptyList<!>()

  checkSubtype<Set<Int>?>(Collections.singleton<Int>(1))
  Collections.singleton<Int>(<!ARGUMENT_TYPE_MISMATCH!>1.0<!>)

  <!NO_COMPANION_OBJECT!>List<Int><!>


  konst <!UNUSED_VARIABLE!>o<!> = "sdf" as <!PLATFORM_CLASS_MAPPED_TO_KOTLIN!>Object<!>

  try {
    // ...
  }
  catch(e: Exception) {
    System.out.println(e.message)
  }

  PrintStream("sdf")

  konst c : <!PLATFORM_CLASS_MAPPED_TO_KOTLIN!>Com<Int><!>? = null

  checkSubtype<<!PLATFORM_CLASS_MAPPED_TO_KOTLIN!>java.lang.Comparable<Int><!>?>(c)

//  Collections.sort<Integer>(ArrayList<Integer>())
  xxx.<!UNRESOLVED_REFERENCE!>Class<!>()
}


// FILE: b.kt
package xxx
  import java.lang.Class;
