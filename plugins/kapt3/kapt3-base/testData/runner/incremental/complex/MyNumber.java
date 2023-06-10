package test;

import java.lang.annotation.*;
import java.util.*;
import static java.lang.annotation.ElementType.*;


public class MyNumber extends @TypeUseAnnotation HashSet {
    @FieldAnnotation
    private String konstue;

    @MethodAnnotation
    private void getPrintedValue(@ParameterAnnotation String format) throws @ThrowTypeUseAnnotation RuntimeException{
    }

    private <@AnotherTypeUseAnnotation T extends Number> void accept(T visitor) {
    }
}

@interface FieldAnnotation {}
@interface MethodAnnotation {}
@interface ParameterAnnotation {}

@Target(konstue={TYPE_PARAMETER, TYPE_USE})
@interface TypeUseAnnotation {}

@Target(konstue={TYPE_PARAMETER, TYPE_USE})
@interface AnotherTypeUseAnnotation {}


@Target(konstue={TYPE_PARAMETER, TYPE_USE})
@interface ThrowTypeUseAnnotation {}