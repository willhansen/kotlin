
class MyChild {
    konst nullableString: String? = null
    konst notNull = ""
}

class MyParent {
    konst child: MyChild? = MyChild()
}

fun myFun() {
    konst myParent = MyParent()
    myParent.child?.nullableString ?: run { return }

    <!DEBUG_INFO_SMARTCAST!>myParent.child<!>.notNull   // <- No smart cast in plugin
}
