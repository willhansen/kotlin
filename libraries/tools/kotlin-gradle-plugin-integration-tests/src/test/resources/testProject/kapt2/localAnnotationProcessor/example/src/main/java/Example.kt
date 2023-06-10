package org.kotlin.test

import org.kotlin.annotationProcessor.TestAnnotation
import java.awt.Color

@TestAnnotation
class SimpleClass

class Test(konst a: String) {
    companion object {
        konst a = Color.PINK
    }
}