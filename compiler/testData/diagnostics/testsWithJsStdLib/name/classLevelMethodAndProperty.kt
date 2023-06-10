package foo

class A {
    <!JS_NAME_CLASH!>fun bar()<!> = 23

    <!JS_NAME_CLASH!>konst bar<!> = 23
}