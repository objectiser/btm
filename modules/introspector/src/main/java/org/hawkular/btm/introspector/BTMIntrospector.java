/*
 * Copyright 2015 Red Hat, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.hawkular.btm.introspector;

import java.io.IOException;
import java.util.Map;

import com.sun.jdi.Bootstrap;
import com.sun.jdi.VirtualMachine;
import com.sun.jdi.VirtualMachineManager;
import com.sun.jdi.connect.AttachingConnector;
import com.sun.jdi.connect.Connector;
import com.sun.jdi.connect.IllegalConnectorArgumentsException;
import com.sun.jdi.event.Event;
import com.sun.jdi.event.EventQueue;
import com.sun.jdi.event.EventSet;
import com.sun.jdi.event.MethodEntryEvent;
import com.sun.jdi.event.MethodExitEvent;
import com.sun.jdi.request.EventRequestManager;
import com.sun.jdi.request.MethodEntryRequest;
import com.sun.jdi.request.MethodExitRequest;

public class BTMIntrospector {

    private static final java.util.Set<String> EXCLUDE=new java.util.HashSet<String>();

    static {
        EXCLUDE.add("java.*");
        EXCLUDE.add("javax.*");
        EXCLUDE.add("sun.*");
        EXCLUDE.add("com.sun.*");
        EXCLUDE.add("org.jboss.modules.*");
        EXCLUDE.add("org.jboss.dmr.*");
        EXCLUDE.add("org.jboss.as.*");
        EXCLUDE.add("org.jboss.msc.*");
        EXCLUDE.add("org.jboss.weld.*");
        EXCLUDE.add("org.jboss.logging.*");
        EXCLUDE.add("org.jboss.logmanager.*");
        EXCLUDE.add("org.apache.commons.*");
        EXCLUDE.add("org.slf4j.*");
        EXCLUDE.add("org.xnio.*");
        EXCLUDE.add("com.arjuna.*");
        EXCLUDE.add("io.undertow.*");
        EXCLUDE.add("org.wildfly.*");
        EXCLUDE.add("com.ctc.*");
        EXCLUDE.add("org.apache.xerces.*");
        EXCLUDE.add("com.ibm.wsdl.*");
        EXCLUDE.add("com.google.common.*");
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        // connect
        VirtualMachine vm = connect(8787);

        EventRequestManager erm = vm.eventRequestManager();

        MethodEntryRequest methodEntryReq0 = erm.createMethodEntryRequest();
        for (String exclude : EXCLUDE) {
            methodEntryReq0.addClassExclusionFilter(exclude);
        }
        methodEntryReq0.enable();

/*
        MethodEntryRequest methodEntryReq1 = erm.createMethodEntryRequest();
        methodEntryReq1.addClassFilter("org.apache.camel.*");
        methodEntryReq1.enable();

        MethodEntryRequest methodEntryReq2 = erm.createMethodEntryRequest();
        methodEntryReq2.addClassFilter("org.switchyard.*");
        methodEntryReq2.enable();

        MethodEntryRequest methodEntryReq3 = erm.createMethodEntryRequest();
        methodEntryReq3.addClassFilter("org.jboss.resteasy.*");
        methodEntryReq3.enable();
*/

        MethodExitRequest methodExitReq0 = erm.createMethodExitRequest();
        for (String exclude : EXCLUDE) {
            methodExitReq0.addClassExclusionFilter(exclude);
        }
        methodExitReq0.enable();
/*
        MethodExitRequest methodExitReq1 = erm.createMethodExitRequest();
        methodExitReq1.addClassFilter("org.apache.camel.*");
        methodExitReq1.enable();

        MethodExitRequest methodExitReq2 = erm.createMethodExitRequest();
        methodExitReq2.addClassFilter("org.switchyard.*");
        methodExitReq2.enable();

        MethodExitRequest methodExitReq3 = erm.createMethodExitRequest();
        methodExitReq3.addClassFilter("org.jboss.resteasy.*");
        methodExitReq3.enable();
*/

        // resume the vm
        vm.resume();

        PathManager pm=new PathManager();

        // process events
        EventQueue eventQueue = vm.eventQueue();
        while (true) {
            EventSet eventSet = eventQueue.remove();
            for (Event event : eventSet) {
                if (event instanceof MethodEntryEvent) {
                    pm.record((MethodEntryEvent)event);
                } else if (event instanceof MethodExitEvent) {
                    pm.record((MethodExitEvent)event);
                }
            }
            eventSet.resume();
        }
    }

    /**
     * Call this with the localhost port to connect to.
     */
    public static VirtualMachine connect(int port) throws IOException {
        String strPort = Integer.toString(port);
        AttachingConnector connector = getConnector();
        try {
            VirtualMachine vm = connect(connector, strPort);
            return vm;
        } catch (IllegalConnectorArgumentsException e) {
            throw new IllegalStateException(e);
        }
    }

    private static AttachingConnector getConnector() {
        VirtualMachineManager vmManager = Bootstrap.virtualMachineManager();
        for (Connector connector : vmManager.attachingConnectors()) {
            System.out.println(connector.name());
            if ("com.sun.jdi.SocketAttach".equals(connector.name())) {
                return (AttachingConnector) connector;
            }
        }
        throw new IllegalStateException();
    }

    private static VirtualMachine connect(AttachingConnector connector, String port)
                    throws IllegalConnectorArgumentsException, IOException {
        Map<String, Connector.Argument> args = connector.defaultArguments();
        Connector.Argument pidArgument = args.get("port");
        if (pidArgument == null) {
            throw new IllegalStateException();
        }
        pidArgument.setValue(port);

        return connector.attach(args);
    }
}