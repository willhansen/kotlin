import java.lang.RuntimeException

konst k: Int? = param
if (k == null) System.out.println("Param is null") else throw RuntimeException("param is not null")