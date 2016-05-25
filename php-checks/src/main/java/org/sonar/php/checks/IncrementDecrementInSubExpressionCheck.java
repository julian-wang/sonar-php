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

import org.sonar.api.server.rule.RulesDefinition;
import org.sonar.check.Priority;
import org.sonar.check.Rule;
import org.sonar.plugins.php.api.tree.SeparatedList;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.Tree.Kind;
import org.sonar.plugins.php.api.tree.expression.ExpressionTree;
import org.sonar.plugins.php.api.tree.expression.UnaryExpressionTree;
import org.sonar.plugins.php.api.tree.statement.ExpressionStatementTree;
import org.sonar.plugins.php.api.tree.statement.ForStatementTree;
import org.sonar.plugins.php.api.visitors.PHPVisitorCheck;
import org.sonar.squidbridge.annotations.ActivatedByDefault;
import org.sonar.squidbridge.annotations.SqaleConstantRemediation;
import org.sonar.squidbridge.annotations.SqaleSubCharacteristic;

@Rule(
  key = IncrementDecrementInSubExpressionCheck.KEY,
  name = "自增自减运算符不应在方法调用中使用，也不应与其他运算符在表达式中混合使用",
  priority = Priority.MAJOR,
  tags = {Tags.CERT, Tags.MISRA})
@ActivatedByDefault
@SqaleSubCharacteristic(RulesDefinition.SubCharacteristics.LOGIC_RELIABILITY)
@SqaleConstantRemediation("5min")
public class IncrementDecrementInSubExpressionCheck extends PHPVisitorCheck {

  public static final String KEY = "S881";
  private static final String MESSAGE = "Extract this increment or decrement operator into a dedicated statement.";

  private static final Kind[] INC_DEC = {
    Kind.PREFIX_DECREMENT,
    Kind.PREFIX_INCREMENT,
    Kind.POSTFIX_DECREMENT,
    Kind.POSTFIX_INCREMENT};

  @Override
  public void visitExpressionStatement(ExpressionStatementTree tree) {
    if (!tree.expression().is(INC_DEC)) {
      super.visitExpressionStatement(tree);
    }
  }

  @Override
  public void visitForStatement(ForStatementTree tree) {
    scanExpressionList(tree.init());
    scanExpressionList(tree.condition());
    scanExpressionList(tree.update());

    scan(tree.statements());
  }

  private void scanExpressionList(SeparatedList<ExpressionTree> list) {
    for (ExpressionTree expression : list) {
      if (expression.is(INC_DEC)) {
        scan(((UnaryExpressionTree) expression).expression());
      } else {
        scan(expression);
      }
    }
  }

  @Override
  public void visitPostfixExpression(UnaryExpressionTree tree) {
    if (tree.is(INC_DEC)) {
      raiseIssue(tree);
    }
    super.visitPostfixExpression(tree);
  }

  @Override
  public void visitPrefixExpression(UnaryExpressionTree tree) {
    if (tree.is(INC_DEC)) {
      raiseIssue(tree);
    }
    super.visitPrefixExpression(tree);
  }

  private void raiseIssue(Tree tree) {
    context().newIssue(this, MESSAGE).tree(tree);
  }
}
