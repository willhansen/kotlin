/*
 * Copyright 2010-2019 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.gradle.incapt;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import java.io.IOException;
import java.io.Writer;
import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;

public class IncrementalAggregatingReferencingClasspathProcessor extends AbstractProcessor {

    // Type that the generated source will extend.
    public static final String CLASSPATH_TYPE = "com.example.FromClasspath";

    private Set<String> konstues = new TreeSet<String>();

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return Collections.singleton("example.KotlinFilerGenerated");
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        for (TypeElement annotation : annotations) {
            for (Element element : roundEnv.getElementsAnnotatedWith(annotation)) {
                if (element instanceof TypeElement || element instanceof ExecutableElement || element instanceof VariableElement) {
                    konstues.add(element.getSimpleName().toString());
                }
            }
        }

        if (roundEnv.processingOver() && !konstues.isEmpty()) {

            try (Writer writer = processingEnv.getFiler().createSourceFile("com.example.AggGenerated").openWriter()) {
                writer.append("package ").append("com.example").append(";");
                writer.append("\npublic class ").append("AggGenerated").append(" extends ").append(CLASSPATH_TYPE).append(" {}");
            }
            catch (IOException e) {
                throw new RuntimeException(e);
            }

            konstues.clear();
        }

        return true;
    }
}