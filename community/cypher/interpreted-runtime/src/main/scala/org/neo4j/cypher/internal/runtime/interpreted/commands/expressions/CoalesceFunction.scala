/*
 * Copyright (c) 2002-2020 "Neo4j,"
 * Neo4j Sweden AB [http://neo4j.com]
 *
 * This file is part of Neo4j.
 *
 * Neo4j is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.neo4j.cypher.internal.runtime.interpreted.commands.expressions

import org.neo4j.cypher.internal.runtime.CypherRow
import org.neo4j.cypher.internal.runtime.ReadableRow
import org.neo4j.cypher.internal.runtime.interpreted.commands.AstNode
import org.neo4j.cypher.internal.runtime.interpreted.pipes.QueryState
import org.neo4j.cypher.internal.util.symbols._
import org.neo4j.values.AnyValue
import org.neo4j.values.storable.Values

case class CoalesceFunction(override val arguments: Expression*) extends Expression {
  override def apply(ctx: ReadableRow, state: QueryState): AnyValue =
    arguments.
      view.
      map(expression => expression(ctx, state)).
      find(value => !(value eq Values.NO_VALUE)) match {
        case None    => Values.NO_VALUE
        case Some(x) => x
      }

  def innerExpectedType: Option[CypherType] = None

  val argumentsString: String = children.mkString(",")

  override def toString: String = "coalesce(" + argumentsString + ")"

  override def rewrite(f: Expression => Expression): Expression = f(CoalesceFunction(arguments.map(e => e.rewrite(f)): _*))

  override def children: Seq[AstNode[_]] = arguments
}
