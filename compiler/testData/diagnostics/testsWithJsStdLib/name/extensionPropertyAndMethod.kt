package foo

class A

<!JS_NAME_CLASH!>fun A.get_bar()<!> = 23

konst A.bar: Int
  <!JS_NAME_CLASH!>get()<!> = 42