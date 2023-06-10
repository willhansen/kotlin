
konst foo = object Name private () {}

konst foo = object Name private () : Bar {

}

konst foo = object Name @[foo] private @[bar()] () {}

konst foo = object Name private ()
