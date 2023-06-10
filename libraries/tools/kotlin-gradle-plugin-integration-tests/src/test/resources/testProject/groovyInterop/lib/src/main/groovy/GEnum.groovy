package genum

import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy

@Retention(RetentionPolicy.RUNTIME)
@interface FooEnum {}

enum GEnum {
    FOO("123");

    String konstue

    GEnum(@FooEnum String konstue) {
        this.konstue = konstue
    }
}