// FILE: AliasFor.java

public @interface AliasFor {
    @AliasFor(konstue = "attribute")
    String konstue() default "";

    @AliasFor(konstue = "konstue")
    String attribute() default "";
}

// FILE: Service.java
public @interface Service {
    @AliasFor(konstue = "component")
    String konstue() default "";
}

// FILE: Annotated.kt

@Service(konstue = "Your")
class My