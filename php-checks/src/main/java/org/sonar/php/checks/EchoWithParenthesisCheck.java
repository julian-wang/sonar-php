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
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.expression.FunctionCallTree;
import org.sonar.squidbridge.annotations.ActivatedByDefault;
import org.sonar.squidbridge.annotations.SqaleConstantRemediation;
import org.sonar.squidbridge.annotations.SqaleSubCharacteristic;

@Rule(
  key = EchoWithParenthesisCheck.KEY,
  name = "不要使用括号调用\"echo\"",
  priority = Priority.MAJOR,
  tags = {Tags.PITFALL})
@ActivatedByDefault
@SqaleSubCharacteristic(RulesDefinition.SubCharacteristics.READABILITY)
@SqaleConstantRemediation("2min")
public class EchoWithParenthesisCheck extends FunctionUsageCheck {

  public static final String KEY = "S2041";
  private static final String MESSAGE = "Remove the parentheses from this \"echo\" call.";

  @Override
  protected ImmutableSet<String> functionNames() {
    return ImmutableSet.of("echo");
  }

  @Override
  protected void createIssue(FunctionCallTree tree) {
    if (isParenthesized(tree)) {
      context().newIssue(this, MESSAGE).tree(tree);
    }
  }

  private static boolean isParenthesized(FunctionCallTree tree) {
    return tree.arguments().size() == 1 && tree.arguments().get(0).is(Tree.Kind.PARENTHESISED_EXPRESSION);
  }

}
