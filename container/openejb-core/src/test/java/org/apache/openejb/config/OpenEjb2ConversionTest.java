/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.openejb.config;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import junit.framework.TestCase;
import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.jee.JaxbJavaee;
import org.apache.openejb.jee.oejb3.OpenejbJar;
import org.apache.openejb.jee.jpa.EntityMappings;

/**
 * @version $Rev$ $Date$
 */
public class OpenEjb2ConversionTest extends TestCase {
    public void testItests22() throws Exception {
        convert("convert/oej2/cmp/itest-2.2/itest-2.2-");
    }

    public void testDaytrader() throws Exception {
        convert("convert/oej2/cmp/daytrader/daytrader-");
    }

    public void testOneToOne() throws Exception {
        convert("convert/oej2/cmp/onetoone/simplepk/");
    }

    public void testOneToOneUni() throws Exception {
        convert("convert/oej2/cmp/onetoone/simplepk/unidirectional-");
    }

    public void testOneToMany() throws Exception {
        convert("convert/oej2/cmp/onetomany/simplepk/");
    }

    public void testOneToManyUni() throws Exception {
        convert("convert/oej2/cmp/onetomany/simplepk/one-unidirectional-");
    }

    public void testManyToOneUni() throws Exception {
        convert("convert/oej2/cmp/onetomany/simplepk/many-unidirectional-");
    }

    public void testManyToMany() throws Exception {
        convert("convert/oej2/cmp/manytomany/simplepk/");
    }

    public void testManyToManyUni() throws Exception {
        convert("convert/oej2/cmp/manytomany/simplepk/unidirectional-");
    }

    private EntityMappings convert(String prefix) throws Exception {
        return convert(prefix + "ejb-jar.xml", prefix + "openejb-jar.xml", prefix + "orm.xml");
    }

    private EntityMappings convert(String ejbJarFileName, String openejbJarFileName, String expectedFileName) throws Exception {
        InputStream in = getClass().getClassLoader().getResourceAsStream(ejbJarFileName);
        EjbJar ejbJar = (EjbJar) JaxbJavaee.unmarshal(EjbJar.class, new ByteArrayInputStream(readContent(in).getBytes()));

        // create and configure the module
        EjbModule ejbModule = new EjbModule(getClass().getClassLoader(), "TestModule", ejbJarFileName, ejbJar, new OpenejbJar());
        InitEjbDeployments initEjbDeployments = new InitEjbDeployments();
        initEjbDeployments.deploy(ejbModule, new HashMap<String,String>());
        AppModule appModule = new AppModule(getClass().getClassLoader(), "TestModule");
        appModule.getEjbModules().add(ejbModule);

        // add the altDD
        ejbModule.getAltDDs().put("openejb-jar.xml", getClass().getClassLoader().getResource(openejbJarFileName));

        // convert the cmp declarations into jpa entity declarations
        CmpJpaConversion cmpJpaConversion = new CmpJpaConversion();
        cmpJpaConversion.deploy(appModule);
//        EntityMappings entityMappings = cmpJpaConversion.generateEntityMappings(ejbModule);

        // load the openejb-jar.xml file
//        String openejbJarXml = readContent(getClass().getClassLoader().getResourceAsStream(openejbJarFileName));
//        JAXBElement element = (JAXBElement) JaxbOpenejbJar2.unmarshal(OpenejbJarType.class, new ByteArrayInputStream(openejbJarXml.getBytes()));
//        OpenejbJarType openejbJarType = (OpenejbJarType) element.getValue();

        // fill in the jpa entity declarations with database mappings from the openejb-jar.xml file
        OpenEjb2Conversion openEjb2Conversion = new OpenEjb2Conversion();
        openEjb2Conversion.deploy(appModule);
//        openEjb2CmpConversion.mergeEntityMappings(entityMappings, openejbJarType);

        // compare the results to the expected results (direct text comparison)
        if (expectedFileName != null) {
            in = getClass().getClassLoader().getResourceAsStream(expectedFileName);
            String expected = readContent(in);
            String actual = toString(appModule.getCmpMappings());
            assertEquals(expected, actual);
        }
        return appModule.getCmpMappings();
    }


    private String toString(EntityMappings entityMappings) throws JAXBException {
        JAXBContext entityMappingsContext = JAXBContext.newInstance(EntityMappings.class);

        Marshaller marshaller = entityMappingsContext.createMarshaller();
        marshaller.setProperty("jaxb.formatted.output", true);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        marshaller.marshal(entityMappings, baos);

        String actual = new String(baos.toByteArray());
        return actual.trim();
    }


    private String readContent(InputStream in) throws IOException {
        StringBuffer sb = new StringBuffer();
        in = new BufferedInputStream(in);
        int i = in.read();
        while (i != -1) {
            sb.append((char) i);
            i = in.read();
        }
        return sb.toString().trim();
    }

}
