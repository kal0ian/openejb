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
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.openejb.core.ivm.naming.java;

import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.Name;
import javax.naming.NamingException;
import javax.naming.spi.InitialContextFactory;
import javax.naming.spi.ObjectFactory;

import org.apache.openejb.core.CoreDeploymentInfo;
import org.apache.openejb.core.ThreadContext;
import org.apache.openejb.spi.ContainerSystem;
import org.apache.openejb.loader.SystemInstance;

public class javaURLContextFactory implements ObjectFactory, InitialContextFactory {

    public Context getInitialContext(Hashtable env) throws NamingException {
        return getContext();
    }

    public Object getObjectInstance(Object obj, Name name, Context nameCtx, Hashtable env)
            throws NamingException {
        if (obj == null) {
            /*
                  A null obj ref means the NamingManager is requesting
                  a Context that can resolve the 'java:' schema
               */
            return getContext();
        } else if (obj instanceof java.lang.String) {
            String string = (String) obj;
            if (string.startsWith("java:comp") || string.startsWith("java:openejb")) {
                /*
                     If the obj is a URL String with the 'java:' schema
                     resolve the URL in the context of this threads JNDI ENC
                     */
                string = string.substring(string.indexOf(':'));
                Context encRoot = getContext();
                return encRoot.lookup(string);
            }
        }
        return null;
    }

    public Object getObjectInstance(Object obj, Hashtable env)
            throws NamingException {
        return getContext();
    }

    public Context getContext() {
        Context jndiCtx = null;

        ThreadContext callContext = ThreadContext.getThreadContext();
        if (callContext == null) {
            ContainerSystem containerSystem = SystemInstance.get().getComponent(ContainerSystem.class);
            return containerSystem.getJNDIContext();
        }

        CoreDeploymentInfo di = callContext.getDeploymentInfo();
        if (di != null) {
            return di.getJndiEnc();
        } else {
            ContainerSystem containerSystem = SystemInstance.get().getComponent(ContainerSystem.class);
            return containerSystem.getJNDIContext();
        }
    }
}
