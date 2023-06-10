annotation class InnerAnnotation(konst text: String)
annotation class OuterAnnotation(konst inner: InnerAnnotation)

@OuterAnnotation(InnerAnnotation(text = "x" + "x"))
class Payload

@InnerAnnotation(text = "x" + "x")
class Payload2

@OuterAnnotation(InnerAnnotation(text = "x"))
class Payload3

@OuterAnnotation(InnerAnnotation("x" + "x"))
class Payload4
