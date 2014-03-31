/**
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
 */
package org.apache.drill.exec.store.dfs;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.hydromatic.optiq.Function;
import net.hydromatic.optiq.Schema;
import net.hydromatic.optiq.SchemaPlus;

import org.apache.drill.exec.planner.logical.DrillTable;
import org.apache.drill.exec.store.AbstractSchema;
import org.apache.drill.exec.store.SchemaFactory;
import org.apache.drill.exec.store.dfs.WorkspaceSchemaFactory.WorkspaceSchema;

import com.google.common.collect.Maps;


/**
 * This is the top level schema that responds to root level path requests. Also supports
 */
public class FileSystemSchemaFactory implements SchemaFactory{
  static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(FileSystemSchemaFactory.class);

  private List<WorkspaceSchemaFactory> factories;
  private String schemaName;
  
  
  public FileSystemSchemaFactory(String schemaName, List<WorkspaceSchemaFactory> factories) {
    super();
    this.schemaName = schemaName;
    this.factories = factories;
  }

  @Override
  public Schema add(SchemaPlus parent) {
    FileSystemSchema schema = new FileSystemSchema(schemaName);
    SchemaPlus plusOfThis = parent.add(schema.getName(), schema);
    schema.setPlus(plusOfThis);
    return schema;
  }

  public class FileSystemSchema extends AbstractSchema{

    private final WorkspaceSchema defaultSchema;
    private final Map<String, WorkspaceSchema> schemaMap = Maps.newHashMap();
    
    public FileSystemSchema(String name) {
      super(name);
      for(WorkspaceSchemaFactory f :  factories){
        WorkspaceSchema s = f.createSchema();
        schemaMap.put(s.getName(), s);
      }
      
      defaultSchema = schemaMap.get("default");
    }

    void setPlus(SchemaPlus plusOfThis){
      for(WorkspaceSchema s : schemaMap.values()){
        plusOfThis.add(s.getName(), s);
      }
    }
    
    @Override
    public DrillTable getTable(String name) {
      return defaultSchema.getTable(name);
    }

    @Override
    public Collection<Function> getFunctions(String name) {
      return defaultSchema.getFunctions(name);
    }

    @Override
    public Set<String> getFunctionNames() {
      return defaultSchema.getFunctionNames();
    }

    @Override
    public Schema getSubSchema(String name) {
      return defaultSchema.getSubSchema(name);
    }

    @Override
    public Set<String> getSubSchemaNames() {
      return defaultSchema.getSubSchemaNames();
    }

    @Override
    public Set<String> getTableNames() {
      return defaultSchema.getTableNames();
    }
    
  }

}
