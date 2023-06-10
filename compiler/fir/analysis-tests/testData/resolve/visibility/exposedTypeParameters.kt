private open class A

internal open class B

public open class C {
    protected open class D
}

public open class E

// inkonstid, A is private
public class Test1<T: <!EXPOSED_TYPE_PARAMETER_BOUND!>A<!>>

// konstid, both type parameters is public
public class Test2<T: C, P: E>

// inkonstid, D is protected
public class Test3<T: E, P: <!EXPOSED_TYPE_PARAMETER_BOUND, INVISIBLE_REFERENCE!>C.D<!>>

// konstid, B is internal
internal class Test4<T: B>

// konstid, B is internal
private class Test5<T: B>

public class Container : <!SUPERTYPE_NOT_INITIALIZED!>C<!> {
    // konstid, D is protected in C
    protected class Test6<T: C.D>

    // inkonstid, B is internal
    protected class Test7<T: <!EXPOSED_TYPE_PARAMETER_BOUND!>B<!>>
}

// inkonstid, A is private, B is internal, D is protected
public interface Test8<T: <!EXPOSED_TYPE_PARAMETER_BOUND!>A<!>, P: <!EXPOSED_TYPE_PARAMETER_BOUND!>B<!>, F: C, N: <!EXPOSED_TYPE_PARAMETER_BOUND, INVISIBLE_REFERENCE!>C.D<!>, M: E>
