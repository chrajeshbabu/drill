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
package org.apache.drill.exec.store.phoenix;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.sql.DataSource;

import org.apache.calcite.plan.RelOptRule;
import org.apache.calcite.rel.RelNode;
import org.apache.calcite.rel.convert.ConverterRule;
import org.apache.calcite.schema.SchemaPlus;
import org.apache.commons.dbcp.BasicDataSource;
import org.apache.drill.common.JSONOptions;
import org.apache.drill.common.expression.SchemaPath;
import org.apache.drill.exec.ops.OptimizerRulesContext;
import org.apache.drill.exec.physical.base.AbstractGroupScan;
import org.apache.drill.exec.planner.logical.DrillRel;
import org.apache.drill.exec.planner.physical.Prel;
import org.apache.drill.exec.server.DrillbitContext;
import org.apache.drill.exec.store.AbstractStoragePlugin;
import org.apache.drill.exec.store.SchemaConfig;
import org.apache.phoenix.calcite.PhoenixSchema;
import org.apache.phoenix.calcite.rel.PhoenixRel;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;

public class PhoenixStoragePlugin extends AbstractStoragePlugin {
  private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(PhoenixStoragePlugin.class);

  private static final ImmutableSet<RelOptRule> RULES;

  private final PhoenixStorageConfig config;
  private final DrillbitContext context;
  private final String name;
  private final BasicDataSource source;


  public PhoenixStoragePlugin(PhoenixStorageConfig config, DrillbitContext context, String name) {
    this.context = context;
    this.config = config;
    this.name = name;
    this.source = new BasicDataSource();
    source.setDriverClassName("org.apache.phoenix.jdbc.PhoenixDriver");
    source.setUrl(config.getUrl());
  }

  static {
    ImmutableSet.Builder<RelOptRule> builder = ImmutableSet.builder();
    builder.add(new PhoenixPrule());
    builder.add(new PhoenixDrelConverterRule());
    RULES = builder.build();
  }

  private static class PhoenixPrule extends ConverterRule {

    private PhoenixPrule() {
      super(PhoenixDrel.class, DrillRel.DRILL_LOGICAL, Prel.DRILL_PHYSICAL, "PHOENIX_PREL_Converter");
    }

    @Override
    public RelNode convert(RelNode in) {

      return new PhoenixIntermediatePrel(
          in.getCluster(),
          in.getTraitSet().replace(getOutTrait()),
          in.getInput(0));
    }

  }

  private static class PhoenixDrelConverterRule extends ConverterRule {

    public PhoenixDrelConverterRule() {
      super(RelNode.class, PhoenixRel.SERVER_CONVENTION, DrillRel.DRILL_LOGICAL, "PHOENIX_DREL_Converter");
    }

    @Override
    public RelNode convert(RelNode in) {
      return new PhoenixDrel(in.getCluster(), in.getTraitSet().replace(DrillRel.DRILL_LOGICAL),
          convert(in, in.getTraitSet().replace(this.getInTrait())));
    }

  }


  @Override
  public void registerSchemas(SchemaConfig config, SchemaPlus parent) {
    Map<String, Object> operand = Maps.newHashMap();
    operand.put("url", this.getConfig().getUrl());
    parent.add(name, PhoenixSchema.FACTORY.create(parent, name, operand));
  }

  @Override
  public PhoenixStorageConfig getConfig() {
    return config;
  }

  public DrillbitContext getContext() {
    return this.context;
  }

  public String getName() {
    return this.name;
  }

  @Override
  public boolean supportsRead() {
    return true;
  }

  public DataSource getSource() {
    return source;
  }

  @Override
  public AbstractGroupScan getPhysicalScan(String userName, JSONOptions selection, List<SchemaPath> columns)
      throws IOException {
    throw new UnsupportedOperationException();
  }

  @Override
  public Set<RelOptRule> getOptimizerRules(OptimizerRulesContext context) {
    return RULES;
  }
}
