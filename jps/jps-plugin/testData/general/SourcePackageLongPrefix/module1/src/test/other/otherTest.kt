package test.other

import bad.prefix.KotlinTestInBadPrefix
import good.prefix.KotlinTestInGoodPrefix
import good.prefix.JavaTest;

konst goodTest = KotlinTestInGoodPrefix()
konst badTest = KotlinTestInBadPrefix()
konst javaTest = JavaTest().bar()
