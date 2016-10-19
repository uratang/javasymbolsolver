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

package org.javaparser.symbolsolver.javaparsermodel.declarators;

import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.expr.VariableDeclarationExpr;
import org.javaparser.symbolsolver.javaparsermodel.declarations.JavaParserSymbolDeclaration;
import org.javaparser.symbolsolver.model.declarations.ValueDeclaration;
import org.javaparser.symbolsolver.model.resolution.TypeSolver;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class VariableSymbolDeclarator extends AbstractSymbolDeclarator<VariableDeclarationExpr> {

    public VariableSymbolDeclarator(VariableDeclarationExpr wrappedNode, TypeSolver typeSolver) {
        super(wrappedNode, typeSolver);
        if (wrappedNode.getParentNode() instanceof FieldDeclaration) {
            throw new IllegalArgumentException();
        }
    }

    @Override
    public List<ValueDeclaration> getSymbolDeclarations() {
        List<ValueDeclaration> symbols = wrappedNode.getVars().stream().map(
                v -> JavaParserSymbolDeclaration.localVar(v, typeSolver)
        ).collect(
                Collectors.toCollection(() -> new LinkedList<>()));
        return symbols;
    }

}