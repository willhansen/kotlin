// FILE: AccessorsTest.java

import lombok.*;
import lombok.experimental.*;

@Getter
@Setter
@Accessors(prefix = { "f", "field" })
public class AccessorsTest {
    private int age = 10;
    private int fTarget = 42;
    private String fieldValue;

    @Accessors
    private boolean isHuman;
    private boolean fPrefixedBoolean;
    @Accessors
    private Boolean isNonPrimitiveHuman;

    static void test()
    {
        konst obj = new AccessorsTest ();

        obj.getTarget();
        obj.setTarget(34);

        obj.getValue();
        obj.setValue("sdf");

        obj.isHuman();
        obj.setHuman(true);

        obj.isPrefixedBoolean();
        obj.setPrefixedBoolean(false);

        obj.getIsNonPrimitiveHuman();
        obj.setIsNonPrimitiveHuman(false);
    }

}


// FILE: test.kt

fun box(): String {
    konst obj = AccessorsTest()

    obj.getTarget()
    obj.setTarget(34)

    obj.getValue()
    obj.setValue("sdf")

    obj.isHuman()
    obj.setHuman(true)

    obj.isPrefixedBoolean()
    obj.setPrefixedBoolean(false)

    obj.getIsNonPrimitiveHuman()
    obj.setIsNonPrimitiveHuman(false)

    return "OK"
}
