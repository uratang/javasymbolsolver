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

package org.javaparser.symbolsolver;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseException;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.stmt.Statement;
import org.javaparser.symbolsolver.javaparsermodel.JavaParserFacade;
import org.javaparser.symbolsolver.model.declarations.TypeDeclaration;
import org.javaparser.symbolsolver.model.resolution.SymbolReference;
import org.javaparser.symbolsolver.model.resolution.TypeSolver;
import org.javaparser.symbolsolver.model.usages.typesystem.ReferenceType;
import org.javaparser.symbolsolver.model.usages.typesystem.Type;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

/**
 * It print information extracted from a source file. It is mainly intended as an example usage of JavaSymbolSolver.
 */
public class SourceFileInfoExtractor {

    private TypeSolver typeSolver;

    private int ok = 0;
    private int ko = 0;
    private int unsupported = 0;
    private boolean printFileName = true;
    private PrintStream out = System.out;
    private PrintStream err = System.err;

    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

    private boolean verbose = false;

    public void setPrintFileName(boolean printFileName) {
        this.printFileName = printFileName;
    }

    public void clear() {
        ok = 0;
        ko = 0;
        unsupported = 0;
    }

    public void setOut(PrintStream out) {
        this.out = out;
    }

    public void setErr(PrintStream err) {
        this.err = err;
    }

    public int getOk() {
        return ok;

    }

    public int getUnsupported() {
        return unsupported;
    }

    public int getKo() {
        return ko;
    }

    private void solveTypeDecl(ClassOrInterfaceDeclaration node) {
        TypeDeclaration typeDeclaration = JavaParserFacade.get(typeSolver).getTypeDeclaration(node);
        if (typeDeclaration.isClass()) {
            out.println("\n[ Class " + typeDeclaration.getQualifiedName() + " ]");
            for (ReferenceType sc : typeDeclaration.asClass().getAllSuperClasses()) {
                out.println("  superclass: " + sc.getQualifiedName());
            }
            for (ReferenceType sc : typeDeclaration.asClass().getAllInterfaces()) {
                out.println("  interface: " + sc.getQualifiedName());
            }
        }
    }

    private void solve(Node node) {
        if (node instanceof ClassOrInterfaceDeclaration) {
            solveTypeDecl((ClassOrInterfaceDeclaration) node);
        } else if (node instanceof Expression) {
            if ((node.getParentNode() instanceof ImportDeclaration) || (node.getParentNode() instanceof Expression)
                    || (node.getParentNode() instanceof MethodDeclaration)
                    || (node.getParentNode() instanceof PackageDeclaration)) {
                // skip
            } else if ((node.getParentNode() instanceof Statement) || (node.getParentNode() instanceof VariableDeclarator)) {
                try {
                    Type ref = JavaParserFacade.get(typeSolver).getType(node);
                    out.println("  Line " + node.getRange().begin.line + ") " + node + " ==> " + ref.describe());
                    ok++;
                } catch (UnsupportedOperationException upe) {
                    unsupported++;
                    err.println(upe.getMessage());
                    throw upe;
                } catch (RuntimeException re) {
                    ko++;
                    err.println(re.getMessage());
                    throw re;
                }
            }
        }
        for (Node child : node.getChildrenNodes()) {
            solve(child);
        }
    }

    private void solveMethodCalls(Node node) {
        if (node instanceof MethodCallExpr) {
            out.println("  Line " + node.getBegin().line + ") " + node + " ==> " + toString((MethodCallExpr)node));
        }
        for (Node child : node.getChildrenNodes()) {
            solveMethodCalls(child);
        }
    }

    private String toString(MethodCallExpr node) {
        try {
            return toString(JavaParserFacade.get(typeSolver).solve(node));
        } catch (Exception e) {
            if (verbose) {
                System.err.println("Error resolving call at L"+node.getBegin().line + ": "+node);
                e.printStackTrace();
            }
            return "ERROR";
        }
    }

    private String toString(SymbolReference<org.javaparser.symbolsolver.model.declarations.MethodDeclaration> methodDeclarationSymbolReference) {
        if (methodDeclarationSymbolReference.isSolved()) {
            return methodDeclarationSymbolReference.getCorrespondingDeclaration().getQualifiedSignature();
        } else {
            return "UNSOLVED";
        }
    }

    public void solve(File file) throws IOException, ParseException {
        if (file.isDirectory()) {
            for (File f : file.listFiles()) {
                solve(f);
            }
        } else {
            if (file.getName().endsWith(".java")) {
                if (printFileName) {
                    out.println("- parsing " + file.getAbsolutePath());
                }
                CompilationUnit cu = JavaParser.parse(file);
                solve(cu);
            }
        }
    }

    public void solveMethodCalls(File file) throws IOException, ParseException {
        if (file.isDirectory()) {
            for (File f : file.listFiles()) {
                solveMethodCalls(f);
            }
        } else {
            if (file.getName().endsWith(".java")) {
                if (printFileName) {
                    out.println("- parsing " + file.getAbsolutePath());
                }
                CompilationUnit cu = JavaParser.parse(file);
                solveMethodCalls(cu);
            }
        }
    }

    public void setTypeSolver(TypeSolver typeSolver) {
        this.typeSolver = typeSolver;
    }

}