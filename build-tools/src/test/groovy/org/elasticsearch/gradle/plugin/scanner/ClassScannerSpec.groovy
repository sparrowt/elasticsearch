/*
 * Copyright Elasticsearch B.V. and/or licensed to Elasticsearch B.V. under one
 * or more contributor license agreements. Licensed under the Elastic License
 * 2.0 and the Server Side Public License, v 1; you may not use this file except
 * in compliance with, at your election, the Elastic License 2.0 or the Server
 * Side Public License, v 1.
 */

package org.elasticsearch.gradle.plugin.scanner

import spock.lang.Specification

import org.elasticsearch.plugin.api.Extensible
import org.hamcrest.Matchers
import org.objectweb.asm.ClassReader
import org.objectweb.asm.Type

import java.nio.file.Path
import java.nio.file.Paths
import java.util.stream.Stream

import static org.hamcrest.MatcherAssert.assertThat

class ClassScannerSpec extends Specification {
    def "class and interface hierarchy is scanned"() {
        given:
        def reader = new ClassScanner(
            Type.getDescriptor(Extensible.class), (classname, map) -> {
            map.put(classname, classname)
            return null
        }
        )
        Stream<ClassReader> classReaderStream = ofClassPath()

        when:
        reader.visit(classReaderStream);
        Map<String, String> extensibleClasses = reader.getFoundClasses()

        then:
        assertThat(
            extensibleClasses,
            Matchers.allOf(
                Matchers.hasEntry(
                    "org/elasticsearch/plugin/scanner/test_classes/ExtensibleClass",
                    "org/elasticsearch/plugin/scanner/test_classes/ExtensibleClass"
                ),
                Matchers.hasEntry(
                    "org/elasticsearch/plugin/scanner/test_classes/ImplementingExtensible",
                    "org/elasticsearch/plugin/scanner/test_classes/ExtensibleInterface"
                ),
                Matchers.hasEntry(
                    "org/elasticsearch/plugin/scanner/test_classes/SubClass",
                    "org/elasticsearch/plugin/scanner/test_classes/ExtensibleClass"
                )
            )
        );
    }

    static Stream<ClassReader> ofClassPath() throws IOException {
        String classpath = System.getProperty("java.class.path");
        return ofClassPath(classpath);
    }

    static Stream<ClassReader> ofClassPath(String classpath) {
        if (classpath != null && classpath.equals("") == false) {// todo when do we set cp to "" ?
            String[] pathelements = classpath.split(":");
            return ClassReaders.ofPaths(Arrays.stream(pathelements).map(Paths::get));
        }
        return Stream.empty();
    }

}
