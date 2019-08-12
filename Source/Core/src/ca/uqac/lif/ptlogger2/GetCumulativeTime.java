/*
    Part-time Logger 2, a time tracking application
    Copyright (C) 2019  Sylvain Hallé

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published
    by the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Lesser General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package ca.uqac.lif.ptlogger2;

import ca.uqac.lif.cep.Connector;
import static ca.uqac.lif.cep.Connector.INPUT;
import static ca.uqac.lif.cep.Connector.OUTPUT;
import ca.uqac.lif.cep.GroupProcessor;
import ca.uqac.lif.cep.functions.ApplyFunction;
import ca.uqac.lif.cep.functions.Cumulate;
import ca.uqac.lif.cep.functions.CumulativeFunction;
import ca.uqac.lif.cep.functions.FunctionTree;
import ca.uqac.lif.cep.tmf.Slice;
import ca.uqac.lif.cep.tuples.FetchAttribute;
import ca.uqac.lif.cep.util.Numbers;

public class GetCumulativeTime extends GroupProcessor
{
  public GetCumulativeTime()
  {
    super(1, 1);
    GroupProcessor sum = new GroupProcessor(1, 1);
    {
      ApplyFunction get_time = new ApplyFunction(new FunctionTree(Numbers.numberCast, new FetchAttribute("Minutes")));
      Cumulate add = new Cumulate(new CumulativeFunction<Number>(Numbers.addition));
      Connector.connect(get_time, add);
      sum.addProcessors(get_time, add);
      sum.associateInput(INPUT, get_time, INPUT);
      sum.associateOutput(OUTPUT, add, OUTPUT);
    }
    Slice slice = new Slice(new FetchAttribute("Category"), sum);
    addProcessors(slice);
    associateInput(INPUT, slice, INPUT);
    associateOutput(OUTPUT, slice, OUTPUT);
  }
}
