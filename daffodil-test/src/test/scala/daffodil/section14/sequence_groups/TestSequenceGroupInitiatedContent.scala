package daffodil.section14.sequence_groups

import junit.framework.Assert._
import org.scalatest.junit.JUnit3Suite
import scala.xml._
import daffodil.xml.XMLUtils
import daffodil.xml.XMLUtils._
import daffodil.compiler.Compiler
import daffodil.util._
import daffodil.tdml.DFDLTestSuite
import java.io.File
import daffodil.debugger.Debugger

class TestSequenceGroupInitiatedContent extends JUnit3Suite {

  val testDir_01 = "/daffodil/section14/sequence_groups/"
  val tdml_01 = testDir_01 + "SequenceGroupInitiatedContent.tdml"
  lazy val runner_01 = new DFDLTestSuite(Misc.getRequiredResource(tdml_01))

  def test_baseline() { runner_01.runOneTest("initiatedContentBaseline") }
  def test_1() { runner_01.runOneTest("initiatedContent1") }
  def test_2() { runner_01.runOneTest("initiatedContent2") }
  def test_3() { runner_01.runOneTest("initiatedContent3") }

}
