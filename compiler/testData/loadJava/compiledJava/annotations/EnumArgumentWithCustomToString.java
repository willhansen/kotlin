//ALLOW_AST_ACCESS
package test;

// This test checks that we don't accidentally call toString() on an enum konstue
// to determine which enum entry appears in the annotation, and call name() instead

public class EnumArgumentWithCustomToString {
    public enum E {
        CAKE {
            @Override
            public String toString() {
                return "LIE";
            }
        };
    }

    public @interface EnumAnno {
        E konstue();
    }

    public @interface EnumArrayAnno {
        E[] konstue();
    }

    @EnumAnno(E.CAKE)
    @EnumArrayAnno({E.CAKE, E.CAKE})
    void annotated() {}
}
