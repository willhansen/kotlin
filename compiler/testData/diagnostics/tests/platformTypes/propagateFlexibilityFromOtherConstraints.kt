// FIR_IDENTICAL
// FULL_JDK
// FILE: test.kt

@file:Suppress("UNUSED_PARAMETER")

import java.util.Comparator

abstract class DataView {
    abstract konst presentationName: String
}

fun <A> comboBox(
    model: SortedComboBoxModel<A>,
    graphProperty: GraphProperty<A>,
) {}

class GraphProperty<B>

fun test() {
    konst presentationName: (DataView) -> String = { it.presentationName }
    konst parentComboBoxModel/*: SortedComboBoxModel<DataView>*/ = SortedComboBoxModel(Comparator.comparing(presentationName))
    comboBox(parentComboBoxModel, GraphProperty<DataView>())
}

// FILE: SortedComboBoxModel.java

import java.util.Comparator;

public class SortedComboBoxModel<C> {
    public SortedComboBoxModel(Comparator<? super C> comparator) {
    }
}
