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

import com.google.common.collect.Lists;
import org.sonar.api.server.rule.RulesDefinition;
import org.sonar.check.Priority;
import org.sonar.check.Rule;
import org.sonar.check.RuleProperty;
import org.sonar.php.api.PHPKeyword;
import org.sonar.plugins.php.api.tree.Tree.Kind;
import org.sonar.plugins.php.api.tree.declaration.ClassDeclarationTree;
import org.sonar.plugins.php.api.tree.declaration.ClassMemberTree;
import org.sonar.plugins.php.api.tree.declaration.ClassPropertyDeclarationTree;
import org.sonar.plugins.php.api.tree.declaration.ClassTree;
import org.sonar.plugins.php.api.tree.expression.AnonymousClassTree;
import org.sonar.plugins.php.api.tree.lexical.SyntaxToken;
import org.sonar.plugins.php.api.visitors.PHPVisitorCheck;
import org.sonar.squidbridge.annotations.SqaleConstantRemediation;
import org.sonar.squidbridge.annotations.SqaleSubCharacteristic;

import java.util.List;

@Rule(
  key = TooManyFieldsInClassCheck.KEY,
  name = "类不应该包含太多字段",
  priority = Priority.MAJOR,
  tags = {Tags.BRAIN_OVERLOAD})
@SqaleSubCharacteristic(RulesDefinition.SubCharacteristics.ARCHITECTURE_CHANGEABILITY)
@SqaleConstantRemediation("1h")
public class TooManyFieldsInClassCheck extends PHPVisitorCheck {

  public static final String KEY = "S1820";
  private static final String MESSAGE = "Refactor this class so it has no more than %s%s fields, rather than the %s it currently has.";

  public static final int DEFAULT_MAX = 20;
  public static final boolean DEFAULT_COUNT_NON_PUBLIC = true;

  @RuleProperty(
    key = "字段的最大阈值",
    defaultValue = "" + DEFAULT_MAX)
  int maximumFieldThreshold = DEFAULT_MAX;

  @RuleProperty(
    key = "是否计算非公开字段",
    type = "BOOLEAN",
    defaultValue = "" + DEFAULT_COUNT_NON_PUBLIC)
  boolean countNonpublicFields = DEFAULT_COUNT_NON_PUBLIC;

  @Override
  public void visitClassDeclaration(ClassDeclarationTree tree) {
    super.visitClassDeclaration(tree);

    if (tree.is(Kind.CLASS_DECLARATION)) {
      visitClass(tree);
    }
  }

  private void visitClass(ClassTree tree) {
    int nbFields = getNumberOfFields(tree);

    if (nbFields > maximumFieldThreshold) {
      String message = String.format(MESSAGE, maximumFieldThreshold, countNonpublicFields ? "" : " public", nbFields);
      context().newIssue(this, message).tree(tree);
    }
  }

  @Override
  public void visitAnonymousClass(AnonymousClassTree tree) {
    super.visitAnonymousClass(tree);

    visitClass(tree);
  }

  private int getNumberOfFields(ClassTree tree) {
    List<ClassPropertyDeclarationTree> fields = getClassFields(tree);
    int nbFields = fields.size();

    if (!countNonpublicFields) {
      nbFields -= getNumberOfNonPublicFields(fields);
    }
    return nbFields;
  }

  private static List<ClassPropertyDeclarationTree> getClassFields(ClassTree classDeclaration) {
    List<ClassPropertyDeclarationTree> fields = Lists.newArrayList();

    for (ClassMemberTree classMember : classDeclaration.members()) {
      if (classMember.is(Kind.CLASS_PROPERTY_DECLARATION, Kind.CLASS_CONSTANT_PROPERTY_DECLARATION)) {
        fields.add((ClassPropertyDeclarationTree) classMember);
      }
    }
    return fields;
  }

  private static int getNumberOfNonPublicFields(List<ClassPropertyDeclarationTree> fields) {
    int nbNonPublicFields = 0;

    for (ClassPropertyDeclarationTree field : fields) {

      // class constants are public in PHP
      if (field.is(Kind.CLASS_PROPERTY_DECLARATION) && isNonPublic(field.modifierTokens())) {
        nbNonPublicFields++;
      }
    }
    return nbNonPublicFields;
  }

  private static boolean isNonPublic(List<SyntaxToken> modifiers) {
    for (SyntaxToken modifierToken : modifiers) {
      String modifier = modifierToken.text();
      if (!PHPKeyword.VAR.getValue().equals(modifier) && (PHPKeyword.PROTECTED.getValue().equals(modifier) || PHPKeyword.PRIVATE.getValue().equals(modifier))) {
        return true;
      }
    }
    return false;
  }

}
