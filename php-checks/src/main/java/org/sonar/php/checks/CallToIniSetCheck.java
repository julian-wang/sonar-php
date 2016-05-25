/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2016 SonarSource SA
 * mailto:contact AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.sonar.php.checks;

import com.google.common.collect.ImmutableSet;
import org.sonar.api.server.rule.RulesDefinition;
import org.sonar.check.Priority;
import org.sonar.check.Rule;
import org.sonar.php.checks.utils.FunctionUsageCheck;
import org.sonar.plugins.php.api.tree.expression.FunctionCallTree;
import org.sonar.squidbridge.annotations.SqaleConstantRemediation;
import org.sonar.squidbridge.annotations.SqaleSubCharacteristic;

@Rule(
  key = CallToIniSetCheck.KEY,
  name = "配置不应该动态被改变",
  priority = Priority.CRITICAL,
  tags = {Tags.PITFALL})
@SqaleSubCharacteristic(RulesDefinition.SubCharacteristics.INSTRUCTION_RELIABILITY)
@SqaleConstantRemediation("5min")
public class CallToIniSetCheck extends FunctionUsageCheck {

  public static final String KEY = "S2918";
  private static final String MESSAGE = "Move this configuration into a configuration file.";

  @Override
  protected ImmutableSet<String> functionNames() {
    return ImmutableSet.of("ini_set");
  }

  @Override
  protected void createIssue(FunctionCallTree tree) {
    context().newIssue(this, MESSAGE).tree(tree.callee());
  }

}
