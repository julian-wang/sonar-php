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
import java.util.Locale;
import org.sonar.api.server.rule.RulesDefinition;
import org.sonar.check.Priority;
import org.sonar.check.Rule;
import org.sonar.php.api.PHPKeyword;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.Tree.Kind;
import org.sonar.plugins.php.api.tree.expression.LiteralTree;
import org.sonar.plugins.php.api.tree.lexical.SyntaxToken;
import org.sonar.plugins.php.api.visitors.PHPVisitorCheck;
import org.sonar.squidbridge.annotations.SqaleConstantRemediation;
import org.sonar.squidbridge.annotations.SqaleSubCharacteristic;

import java.util.Set;
import java.util.regex.Pattern;

@Rule(
  key = KeywordsAndConstantsNotLowerCaseCheck.KEY,
  name = "PHP关键字和常量\"true\", \"false\", \"null\" 都应该使用小写",
  priority = Priority.MINOR,
  tags = {Tags.CONVENTION, Tags.PSR2})
@SqaleSubCharacteristic(RulesDefinition.SubCharacteristics.READABILITY)
@SqaleConstantRemediation("1min")
public class KeywordsAndConstantsNotLowerCaseCheck extends PHPVisitorCheck {

  public static final String KEY = "S1781";
  private static final String MESSAGE = "Write this \"%s\" %s in lower case.";

  private static final Pattern PATTERN = Pattern.compile("[a-z_]+");
  private static final Set<String> KEYWORDS = ImmutableSet.copyOf(PHPKeyword.getKeywordValues());

  @Override
  public void visitLiteral(LiteralTree tree) {
    super.visitLiteral(tree);

    if (tree.is(Kind.NULL_LITERAL, Kind.BOOLEAN_LITERAL)) {
      check(tree, tree.value(), "constant");
    }
  }

  @Override
  public void visitToken(SyntaxToken token) {
    super.visitToken(token);

    if (KEYWORDS.contains(token.text().toLowerCase(Locale.ENGLISH))) {
      check(token, token.text(), "keyword");
    }
  }

  private void check(Tree tree, String value, String kind) {
    if (!PATTERN.matcher(value).matches()) {
      String message = String.format(MESSAGE, value, kind);
      context().newIssue(this, message).tree(tree);
    }
  }

}
