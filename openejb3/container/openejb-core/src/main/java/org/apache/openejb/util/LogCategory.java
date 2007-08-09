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
package org.apache.openejb.util;
/**
 * Contains Logger categories used in OpenEJB. Be careful when adding new Categories. For example, if a new Category
 * named OpenEJB.shutdown needs to be added, then the following is not a recommended way
 * public static final LogCategory OPENEJB_SHUTDOWN = new LogCategory("OpenEJB.shutdown");
 * The above is not recommended because the above logger has a parent logger in OpenEJB. If we change the Parent logger
 * category i.e. lets say to OPENEJB (all uppercase), then to maintain the parent-child relationship, we will need
 * to change other loggers too. For example, we will not need to change OPENEJB_STARTUP and OPENEJB_SERVER because 
 * of the way they are defined (using OPENEJB.name as a prefix).
 * A better way of adding the Category would be
 * public static final LogCategory OPENEJB_SHUTDOWN = new LogCategory( OPENEJB.name + ".shutdown");
 * 
 *
 */
public final class LogCategory {
	private final String name;
	public static final LogCategory OPENEJB = new LogCategory( "OpenEJB");
	public static final LogCategory OPENEJB_ADMIN = new LogCategory( OPENEJB.name + ".admin");
	public static final LogCategory OPENEJB_STARTUP = new LogCategory( OPENEJB.name + ".startup");
	public static final LogCategory OPENEJB_STARTUP_CONFIG = new LogCategory( OPENEJB_STARTUP.name+".config");
	public static final LogCategory OPENEJB_STARTUP_VALIDATION = new LogCategory( OPENEJB_STARTUP.name+".validation");
	public static final LogCategory OPENEJB_SERVER = new LogCategory( OPENEJB.name + ".server");
	public static final LogCategory OPENEJB_SERVER_REMOTE = new LogCategory( OPENEJB_SERVER.name + ".remote");
	public static final LogCategory OPENEJB_SECURITY = new LogCategory( OPENEJB.name+".security");
	public static final LogCategory OPENEJB_RESOURCE_JDBC = new LogCategory( OPENEJB.name+".resource.jdbc");
	public static final LogCategory OPENEJB_CONNECTOR = new LogCategory( OPENEJB.name+".connector");
	public static final LogCategory OPENEJB_DEPLOY = new LogCategory( OPENEJB.name+".deploy");
	public static final LogCategory TRANSACTION = new LogCategory( "Transaction");
	public static final LogCategory ACTIVEMQ = new LogCategory( "org.apache.activemq");
	public static final LogCategory GERONIMO = new LogCategory( "org.apache.geronimo");
	public static final LogCategory OPENJPA = new LogCategory( "openjpa");
	public static final LogCategory CORBA_ADAPTER = new LogCategory( "CORBA-Adapter");
	public static final LogCategory TIMER = new LogCategory( "Timer");
	public static final LogCategory HTTPSERVER = new LogCategory( "HttpServer");
	public static final LogCategory SERVICEPOOL = new LogCategory( "ServicePool");
	private LogCategory(String name){
		this.name = name;
	}
	public String getName() {
		return name;
	}
	/**
	 * Creates a child category of this category. <B>Use this method sparingly</B>. This method is to be used in only those circumstances where the name of the 
	 * category is not known upfront and is a derived name. If you know the name of the category, it is highly recommended to add a static final field 
	 * of type LogCategory in this class
	 * @param child
	 * @return - LogCategory
	 */
	public LogCategory createChild(String child){
		return new LogCategory(this.name+"."+child);
	}

}
