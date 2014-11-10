/*******************************************************************************

 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package org.apache.drill.exec.vector.complex.fn;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.drill.common.expression.PathSegment;
import org.apache.drill.common.expression.SchemaPath;

import com.google.common.collect.ImmutableMap;

/**
 * This class manages the projection pushdown for a complex path.
 */
class FieldSelection {

  public static final FieldSelection INVALID_NODE = new FieldSelection(null, ValidityMode.NEVER_VALID);
  public static final FieldSelection ALL_VALID = new FieldSelection(null, ValidityMode.ALWAYS_VALID);

  private enum ValidityMode {CHECK_CHILDREN, NEVER_VALID, ALWAYS_VALID}

  private final Map<String, FieldSelection> children;
  private final ValidityMode mode;

  private FieldSelection(){
    this(new HashMap<String, FieldSelection>(), ValidityMode.CHECK_CHILDREN);
  }

  private FieldSelection(Map<String, FieldSelection> children, ValidityMode mode){
    this.children = children;
    this.mode = mode;
  }

  /**
   * Create a new tree that has all leaves fixed to support full depth validity.
   */
  private FieldSelection fixNodes(){
    if(children.isEmpty()){
      return ALL_VALID;
    }else{
      ImmutableMap.Builder<String, FieldSelection> newMap = ImmutableMap.builder();
      for(Map.Entry<String, FieldSelection> entry : children.entrySet()){
        newMap.put(entry.getKey(), entry.getValue().fixNodes());
      }
      return new FieldSelection(newMap.build(), ValidityMode.CHECK_CHILDREN);
    }
  }

  private FieldSelection addChild(String name){
    if(children.containsKey(name)){
      return children.get(name);
    }

    FieldSelection n = new FieldSelection();
    children.put(name, n);
    return n;
  }

  private void add(PathSegment segment){
    if(segment.isNamed()){
      FieldSelection child = addChild(segment.getNameSegment().getPath());
      if(!segment.isLastPath()){
        child.add(segment.getChild());
      }
    }
  }

  public boolean isNeverValid(){
    return mode == ValidityMode.NEVER_VALID;
  }

  public FieldSelection getChild(String name){
    switch(mode){
    case ALWAYS_VALID:
      return ALL_VALID;
    case CHECK_CHILDREN:
      FieldSelection n = children.get(name);
      if(n == null){
        return INVALID_NODE;
      }else{
        return n;
      }
    case NEVER_VALID:
      return INVALID_NODE;
    default:
      throw new IllegalStateException();

    }
  }

  private static boolean containsStar(List<SchemaPath> columns) {
    for (SchemaPath expr : columns) {
      if (expr.getRootSegment().getPath().equals("*")) {
        return true;
      }
    }
    return false;
  }

  /**
   * Generates a field selection based on a list of fields.  Assumes that a partial path a.b is equivalent to a.b.*
   * @param fields
   * @return
   */
  public static FieldSelection getFieldSelection(List<SchemaPath> fields){
    if(containsStar(fields)){
      return ALL_VALID;
    }else{
      FieldSelection root = new FieldSelection();
      for(SchemaPath p : fields){
        root.add(p.getRootSegment());
      }
      return root.fixNodes();
    }
  }

}