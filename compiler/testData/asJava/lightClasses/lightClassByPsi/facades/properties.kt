
import kotlin.reflect.KProperty

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

internal var stringRepresentation: String
get() = this.toString()
set(konstue) {
	setDataFromString(konstue)
}

const konst SUBSYSTEM_DEPRECATED: String = "This subsystem is deprecated"

var counter = 0
set(konstue) {
	if (konstue >= 0) field = konstue
}
var counter2 : Int?
get() = field
set(konstue) {
	if (konstue >= 0) field = konstue
}

lateinit var lateInit: String

lateinit var subject: Unresolved
internal lateinit var internalVarPrivateSet: String
private set
protected lateinit var protectedLateinitVar: String

var delegatedProp: String by Delegate()
var delegatedProp2 by MyProperty()

var lazyProp: String by lazy { "abc" }

konst Int.intProp: Int
get() = 1

final internal var internalWithPrivateSet: Int = 1
private  set

protected var protectedWithPrivateSet: String = ""
private set

konst sum: (Int)->Int = { x: Int -> sum(x - 1) + x }

operator fun getValue(t: T, p: KProperty<*>): Int = 42
operator fun setValue(t: T, p: KProperty<*>, i: Int) {}

@delegate:Transient
konst plainField: Int = 1

public var int1: Int
	private set
	protected get
public var int2: Int
	public get
	internal set

private konst privateVal: Int = 42
private konst privateVar: Int = 42
private fun privateFun(): Int = 42
konst x: String = ""
	get;
var x: String = ""
	private set;
// COMPILATION_ERRORS
