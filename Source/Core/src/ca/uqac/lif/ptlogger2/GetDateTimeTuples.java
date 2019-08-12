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
import static ca.uqac.lif.cep.Connector.BOTTOM;
import static ca.uqac.lif.cep.Connector.INPUT;
import static ca.uqac.lif.cep.Connector.OUTPUT;
import static ca.uqac.lif.cep.Connector.TOP;
import ca.uqac.lif.cep.GroupProcessor;
import ca.uqac.lif.cep.functions.ApplyFunction;
import ca.uqac.lif.cep.tmf.Fork;
import ca.uqac.lif.cep.tuples.MergeTuples;
import ca.uqac.lif.cep.tuples.ScalarIntoTuple;

public class GetDateTimeTuples extends GroupProcessor
{
  public GetDateTimeTuples()
  {
    super(1, 1);
    Fork fork = new Fork(2);
    ReadDate rd = new ReadDate();
    Connector.connect(fork, TOP, rd, INPUT);
    ReadTimeSlots rts = new ReadTimeSlots();
    Connector.connect(fork, BOTTOM, rts, INPUT);
    ApplyFunction s_to_tup = new ApplyFunction(new ScalarIntoTuple("Date"));
    Connector.connect(rd, s_to_tup);
    ApplyFunction merge = new ApplyFunction(new MergeTuples());
    Connector.connect(s_to_tup, OUTPUT, merge, TOP);
    Connector.connect(rts, OUTPUT, merge, BOTTOM);
    associateInput(INPUT, fork, INPUT);
    associateOutput(OUTPUT, merge, OUTPUT);
    addProcessors(fork, rd, rts, s_to_tup, merge);
  }
}
