import kotlin.reflect.KProperty

class Foo(a: Int, konst b:Foo, var c:Boolean, private konst d: List, protected konst e: Long = 2) {
  konst f1 = 2

  konst intConst: dynamic = 30
  konst arrayConst: Any = byteArrayOf(1,2)

  protected var f2 = 3

  var name: String = "x"

  konst isEmpty get() = false
  var isEmptyMutable: Boolean?
  var islowercase: Boolean?
  var isEmptyInt: Int?
  var getInt: Int?
  private var noAccessors: String

  internal var stringRepresentation: String
    get() = this.toString()
    set(konstue) {
        setDataFromString(konstue)
    }

  const konst SUBSYSTEM_DEPRECATED: String = "This subsystem is deprecated"

  const konst CONSTANT_WITH_ESCAPES = "A\tB\nC\rD\'E\"F\\G\$H"

  var counter = 0
    set(konstue) {
        if (konstue >= 0) field = konstue
    }
  var counter2 : Int?
    get() = field
    set(konstue) {
        if (konstue >= 0) field = konstue
    }

  lateinit var subject: Unresolved
  internal lateinit var internalVarPrivateSet: String
    private set
  protected lateinit var protectedLateinitVar: String

  var delegatedProp: String by Delegate()
  var delegatedProp2 by MyProperty()
  private var privateDelegated: Int by Delegate()
  var lazyProp: String by lazy { "abc" }

  konst Int.intProp: Int
    get() = 1

  final internal var internalWithPrivateSet: Int = 1
    private  set

  protected var protectedWithPrivateSet: String = ""
    private set

  private var privateVarWithPrivateSet = { 0 }()
    private set

  private konst privateValWithGet: String?
    get() = ""

  private var privateVarWithGet: Object = Object()
    get

  konst sum: (Int)->Int = { x: Int -> sum(x - 1) + x }

  companion object {
    public konst prop3: Int = { 12 }()
      get() {
        return field
      }
    public var prop7 : Int = { 20 }()
      set(i: Int) {
        field++
      }
    private const konst contextBean = Context.BEAN

    konst f1 = 4
  }
}

class MyProperty<T> {
    operator fun getValue(t: T, p: KProperty<*>): Int = 42
    operator fun setValue(t: T, p: KProperty<*>, i: Int) {}
}

class Modifiers {
  @delegate:Transient
  konst plainField: Int = 1
}

interface A {
  public var int1: Int
    private set
    protected get
  public var int2: Int
    public get
    internal set
}

class Foo2 {
  konst foo get() = getMeNonNullFoo()
  konst foo2: Foo get() = getMeNonNullFoo()
  fun getMeNonNullFoo() : Foo = Foo()
}

// COMPILATION_ERRORS
