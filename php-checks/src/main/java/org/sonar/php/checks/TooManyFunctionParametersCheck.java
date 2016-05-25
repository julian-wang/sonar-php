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
import org.sonar.check.RuleProperty;
import org.sonar.plugins.php.api.tree.declaration.ClassDeclarationTree;
import org.sonar.plugins.php.api.tree.declaration.ClassTree;
import org.sonar.plugins.php.api.tree.declaration.MethodDeclarationTree;
import org.sonar.plugins.php.api.tree.declaration.ParameterListTree;
import org.sonar.plugins.php.api.tree.expression.AnonymousClassTree;
import org.sonar.plugins.php.api.visitors.PHPVisitorCheck;
import org.sonar.squidbridge.annotations.ActivatedByDefault;
import org.sonar.squidbridge.annotations.SqaleConstantRemediation;
import org.sonar.squidbridge.annotations.SqaleSubCharacteristic;

@Rule(
  key = TooManyFunctionParametersCheck.KEY,
  name = "函数不应包含太多的参数",
  priority = Priority.MAJOR,
  tags = {Tags.BRAIN_OVERLOAD})
@ActivatedByDefault
@SqaleSubCharacteristic(RulesDefinition.SubCharacteristics.UNIT_TESTABILITY)
@SqaleConstantRemediation("20min")
public class TooManyFunctionParametersCheck extends PHPVisitorCheck {

  public static final String KEY = "S107";

  private static final String MESSAGE = "This function has %s parameters, which is greater than the %s authorized.";

  public static final int DEFAULT_MAX = 7;
  public static final int DEFAULT_CONSTRUCTOR_MAX = 7;

  @RuleProperty(
    key = "max",
    defaultValue = "" + DEFAULT_MAX)
  int max = DEFAULT_MAX;

  @RuleProperty(
    key = "构造函数最大值",
    defaultValue = "" + DEFAULT_CONSTRUCTOR_MAX)
  int constructorMax = DEFAULT_CONSTRUCTOR_MAX;

  private ClassTree classTree = null;

  @Override
  public void visitParameterList(ParameterListTree parameterList) {
    int numberOfParameters = parameterList.parameters().size();
    int maxValue = isConstructorParameterList(parameterList) ? constructorMax : max;
    if (numberOfParameters > maxValue) {
      context().newIssue(this, String.format(MESSAGE, numberOfParameters, maxValue)).tree(parameterList);
    }
    super.visitParameterList(parameterList);
  }

  private boolean isConstructorParameterList(ParameterListTree parameterList) {
    if (classTree != null) {
      MethodDeclarationTree constructor = classTree.fetchConstructor();
      if (constructor != null) {
        return parameterList.equals(constructor.parameters());
      }
    }
    return false;
  }

  @Override
  public void visitClassDeclaration(ClassDeclarationTree tree) {
    classTree = tree;
    super.visitClassDeclaration(tree);
    classTree = null;
  }

  @Override
  public void visitAnonymousClass(AnonymousClassTree tree) {
    classTree = tree;
    super.visitAnonymousClass(tree);
    classTree = null;
  }

}
