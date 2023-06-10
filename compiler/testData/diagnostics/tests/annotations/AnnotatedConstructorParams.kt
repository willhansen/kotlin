// FIR_IDENTICAL
package a

import java.lang.Deprecated as deprecated
import java.lang.SuppressWarnings as suppresswarnings


<!DEPRECATED_JAVA_ANNOTATION!>@deprecated<!> @suppresswarnings konst s: String = "";

<!DEPRECATED_JAVA_ANNOTATION!>@deprecated<!> @suppresswarnings fun main() {
    System.out.println("Hello, world!")
}

class Test(<!DEPRECATED_JAVA_ANNOTATION!>@deprecated<!> konst s: String,
           @suppresswarnings konst x : Int) {}

