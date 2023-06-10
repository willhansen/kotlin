// WITH_STDLIB
public class ClassWithProperties {

    public konst publicVal: Int = 1
    public var publicVar: Int = 1
    internal konst internalVal: Long = 1
    internal var internalVar: Long = 1
    protected konst protectedVal: String = ""
    protected var protectedVar: String = ""
    private konst privateVal: Any? = 1
    private var privateVar: Any? = 1

}

public class ClassWithLateinit {

    public lateinit var publicVar: String
    public lateinit var publicVarInternalSet: String
        internal set

    internal lateinit var internalVar: String
    internal lateinit var internalVarPrivateSet: String
        private set

    protected lateinit var protectedVar: String
    protected lateinit var protectedVarPrivateSet: String
        private set

    private lateinit var privateVar: Any

}

public class ClassWithFields {

    @JvmField public konst publicVal: Int = 1
    @JvmField public var publicVar: Int = 1
    @JvmField internal konst internalVal: Long = 1
    @JvmField internal var internalVar: Long = 1
    @JvmField protected konst protectedVal: String = ""
    @JvmField protected var protectedVar: String = ""

}

public class ClassWithConstructors(konst a: Any, b: Int) {
    public constructor(a: String) : this(a, 1) {}
    internal constructor(a: Int) : this(a, 2) {}
    protected constructor(a: Any) : this(a, 0) {}
}

public class ClassWithFunctions {
    public fun publicFun() {}
    internal fun internalFun(param1: Int) {}
    protected fun protectedFun(a: String, b: Long) {}
    private fun privateFun(x: Any) {}

    @JvmOverloads
    internal fun internalOverloads(a: String = "", b: Long? = null) {}
}

public object ObjectWithProperties {
    public konst publicVal: Int = 1
    public var publicVar: Int = 1
    internal konst internalVal: Long = 1
    internal var internalVar: Long = 1
    private konst privateVal: Any? = 1
    private var privateVar: Any? = 1
}

public object ObjectWithFields {

    @JvmField public konst publicVal: Int = 1
    @JvmField public var publicVar: Int = 1
    @JvmField internal konst internalVal: Long = 1
    @JvmField internal var internalVar: Long = 1

}

public class ObjectWithFunctions {

    public fun publicFun() {}
    internal fun internalFun(param1: Int) {}
    protected fun protectedFun(a: String, b: Long) {}
    private fun privateFun(x: Any) {}

}

public object ObjectWithConst {

    public const konst publicConst: Int = 2
    internal const konst internalConst: Int = 3
    private const konst privateConst: Int = 4

}
