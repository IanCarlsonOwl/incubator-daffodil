package daffodil.schema

/**
 * Copyright (c) 2010 NCSA.  All rights reserved.
 * Developed by: NCSA Cyberenvironments and Technologies
 *               University of Illinois at Urbana-Champaign
 *               http://cet.ncsa.uiuc.edu/
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to
 * deal with the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or
 * sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *  1. Redistributions of source code must retain the above copyright notice,
 *     this list of conditions and the following disclaimers.
 *  2. Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimers in the
 *     documentation and/or other materials provided with the distribution.
 *  3. Neither the names of NCSA, University of Illinois, nor the names of its
 *     contributors may be used to endorse or promote products derived from this
 *     Software without specific prior written permission.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.  IN NO EVENT SHALL THE
 * CONTRIBUTORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS
 * WITH THE SOFTWARE.
 *
 */

/*
 * Created By: Alejandro Rodriguez < alejandr @ ncsa . uiuc . edu >
 * Date: 2010
 */

import annotation.Annotation
import org.jdom.Parent
import daffodil.exceptions.{ElementProcessingException, ElementNotFoundException}
import daffodil.processors._
import daffodil.xml.{XMLUtil, Namespaces}
import input.BasicProcessor
import daffodil.parser.RollbackStream
import daffodil.parser.regex.Regex
import scala.collection.mutable.LinkedList

@SerialVersionUID(1)
class SimpleElement(val name:String,ann:Annotation,target:String,namespaces:Namespaces)
        extends BasicNodeImpl(target,namespaces,ann) with Element{

  override  def getName(parent:Parent):String = name

  override protected def findChildren(input:RollbackStream,variables:VariableMap,
                                      parent:Parent,maxLength:Int,terminators:List[Regex],
                                      processor:BasicProcessor) : ChildResult = {

    input checkpoint;
    val element = XMLUtil.addNewChild(parent,name,target,namespaces)

    try {
      val parseAttempt = processor(input,element,variables,namespaces,terminators)
      val result = parseAttempt match {
        case Success => new ChildSuccess(LinkedList(element))
        case Last => new ChildLast(LinkedList(element))
        case LastEmpty | Empty => setDefault(input,parent,element)
        case _ => // Failure =>
          throw new ElementNotFoundException("Element not found",
            schemaContext = annotation element,documentContext = element,position = Some(input getPosition))
      }

      input uncheck;
      result
    }catch{
      case e:ElementProcessingException =>
        input.rollback
        XMLUtil.removeChild(parent,element)
        if (e.schemaContext == null)
          e.schemaContext = ann.element
        if (e.documentContext == null)
          e.documentContext = parent
        if (e.position == None)
          e.position = Some(input.getPosition)
        throw e
    }
  }

  private def setDefault(input:RollbackStream, parent:Parent, element:org.jdom.Element):ChildResult = {
    annotation.format.defaultValue match {
      case Some(s) => element setText(s); new ChildSuccess(LinkedList(element))
      case None => if (this.getMinOccurs == 0) {
        input.rollback
        XMLUtil.removeChild(parent,element)
        new ChildLast(null)
      } else
        throw new ElementNotFoundException("Element not found",
          schemaContext = annotation element,documentContext = element,position = Some(input getPosition))
    }
  }

  override def canEqual(o:Any):Boolean = o.isInstanceOf[SimpleElement]

  override def equals(o:Any) = o match {
    case that:SimpleElement => {
      that.canEqual(this) && super.equals(that) && this.name == that.name
    }
    case _ => false
  }

}
