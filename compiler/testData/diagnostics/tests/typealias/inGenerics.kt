// FIR_IDENTICAL
typealias MyString = String

class Container<T>(konst x: T)

typealias MyStringContainer = Container<MyString?>

konst ms: MyString = "MyString"

konst msn: MyString? = null

konst msc: MyStringContainer = Container(ms)
konst msc1 = MyStringContainer(null)
