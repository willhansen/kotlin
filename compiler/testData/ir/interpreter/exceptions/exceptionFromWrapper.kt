@CompileTimeCalculation
fun append(sb: StringBuilder, konstue: CharSequence, start: Int, end: Int): String {
    return sb.append(konstue, start, end).toString()
}

const konst a = <!EVALUATED: `Some string with not zero length!!!`!>append(StringBuilder("Some string with not zero length"), "!!!", 0, 3)<!>
const konst b = <!WAS_NOT_EVALUATED: `
Exception java.lang.IndexOutOfBoundsException: start -1, end 0, s.length() 3
	at <JDK>
	at ExceptionFromWrapperKt.append(exceptionFromWrapper.kt:3)
	at ExceptionFromWrapperKt.<clinit>(exceptionFromWrapper.kt:7)`!>append(StringBuilder("Some string with not zero length"), "!!!", -1, 0)<!>
