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

public class Node {

    private java.util.List<Node> _children=new java.util.ArrayList<Node>();
    private Node _parent;
    private String _methodSignature;
    private String _className;

    public Node(Node parent) {
        _parent = parent;

        if (parent != null) {
            parent._children.add(this);
        }
    }

    public Node getParent() {
        return (_parent);
    }

    public java.util.List<Node> getChildren() {
        return (_children);
    }

    public void setClassName(String name) {
        _className = name;
    }

    public String getClassName() {
        return (_className);
    }

    public void setMethodSignature(String sig) {
        _methodSignature = sig;
    }

    public String getMethodSignature() {
        return (_methodSignature);
    }

    public void toString(StringBuffer buf, int indent) {
        for (int i=0; i < indent; i++) {
            buf.append("\t");
        }
        buf.append(getClassName());
        buf.append(":");
        buf.append(getMethodSignature());
        buf.append("\r\n");

        for (Node node : _children) {
            node.toString(buf, indent+1);
        }
    }
}
