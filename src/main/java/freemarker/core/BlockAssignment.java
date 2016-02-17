/*
 * Copyright 2014 Attila Szegedi, Daniel Dekany, Jonathan Revusky
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

package freemarker.core;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Map;

import freemarker.template.SimpleScalar;
import freemarker.template.TemplateException;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;
import freemarker.template.TemplateTransformModel;

/**
 * Like [#local x]...[/#local].
 */
final class BlockAssignment extends TemplateElement {

    private final String varName;
    private final Expression namespaceExp;
    private final int scope;
    private final MarkupOutputFormat<?> markupOutputFormat;

    BlockAssignment(TemplateElement nestedBlock, String varName, int scope, Expression namespaceExp, MarkupOutputFormat<?> markupOutputFormat) {
        setNestedBlock(nestedBlock);
        this.varName = varName;
        this.namespaceExp = namespaceExp;
        this.scope = scope;
        this.markupOutputFormat = markupOutputFormat;
    }

    @Override
    void accept(Environment env) throws TemplateException, IOException {
        if (getNestedBlock() != null) {
            env.visitAndTransform(getNestedBlock(), new CaptureOutput(env), null);
        } else {
            TemplateModel value = capturedStringToModel("");
            if (namespaceExp != null) {
                Environment.Namespace ns = (Environment.Namespace) namespaceExp.eval(env);
                ns.put(varName, value);
            } else if (scope == Assignment.NAMESPACE) {
                env.setVariable(varName, value);
            } else if (scope == Assignment.GLOBAL) {
                env.setGlobalVariable(varName, value);
            } else if (scope == Assignment.LOCAL) {
                env.setLocalVariable(varName, value);
            }
        }
    }

    private TemplateModel capturedStringToModel(String s) throws TemplateModelException {
        return markupOutputFormat == null ? new SimpleScalar(s) : markupOutputFormat.fromMarkup(s);
    }

    private class CaptureOutput implements TemplateTransformModel {
        private final Environment env;
        private final Environment.Namespace fnsModel;
        
        CaptureOutput(Environment env) throws TemplateException {
            this.env = env;
            TemplateModel nsModel = null;
            if (namespaceExp != null) {
                nsModel = namespaceExp.eval(env);
                if (!(nsModel instanceof Environment.Namespace)) {
                    throw new NonNamespaceException(namespaceExp, nsModel, env);
                }
            }
            fnsModel = (Environment.Namespace ) nsModel; 
        }
        
        public Writer getWriter(Writer out, Map args) {
            return new StringWriter() {
                @Override
                public void close() throws IOException {
                    TemplateModel result;
                    try {
                        result = capturedStringToModel(toString());
                    } catch (TemplateModelException e) {
                        // [Java 1.6] e to cause
                        throw new IOException("Failed to create FTL value from captured string: " + e);
                    }
                    switch(scope) {
                        case Assignment.NAMESPACE: {
                            if (fnsModel != null) {
                                fnsModel.put(varName, result);
                            } else {
                                env.setVariable(varName, result);
                            }
                            break;
                        }
                        case Assignment.LOCAL: {
                            env.setLocalVariable(varName, result);
                            break;
                        }
                        case Assignment.GLOBAL: {
                            env.setGlobalVariable(varName, result);
                            break;
                        }
                    }
                }
            };
        }
    }
    
    @Override
    protected String dump(boolean canonical) {
        StringBuilder sb = new StringBuilder();
        if (canonical) sb.append("<");
        sb.append(getNodeTypeSymbol());
        sb.append(' ');
        sb.append(varName);
        if (namespaceExp != null) {
            sb.append(" in ");
            sb.append(namespaceExp.getCanonicalForm());
        }
        if (canonical) {
            sb.append('>');
            sb.append(getNestedBlock() == null ? "" : getNestedBlock().getCanonicalForm());
            sb.append("</");
            sb.append(getNodeTypeSymbol());
            sb.append('>');
        } else {
            sb.append(" = .nested_output");
        }
        return sb.toString();
    }
    
    @Override
    String getNodeTypeSymbol() {
        return Assignment.getDirectiveName(scope);
    }
    
    @Override
    int getParameterCount() {
        return 3;
    }

    @Override
    Object getParameterValue(int idx) {
        switch (idx) {
        case 0: return varName;
        case 1: return Integer.valueOf(scope);
        case 2: return namespaceExp;
        default: throw new IndexOutOfBoundsException();
        }
    }

    @Override
    ParameterRole getParameterRole(int idx) {
        switch (idx) {
        case 0: return ParameterRole.ASSIGNMENT_TARGET;
        case 1: return ParameterRole.VARIABLE_SCOPE;
        case 2: return ParameterRole.NAMESPACE;
        default: throw new IndexOutOfBoundsException();
        }
    }

    @Override
    boolean isNestedBlockRepeater() {
        return false;
    }
}
