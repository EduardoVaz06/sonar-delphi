/*
 * Sonar Delphi Plugin
 * Copyright (C) 2011 Sabre Airline Solutions and Fabricio Colombo
 * Author(s):
 * Przemyslaw Kociolek (przemyslaw.kociolek@sabre.com)
 * Michal Wojcik (michal.wojcik@sabre.com)
 * Fabricio Colombo (fabricio.colombo.mva@gmail.com)
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
package org.sonar.plugins.delphi.antlr;


import static org.junit.Assert.assertFalse;


import java.io.File;
import java.io.IOException;
import org.junit.Ignore;
import org.junit.Test;
import org.sonar.plugins.delphi.antlr.ast.DelphiAST;
import org.sonar.plugins.delphi.utils.DelphiUtils;

public class GrammarTest {

  private static final String BASE_DIR = "/org/sonar/plugins/delphi/grammar/";

  private void parseFile(String fileName) throws IOException {
    parseFile(fileName, null);
  }

  private void parseFile(String fileName, String encoding) throws IOException {
    System.out.println("Parsing file: " + BASE_DIR + fileName);
    DelphiAST ast = new DelphiAST(DelphiUtils.getResource(BASE_DIR + fileName), encoding);
    assertFalse(ast.isError());

    String name = fileName.replace(".pas", "");

    String outputFileName =
        File.createTempFile(name, "").getParentFile().getAbsolutePath() + File.separatorChar
            + "AST_" + name + ".xml";
    ast.generateXML(outputFileName);
    System.out.println("Generated AST XML file at " + outputFileName);
  }

  @Test
  public void testEmptyBeginStatement() throws Exception {
    parseFile("EmptyProcs.pas");
  }

  @Test
  public void testParseMultipleAttributes() throws Exception {
    parseFile("MultipleAttributes.pas");
  }

  @Test
  public void testParseRecordInitialization() throws Exception {
    parseFile("RecordInitialization.pas");
  }

  @Test
  public void testParseRecordConstructor() throws Exception {
    parseFile("RecordConstructor.pas");
  }

  @Test
  public void testParseLabel() throws Exception {
    parseFile("LabelUsage.pas");
  }

  @Test
  public void testParseDUnitX() throws Exception {
    parseFile("DUnitX.pas", "utf-8");
  }

  @Test
  public void testParseUTF8FileWithBOM() throws Exception {
    parseFile("UTF8WithBOM.pas", "utf-8");
  }

  @Test
  public void testParseAnonymousMethods() throws Exception {
    parseFile("AnonymousMethods.pas");
  }

  @Test
  public void testParseGenerics() throws Exception {
    parseFile("Generics.pas");
  }

  @Test
  public void testParseKeyWordsAsIdentifier() throws Exception {
    parseFile("KeyWordsAsIdentifier.pas");
  }

  @Test
  public void testParseListUtils() throws Exception {
    parseFile("ListUtils.pas");
  }

}
