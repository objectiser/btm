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

import com.sun.jdi.ThreadReference;
import com.sun.jdi.event.MethodEntryEvent;
import com.sun.jdi.event.MethodExitEvent;

public class PathManager {

    private java.util.Map<ThreadReference, Node> _paths=new java.util.HashMap<ThreadReference, Node>();

    public void record(MethodEntryEvent entry) {
        Node currentNode=_paths.get(entry.thread());

        Node newNode=new Node(currentNode);

        newNode.setClassName(entry.method().declaringType().name());
        newNode.setMethodSignature(entry.method().name()+entry.method().signature());

        _paths.put(entry.thread(), newNode);
    }

    public void record(MethodExitEvent exit) {
        Node currentNode=_paths.get(exit.thread());

        if (currentNode != null) {
            String sig=exit.method().name()+exit.method().signature();

            String curSig;

            do {
                curSig = currentNode.getMethodSignature();

                if (!curSig.equals(sig)) {
                    System.err.println("SIGNATURE MISMATCH: entry "+currentNode.getMethodSignature()+" exit "+sig);
                }

                if (currentNode.getParent() == null) {
                    _paths.remove(exit.thread());

                    // Display path
                    StringBuffer buf=new StringBuffer();
                    currentNode.toString(buf, 0);

                    if (!buf.toString().startsWith("org.apache.camel.support.TimerListenerManager:run()V")) {
                        System.out.println("-------------------------------------\r\n"+buf.toString());

                        if (!curSig.equals(sig)) {
                            System.out.println(">>>> TRACE ABORTED due to mismatched signatures: expecting="+
                                            sig);
                        }
                    }
                } else {
                    _paths.put(exit.thread(), currentNode.getParent());
                    currentNode = currentNode.getParent();
                }
            } while (!curSig.equals(sig) && currentNode.getParent() != null);
        }
    }
}
