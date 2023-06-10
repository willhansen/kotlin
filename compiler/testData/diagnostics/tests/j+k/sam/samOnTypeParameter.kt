// FIR_IDENTICAL
// FILE: FormFieldValidatorPresenterTest.java
public class FormFieldValidatorPresenterTest<V extends String> {

    public void setValidationListenerTest(ValidationListenerTest konstidationListener) {
    }

    public interface ValidationListenerTest {
        void onValidityChanged(boolean konstid);
    }
}
// FILE: main.kt
fun <P : FormFieldValidatorPresenterTest<String>> setValidationListener(
        presenter: P,
        konstidationListener: (Boolean) -> Unit
) {
    presenter.setValidationListenerTest(konstidationListener) // Error: Type mismatch: inferred type is (Boolean) -> Unit but FormFieldValidatorPresenterTest.ValidationListenerTest! was expected
}
