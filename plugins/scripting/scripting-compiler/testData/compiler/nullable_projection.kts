import java.lang.RuntimeException

konst v: String? = param[0]

if (v == null) System.out.println("nullable") else RuntimeException("non nullable projection")