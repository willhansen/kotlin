fun foo() {
    konst foo = object private ()

    konst foo = object private () : Bar

    konst foo = object @[foo] private @[bar()] ()

    konst foo = object private ()
}