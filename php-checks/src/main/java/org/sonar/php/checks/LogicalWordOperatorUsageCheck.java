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

import com.google.common.collect.ImmutableList;
import org.sonar.api.server.rule.RulesDefinition;
import org.sonar.check.Priority;
import org.sonar.check.Rule;
import org.sonar.php.api.PHPPunctuator;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.Tree.Kind;
import org.sonar.plugins.php.api.tree.expression.BinaryExpressionTree;
import org.sonar.plugins.php.api.tree.lexical.SyntaxToken;
import org.sonar.plugins.php.api.visitors.PHPSubscriptionCheck;
import org.sonar.squidbridge.annotations.ActivatedByDefault;
import org.sonar.squidbridge.annotations.SqaleConstantRemediation;
import org.sonar.squidbridge.annotations.SqaleSubCharacteristic;

import java.util.List;

@Rule(
  key = LogicalWordOperatorUsageCheck.KEY,
  name = "应该使用\"&&\" 和 \"||\"",
  priority = Priority.MAJOR,
  tags = {Tags.SUSPICIOUS})
@ActivatedByDefault
@SqaleSubCharacteristic(RulesDefinition.SubCharacteristics.LOGIC_RELIABILITY)
@SqaleConstantRemediation("5min")
public class LogicalWordOperatorUsageCheck extends PHPSubscriptionCheck {

  public static final String KEY = "S2010";
  public static final String MESSAGE = "Replace \"%s\" with \"%s\".";

  @Override
  public List<Kind> nodesToVisit() {
    return ImmutableList.of(
      Kind.ALTERNATIVE_CONDITIONAL_AND,
      Kind.ALTERNATIVE_CONDITIONAL_OR);
  }

  @Override
  public void visitNode(Tree tree) {
    SyntaxToken operator = ((BinaryExpressionTree) tree).operator();
    String replacement = tree.is(Kind.ALTERNATIVE_CONDITIONAL_AND)
      ? PHPPunctuator.ANDAND.getValue()
      : PHPPunctuator.OROR.getValue();

    context().newIssue(this, String.format(MESSAGE, operator.text(), replacement))
      .tree(operator);
  }
}
