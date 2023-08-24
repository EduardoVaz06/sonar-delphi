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

import au.com.integradev.delphi.utils.NameConventionUtils;
import org.apache.commons.lang3.EnumUtils;
import org.sonar.check.Rule;
import org.sonar.check.RuleProperty;
import org.sonar.plugins.communitydelphi.api.ast.TypeDeclarationNode;
import org.sonar.plugins.communitydelphi.api.check.DelphiCheck;
import org.sonar.plugins.communitydelphi.api.check.DelphiCheckContext;
import org.sonar.plugins.communitydelphi.api.type.Type;
import org.sonarsource.analyzer.commons.annotations.DeprecatedRuleKey;

@DeprecatedRuleKey(ruleKey = "AttributeNameRule", repositoryKey = "delph")
@Rule(key = "AttributeName")
public class AttributeNameCheck extends DelphiCheck {
  private static final String MESSAGE = "Rename this type to match the expected naming convention.";

  private enum AttributeSuffixSetting {
    REQUIRED,
    ALLOWED,
    FORBIDDEN
  }

  @RuleProperty(
      key = "attributeSuffix",
      defaultValue = "allowed",
      description =
          "Whether to require, allow, or forbid the use of the 'Attribute' suffix for attribute"
              + " classes. Options are: 'required', 'allowed', and 'forbidden'.")
  public String attributeSuffix = "";

  private AttributeSuffixSetting attributeSuffixSetting;

  @Override
  public void start(DelphiCheckContext context) {
    attributeSuffixSetting =
        EnumUtils.getEnumIgnoreCase(
            AttributeSuffixSetting.class, attributeSuffix, AttributeSuffixSetting.ALLOWED);
  }

  private static boolean isAttributeClass(Type type) {
    return type.isClass() && type.isSubTypeOf("System.TCustomAttribute");
  }

  @Override
  public DelphiCheckContext visit(TypeDeclarationNode type, DelphiCheckContext context) {
    if (isViolation(type)) {
      reportIssue(context, type.getTypeNameNode(), MESSAGE);
    }

    return super.visit(type, context);
  }

  private boolean hasValidSuffix(String typeName) {
    switch (attributeSuffixSetting) {
      case REQUIRED:
        return typeName.toLowerCase().endsWith("attribute");
      case FORBIDDEN:
        return !typeName.toLowerCase().endsWith("attribute");
      default:
        return true;
    }
  }

  private boolean isViolation(TypeDeclarationNode type) {
    return isAttributeClass(type.getType())
        && !(NameConventionUtils.compliesWithPascalCase(type.simpleName())
            && hasValidSuffix(type.simpleName()));
  }
}
