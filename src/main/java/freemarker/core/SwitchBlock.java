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

import freemarker.template.TemplateException;

/**
 * An instruction representing a switch-case structure.
 */
final class SwitchBlock extends TemplateElement {

    private Case defaultCase;
    private final Expression searched;

    /**
     * @param searched the expression to be tested.
     */
    SwitchBlock(Expression searched) {
        this.searched = searched;
        setRegulatedChildBufferCapacity(4);
    }

    /**
     * @param cas a Case element.
     */
    void addCase(Case cas) {
        if (cas.condition == null) {
            defaultCase = cas;
        }
        addRegulatedChild(cas);
    }

    @Override
    void accept(Environment env) 
        throws TemplateException, IOException {
        boolean processedCase = false;
        int ln = getRegulatedChildCount();
        try {
            for (int i = 0; i < ln; i++) {
                Case cas = (Case) getRegulatedChild(i);
                boolean processCase = false;

                // Fall through if a previous case tested true.
                if (processedCase) {
                    processCase = true;
                } else if (cas.condition != null) {
                    // Otherwise, if this case isn't the default, test it.
                    processCase = EvalUtil.compare(
                            searched,
                            EvalUtil.CMP_OP_EQUALS, "case==", cas.condition, cas.condition, env);
                }
                if (processCase) {
                    env.visitByHiddingParent(cas);
                    processedCase = true;
                }
            }

            // If we didn't process any nestedElements, and we have a default,
            // process it.
            if (!processedCase && defaultCase != null) {
                env.visitByHiddingParent(defaultCase);
            }
        } catch (BreakInstruction.Break br) {}
    }

    @Override
    protected String dump(boolean canonical) {
        StringBuilder buf = new StringBuilder();
        if (canonical) buf.append('<');
        buf.append(getNodeTypeSymbol());
        buf.append(' ');
        buf.append(searched.getCanonicalForm());
        if (canonical) {
            buf.append('>');
            int ln = getRegulatedChildCount();
            for (int i = 0; i < ln; i++) {
                Case cas = (Case) getRegulatedChild(i);
                buf.append(cas.getCanonicalForm());
            }
            buf.append("</").append(getNodeTypeSymbol()).append('>');
        }
        return buf.toString();
    }

    @Override
    String getNodeTypeSymbol() {
        return "#switch";
    }

    @Override
    int getParameterCount() {
        return 1;
    }

    @Override
    Object getParameterValue(int idx) {
        if (idx != 0) throw new IndexOutOfBoundsException();
        return searched;
    }

    @Override
    ParameterRole getParameterRole(int idx) {
        if (idx != 0) throw new IndexOutOfBoundsException();
        return ParameterRole.VALUE;
    }

    @Override
    boolean isNestedBlockRepeater() {
        return false;
    }
    
}
