/*
 * Copyright 2016 Federico Tomassetti
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.javaparser.symbolsolver.resolution.javaparser.contexts;

import com.github.javaparser.ParseException;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.MethodCallExpr;

import org.javaparser.symbolsolver.javaparser.Navigator;
import org.javaparser.symbolsolver.javaparsermodel.contexts.MethodCallExprContext;
import org.javaparser.symbolsolver.model.usages.MethodUsage;
import org.javaparser.symbolsolver.core.resolution.Context;
import org.javaparser.symbolsolver.resolution.AbstractResolutionTest;
import org.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import org.javaparser.symbolsolver.resolution.typesolvers.JavaParserTypeSolver;
import org.javaparser.symbolsolver.resolution.typesolvers.JreTypeSolver;
import org.junit.Test;

import java.io.File;
import java.util.Collections;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Malte Langkabel
 */
public class MethodCallExprContextResolutionTest extends AbstractResolutionTest {

    @Test
    public void solveNestedMethodCallExprContextWithoutScope() throws ParseException {
        CompilationUnit cu = parseSample("MethodCalls");

        com.github.javaparser.ast.body.ClassOrInterfaceDeclaration clazz = Navigator.demandClass(cu, "MethodCalls");
        MethodDeclaration method = Navigator.demandMethod(clazz, "bar1");
        MethodCallExpr methodCallExpr = Navigator.findMethodCall(method, "foo");
        
        File src = adaptPath(new File("src/test/resources"));
        CombinedTypeSolver combinedTypeSolver = new CombinedTypeSolver();
        combinedTypeSolver.add(new JreTypeSolver());
        combinedTypeSolver.add(new JavaParserTypeSolver(src));
        
        Context context = new MethodCallExprContext(methodCallExpr, combinedTypeSolver);
        
        Optional<MethodUsage> ref = context.solveMethodAsUsage("foo", Collections.emptyList(), combinedTypeSolver);
        assertTrue(ref.isPresent());
        assertEquals("MethodCalls", ref.get().declaringType().getQualifiedName());
    }
}