/*
 * Sonar Delphi Plugin
 * Copyright (C) 2023 Integrated Application Development
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
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package au.com.integradev.delphi.checks;

import org.sonar.check.Rule;
import org.sonar.plugins.communitydelphi.api.ast.VisibilitySectionNode;
import org.sonar.plugins.communitydelphi.api.check.DelphiCheck;
import org.sonar.plugins.communitydelphi.api.check.DelphiCheckContext;
import org.sonarsource.analyzer.commons.annotations.DeprecatedRuleKey;

@DeprecatedRuleKey(ruleKey = "EmptyVisibilitySectionRule", repositoryKey = "delph")
@Rule(key = "EmptyVisibilitySection")
public class EmptyVisibilitySectionCheck extends DelphiCheck {
  private static final String MESSAGE = "Remove this empty visibility section.";

  @Override
  public DelphiCheckContext visit(
      VisibilitySectionNode visibilitySection, DelphiCheckContext context) {
    if (!visibilitySection.isImplicitPublished() && visibilitySection.getChildren().size() == 1) {
      reportIssue(context, visibilitySection, MESSAGE);
    }
    return super.visit(visibilitySection, context);
  }
}
