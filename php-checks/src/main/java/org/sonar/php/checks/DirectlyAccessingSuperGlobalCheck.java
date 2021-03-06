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
import org.sonar.php.checks.utils.CheckUtils;
import org.sonar.plugins.php.api.tree.expression.VariableIdentifierTree;
import org.sonar.plugins.php.api.tree.lexical.SyntaxToken;
import org.sonar.plugins.php.api.visitors.PHPVisitorCheck;
import org.sonar.squidbridge.annotations.SqaleConstantRemediation;
import org.sonar.squidbridge.annotations.SqaleSubCharacteristic;

@Rule(
  key = DirectlyAccessingSuperGlobalCheck.KEY,
  name = "不应该直接访问超级全局变量",
  priority = Priority.MAJOR,
  tags = {Tags.SECURITY})
@SqaleSubCharacteristic(RulesDefinition.SubCharacteristics.INPUT_VALIDATION_AND_REPRESENTATION)
@SqaleConstantRemediation("15min")
public class DirectlyAccessingSuperGlobalCheck extends PHPVisitorCheck {

  private static final String MESSAGE = "Do not access \"%s\" directly.";
  public static final String KEY = "S2043";

  @Override
  public void visitVariableIdentifier(VariableIdentifierTree tree) {
    checkVariable(tree.variableExpression().token());
    super.visitVariableIdentifier(tree);
  }

  private void checkVariable(SyntaxToken variable) {
    String name = variable.text();

    if (CheckUtils.PREDEFINED_VARIABLES.values().contains(name)) {
      context().newIssue(this, String.format(MESSAGE, name)).tree(variable);
    }
  }

}
