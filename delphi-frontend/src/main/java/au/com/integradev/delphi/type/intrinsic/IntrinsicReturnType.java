/*
 * Sonar Delphi Plugin
 * Copyright (C) 2019 Integrated Application Development
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
package au.com.integradev.delphi.type.intrinsic;

import static org.sonar.plugins.communitydelphi.api.type.IntrinsicType.ANSICHAR;
import static org.sonar.plugins.communitydelphi.api.type.IntrinsicType.ANSISTRING;
import static org.sonar.plugins.communitydelphi.api.type.IntrinsicType.UNICODESTRING;
import static org.sonar.plugins.communitydelphi.api.type.IntrinsicType.WIDECHAR;

import au.com.integradev.delphi.type.TypeImpl;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.RoutineKind;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.RoutineNameDeclaration;
import org.sonar.plugins.communitydelphi.api.type.IntrinsicType;
import org.sonar.plugins.communitydelphi.api.type.Type;
import org.sonar.plugins.communitydelphi.api.type.TypeFactory;

public abstract class IntrinsicReturnType extends TypeImpl {
  @Override
  public String getImage() {
    return "<" + getClass().getSimpleName() + ">";
  }

  @Override
  public int size() {
    // meta type
    return 0;
  }

  public abstract Type getReturnType(List<Type> arguments);

  public static Type high(TypeFactory typeFactory) {
    return new HighLowReturnType(typeFactory);
  }

  public static Type low(TypeFactory typeFactory) {
    return new HighLowReturnType(typeFactory);
  }

  public static Type round(TypeFactory typeFactory) {
    return new RoundTruncReturnType("Round", typeFactory);
  }

  public static Type trunc(TypeFactory typeFactory) {
    return new RoundTruncReturnType("Trunc", typeFactory);
  }

  public static Type concat(TypeFactory typeFactory) {
    return new ConcatReturnType(typeFactory);
  }

  public static Type copy(TypeFactory typeFactory) {
    return new CopyReturnType(typeFactory);
  }

  public static Type classReferenceValue() {
    return new ClassReferenceValueType();
  }

  public static Type argumentByIndex(int index) {
    return new ArgumentByIndexReturnType(index);
  }

  private static final class HighLowReturnType extends IntrinsicReturnType {
    private final Type integerType;

    private HighLowReturnType(TypeFactory typeFactory) {
      this.integerType = typeFactory.getIntrinsic(IntrinsicType.INTEGER);
    }

    @Override
    public Type getReturnType(List<Type> arguments) {
      Type type = arguments.get(0);

      if (type.isClassReference()) {
        type = ((ClassReferenceType) type).classType();
      }

      if (type.isArray() || type.isString()) {
        type = integerType;
      }

      return type;
    }
  }

  private static final class RoundTruncReturnType extends IntrinsicReturnType {
    private final String name;
    private final Type int64Type;

    private RoundTruncReturnType(String name, TypeFactory typeFactory) {
      this.name = name;
      this.int64Type = typeFactory.getIntrinsic(IntrinsicType.INT64);
    }

    @Override
    public Type getReturnType(List<Type> arguments) {
      Type type = arguments.get(0);
      if (type.isRecord()) {
        return ((ScopedType) type)
            .typeScope().getRoutineDeclarations().stream()
                .filter(routine -> routine.getRoutineKind() == RoutineKind.OPERATOR)
                .filter(routine -> routine.getImage().equalsIgnoreCase(name))
                .map(RoutineNameDeclaration::getReturnType)
                .findFirst()
                .orElseGet(TypeFactory::unknownType);
      }
      return int64Type;
    }
  }

  private static final class ClassReferenceValueType extends IntrinsicReturnType {
    @Override
    public Type getReturnType(List<Type> arguments) {
      Type type = arguments.get(0);
      if (type.isClassReference()) {
        return ((ClassReferenceType) type).classType();
      }
      return TypeFactory.unknownType();
    }
  }

  private static final class ConcatReturnType extends IntrinsicReturnType {
    private final TypeFactory typeFactory;

    public ConcatReturnType(TypeFactory typeFactory) {
      this.typeFactory = typeFactory;
    }

    @Override
    public Type getReturnType(List<Type> arguments) {
      Type result = null;
      List<Type> elementTypes = new ArrayList<>();

      for (Type argument : arguments) {
        if (argument.isDynamicArray() || argument.isString() || argument.isVariant()) {
          result = widenIfNeeded(argument, result);
        } else if (argument.isArrayConstructor()) {
          elementTypes.addAll(((ArrayConstructorType) argument).elementTypes());
        }
      }

      if (result == null) {
        result = typeFactory.arrayConstructor(elementTypes);
      }

      return result;
    }

    private Type widenIfNeeded(Type newType, @Nullable Type currentType) {
      while (newType.isAlias()) {
        newType = ((AliasType) newType).aliasedType();
      }

      if (newType.is(ANSICHAR)) {
        newType = typeFactory.getIntrinsic(ANSISTRING);
      } else if (newType.is(WIDECHAR)) {
        newType = typeFactory.getIntrinsic(UNICODESTRING);
      }

      if (currentType == null) {
        return newType;
      }

      if (newType.isString()
          && currentType.isString()
          && ((StringType) newType).characterType().size()
              > ((StringType) currentType).characterType().size()) {
        return newType;
      }

      return currentType;
    }
  }

  private static final class CopyReturnType extends IntrinsicReturnType {
    private final TypeFactory typeFactory;

    public CopyReturnType(TypeFactory typeFactory) {
      this.typeFactory = typeFactory;
    }

    @Override
    public Type getReturnType(List<Type> arguments) {
      Type result = arguments.get(0);
      if (result.isChar()) {
        result = typeFactory.getIntrinsic(result.size() == 1 ? ANSISTRING : UNICODESTRING);
      }
      return result;
    }
  }

  private static final class ArgumentByIndexReturnType extends IntrinsicReturnType {
    private final int index;

    private ArgumentByIndexReturnType(int index) {
      this.index = index;
    }

    @Override
    public Type getReturnType(List<Type> arguments) {
      return arguments.get(index);
    }
  }
}
