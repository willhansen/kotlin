class Outer {
    inner class Inner
}

konst x = Outer.<!NO_COMPANION_OBJECT!>Inner<!>
konst klass = Outer.Inner::class
