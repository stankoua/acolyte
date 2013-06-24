package acolyte

import java.util.{ ArrayList, List ⇒ JList }
import java.sql.Statement

import scala.language.implicitConversions
import scala.collection.JavaConversions

import acolyte.ParameterMetaData.ParameterDef
import acolyte.StatementHandler.Parameter
import acolyte.CompositeHandler.{ QueryHandler, UpdateHandler }
import acolyte.RowList.Column

// Acolyte DSL
object Acolyte {
  def handleStatement = new CompositeHandler()

  def connection(h: ConnectionHandler) = new Driver().connect(h)
  def connection(h: StatementHandler) = new Driver().connect(h)

  implicit def CompositeHandlerAsScala(h: CompositeHandler): ScalaCompositeHandler = new ScalaCompositeHandler(h)

  implicit def ResultRowAsScala[R <: Row](r: R): ScalaResultRow =
    new ScalaResultRow(r)

  implicit def RowListAsScala[R <: Row](l: RowList[R]): ScalaRowList[R] =
    new ScalaRowList(l)

  implicit def PairAsColumn[T](c: (Class[T], String)): Column[T] =
    Column.defineCol(c._1, c._2)

}

case class Execution(
  sql: String,
  parameters: List[ExecutedParameter])

case class ExecutedParameter(
  value: AnyRef,
  definition: ParameterDef)

final class ScalaCompositeHandler(
    b: CompositeHandler) extends CompositeHandler {

  def withUpdateHandler(h: Execution ⇒ Int): CompositeHandler = {
    b.withUpdateHandler(new UpdateHandler {
      def apply(sql: String, p: JList[Parameter]): Int = {
        val ps = JavaConversions.asScalaIterable(p).
          foldLeft(Nil: List[ExecutedParameter]) { (l, t) ⇒
            l :+ ExecutedParameter(t.right, t.left)
          }

        h(Execution(sql, ps))
      }
    })
  }

  def withQueryHandler(h: Execution ⇒ Result): CompositeHandler = {
    b.withQueryHandler(new QueryHandler {
      def apply(sql: String, p: JList[Parameter]): Result = {
        val ps = JavaConversions.asScalaIterable(p).
          foldLeft(Nil: List[ExecutedParameter]) { (l, t) ⇒
            l :+ ExecutedParameter(t.right, t.left)
          }

        h(Execution(sql, ps))
      }
    })
  }
}

final class ScalaResultRow(r: Row) extends Row {
  lazy val cells = r.cells

  lazy val list: List[Any] =
    JavaConversions.iterableAsScalaIterable(cells).foldLeft(List[Any]()) {
      (l, v) ⇒ l :+ v
    }

}

final class ScalaRowList[R <: Row](l: RowList[R]) extends RowList[R] {
  override def append(row: R) = l.append(row)
  override def resultSet = l.resultSet

  def getColumnClasses = l.getColumnClasses
  def getColumnLabels = l.getColumnLabels
  def getRows = l.getRows

  def withLabel(i: Int, label: String): RowList[R] = l.withLabel(i, label)

  // Extension
  def :+(row: R) = append(row)

  def withLabels(labels: (Int, String)*): RowList[R] =
    labels.foldLeft[RowList[R]](this) { (l, t) ⇒ l.withLabel(t._1, t._2) }
}
