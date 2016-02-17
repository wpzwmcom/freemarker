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

import java.util.Locale;
import java.util.TimeZone;

import freemarker.template.TemplateModelException;

class ISOTemplateDateFormatFactory extends ISOLikeTemplateDateFormatFactory {
    
    static final ISOTemplateDateFormatFactory INSTANCE = new ISOTemplateDateFormatFactory();

    private ISOTemplateDateFormatFactory() {
        // Not meant to be instantiated
    }

    @Override
    public TemplateDateFormat get(int dateType, boolean zonelessInput, String params, Locale locale, TimeZone timeZone,
            Environment env) throws TemplateModelException, UnknownDateTypeFormattingUnsupportedException,
                    InvalidFormatParametersException {
        // We don't cache these as creating them is cheap (only 10% speedup of ${d?string.xs} with caching)
        return new ISOTemplateDateFormat(
                params, 3,
                dateType, zonelessInput,
                timeZone, this, env);
    }

}
