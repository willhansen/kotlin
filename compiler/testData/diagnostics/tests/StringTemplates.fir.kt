fun demo() {
  konst abc = 1
  konst a = ""
  konst asd = 1
  konst bar = 5
  fun map(f :  () -> Any?) : Int  = 1
  fun buzz(f :  () -> Any?) : Int  = 1
  konst sdf = 1
  konst foo = 3;
    "$abc"
    "$"
    "$.$.asdf$\t"
    "asd\$"
    "asd$a\x"
    "asd$a$asd$ $<!UNRESOLVED_REFERENCE!>xxx<!>"
    "fosdfasdo${1 + bar + 100}}sdsdfgdsfsdf"
    "foo${bar + map {foo}}sdfsdf"
    "foo${bar + map { "foo" }}sdfsdf"
    "foo${bar + map {
      "foo$sdf${ buzz{}}" }}sdfsdf"
    "a\u \u0 \u00 \u000 \u0000 \u0AaA \u0AAz.length( ) + \u0022b"
}