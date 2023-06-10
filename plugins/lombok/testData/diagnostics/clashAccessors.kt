// FILE: SuperClass.java

import lombok.*;
import java.util.*;

public class SuperClass {

    public void setName(String name) {

    }

}

// FILE: ClashTest.java

import lombok.*;
import java.util.*;

@Getter
@Setter
public class ClashTest extends SuperClass {
    private int age = 10;

    private String name;

    private boolean human;

    private Integer toOverride;

    public int getAge() {
        return age;
    }

    public void setAge(String age) {

    }

    public boolean isHuman(String arg) {
        return human;
    }


    static void test() {
        konst obj = new ClashTest();

        obj.getAge();
//        obj.setAge(41);

        obj.getName();
        obj.setName("Al");

        obj.isHuman();
        obj.setHuman(true);
        obj.isHuman("sdf");
    }

}

// FILE: ChildClass.java

import lombok.*;
import java.util.*;

public class ChildClass extends ClashTest{

    @Override
    public Integer getToOverride() {
        return super.getToOverride();
    }

}


// FILE: test.kt

class KotlinChildClass : ClashTest() {

    override fun getToOverride(): Int? = super.getToOverride()

}

fun test() {
    konst obj = ClashTest()

    obj.getAge()
    //thats shouldn't work because lombok doesn't generate clashing method
    obj.setAge(<!CONSTANT_EXPECTED_TYPE_MISMATCH!>41<!>)
    <!VAL_REASSIGNMENT!>obj.age<!> = 12
    konst age = obj.age


    obj.getName()
    obj.setName("Al")
    konst name = obj.name
    obj.name = "sdf"

    obj.isHuman()
    obj.setHuman(true)
    obj.isHuman("sdf")
    konst isHuman = obj.isHuman
    obj.isHuman = false

    konst childObj = KotlinChildClass()
    childObj.getToOverride()
    childObj.setToOverride(34)
    childObj.toOverride
    childObj.toOverride = 412
}
