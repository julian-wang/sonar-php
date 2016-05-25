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
import org.sonar.plugins.php.api.tree.Tree.Kind;
import org.sonar.plugins.php.api.tree.statement.SwitchCaseClauseTree;
import org.sonar.plugins.php.api.tree.statement.SwitchStatementTree;
import org.sonar.plugins.php.api.visitors.PHPVisitorCheck;
import org.sonar.squidbridge.annotations.ActivatedByDefault;
import org.sonar.squidbridge.annotations.SqaleConstantRemediation;
import org.sonar.squidbridge.annotations.SqaleSubCharacteristic;

@Rule(
  key = SwitchWithoutDefaultCheck.KEY,
  name = "\"switch\"语句应该以\"case default\"结束",
  priority = Priority.MAJOR,
  tags = {Tags.CERT, Tags.CWE, Tags.MISRA})
@ActivatedByDefault
@SqaleSubCharacteristic(RulesDefinition.SubCharacteristics.LOGIC_RELIABILITY)
@SqaleConstantRemediation("5min")
public class SwitchWithoutDefaultCheck extends PHPVisitorCheck {

  public static final String KEY = "S131";

  @Override
  public void visitSwitchStatement(SwitchStatementTree switchTree) {
    SwitchCaseClauseTree defaultClause = null;
    int defaultClauseIndex = 0;

    for (SwitchCaseClauseTree clause : switchTree.cases()) {
      if (clause.is(Kind.DEFAULT_CLAUSE)) {
        defaultClause = clause;
        break;
      }
      defaultClauseIndex++;
    }

    if (defaultClause == null) {
      context()
        .newIssue(this, "Add a \"case default\" clause to this \"switch\" statement.")
        .tree(switchTree);
    } else if (defaultClauseIndex < switchTree.cases().size() - 1) {
      context()
        .newIssue(this, "Move this \"case default\" clause to the end of this \"switch\" statement.")
        .tree(defaultClause);
    }

    super.visitSwitchStatement(switchTree);
  }
}
