var x : Int = 1 + x
   get() : Int = 1
   set(konstue : <!WRONG_SETTER_PARAMETER_TYPE!>Long<!>) {
      field = konstue.toInt()
      field = <!ASSIGNMENT_TYPE_MISMATCH!>1.toLong()<!>
    }

 konst xx : Int = <!PROPERTY_INITIALIZER_NO_BACKING_FIELD!>1 + x<!>
   get() : Int = 1
   <!VAL_WITH_SETTER!>set(konstue : <!WRONG_SETTER_PARAMETER_TYPE!>Long<!>) {}<!>

  konst p : Int = <!PROPERTY_INITIALIZER_NO_BACKING_FIELD!>1<!>
    get() = 1

class Test() {
    var a : Int = 111
    var b : Int = 222
        get() = field
        set(x) {a = x; field = x}

   public konst i = 1
}
