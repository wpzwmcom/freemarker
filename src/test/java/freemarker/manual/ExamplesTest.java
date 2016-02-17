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
package freemarker.manual;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.junit.Ignore;

import freemarker.cache.ClassTemplateLoader;
import freemarker.cache.MultiTemplateLoader;
import freemarker.cache.StringTemplateLoader;
import freemarker.cache.TemplateLoader;
import freemarker.template.Configuration;
import freemarker.test.TemplateTest;

@Ignore
public abstract class ExamplesTest extends TemplateTest {

    protected Properties loadPropertiesFile(String name) throws IOException {
        Properties props = new Properties();
        InputStream in = this.getClass().getResourceAsStream(name);
        try {
            props.load(in);
        } finally {
            in.close();
        }
        return props;
    }
    
    @Override
    protected final Configuration createConfiguration() {
        Configuration cfg = new Configuration(Configuration.getVersion());
        setupTemplateLoaders(cfg);
        return cfg;
    }

    protected void setupTemplateLoaders(Configuration cfg) {
        cfg.setTemplateLoader(new MultiTemplateLoader(
                new TemplateLoader[] { new StringTemplateLoader(), new ClassTemplateLoader(this.getClass(), "") }));
    }
    
}
