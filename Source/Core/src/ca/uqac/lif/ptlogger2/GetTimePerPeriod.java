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
import ca.uqac.lif.cep.GroupProcessor;
import ca.uqac.lif.cep.Processor;
import ca.uqac.lif.cep.functions.ApplyFunction;
import ca.uqac.lif.cep.functions.Constant;
import ca.uqac.lif.cep.functions.Cumulate;
import ca.uqac.lif.cep.functions.CumulativeFunction;
import ca.uqac.lif.cep.functions.Function;
import ca.uqac.lif.cep.functions.FunctionTree;

import static ca.uqac.lif.cep.Connector.BOTTOM;
import static ca.uqac.lif.cep.Connector.INPUT;
import static ca.uqac.lif.cep.Connector.OUTPUT;
import static ca.uqac.lif.cep.Connector.TOP;

import ca.uqac.lif.cep.tmf.Filter;
import ca.uqac.lif.cep.tmf.Fork;
import ca.uqac.lif.cep.tmf.Trim;
import ca.uqac.lif.cep.tuples.FetchAttribute;
import ca.uqac.lif.cep.tuples.MergeScalars;
import ca.uqac.lif.cep.util.Bags.RunOn;
import ca.uqac.lif.cep.util.Booleans;
import ca.uqac.lif.cep.util.Equals;
import ca.uqac.lif.cep.util.Lists;
import ca.uqac.lif.cep.util.Numbers;

/**
 * Computes a number of hours, grouping log entries into periods.
 * Graphically, this chain of processors is represented as follows:
 * <p>
 * <img src="{@docRoot}/doc-files/GetTimeByPeriod.png" alt="Processor chain">
 * <p>
 * This processor has two parameters:
 * <ul>
 * <li>A function <i>g</i> that associates an input tuple to a group.
 * For example, to compute hours per month, <i>g</i> can be a function
 * that associates to each tuple the year-month value of its <tt>Date</tt>
 * attribute. To compute hours per week, <i>g</i> can be a function that
 * associates each tuple to a value composed of the year and week number
 * into the year. Etc.</li>
 * <li>A processor P that can perform an optional aggregation over the
 * values computed for each period. If no aggregation is desired, P can
 * simply be a {@link Passthrough}.</li>
 * </ul>
 */
public class GetTimePerPeriod extends GroupProcessor
{
  public GetTimePerPeriod(Function group_function, Processor aggregation)
  {
    super(1, 1);
    Fork f1 = new Fork(2);
    ApplyFunction gf = new ApplyFunction(new FunctionTree(group_function, new FetchAttribute("Date")));
    Connector.connect(f1, BOTTOM, gf, INPUT);
    Fork f2 = new Fork(2);
    Connector.connect(gf, f2);
    Fork f3 = new Fork(2);
    Connector.connect(f2, TOP, f3, INPUT);
    Trim trim = new Trim(1);
    Connector.connect(f3, TOP, trim, INPUT);
    ApplyFunction eq = new ApplyFunction(new FunctionTree(Booleans.not, Equals.instance));
    Connector.connect(trim, OUTPUT, eq, TOP);
    Connector.connect(f3, BOTTOM, eq, BOTTOM);
    Fork f4 = new Fork(2);
    Connector.connect(eq, f4);
    Lists.Pack pack = new Lists.Pack();
    Connector.connect(f1, TOP, pack, TOP);
    Connector.connect(f4, TOP, pack, BOTTOM);
    Filter filter = new Filter();
    Connector.connect(f2, BOTTOM, filter, TOP);
    Connector.connect(f4, BOTTOM, filter, BOTTOM);
    GroupProcessor add_week = new GroupProcessor(1, 1);
    {
      ApplyFunction get_mins = new ApplyFunction(new FunctionTree(Numbers.division, new FunctionTree(Numbers.numberCast, new FetchAttribute("Minutes")), new Constant(60)));
      Cumulate sum = new Cumulate(new CumulativeFunction<Number>(Numbers.addition));
      Connector.connect(get_mins, sum);
      add_week.addProcessors(get_mins, sum);
      add_week.associateInput(INPUT, get_mins, INPUT);
      add_week.associateOutput(OUTPUT, sum, OUTPUT);
    }
    RunOn ro = new RunOn(add_week);
    Connector.connect(pack, ro);
    Connector.connect(ro, aggregation);
    ApplyFunction to_tuple = new ApplyFunction(new MergeScalars("Period", "Minutes"));
    Connector.connect(filter, OUTPUT, to_tuple, TOP);
    Connector.connect(aggregation, OUTPUT, to_tuple, BOTTOM);
    addProcessors(f1, gf, f2, f3, trim, eq, filter, f4, pack, ro, aggregation, to_tuple);
    associateInput(INPUT, f1, INPUT);
    associateOutput(OUTPUT, to_tuple, OUTPUT);
    /*
    super(1, 1);
    Fork fork = new Fork(2);
    Fork fork2 = new Fork(2);
    ApplyFunction week_nb = new ApplyFunction(new FunctionTree(group_function, new FetchAttribute("Date")));
    Connector.connect(fork, BOTTOM, week_nb, INPUT);
    Fork fork3 = new Fork()
    Connector.connect(week_nb, fork2);
    ApplyFunction same_week = new ApplyFunction(new FunctionTree(Booleans.not, Equals.instance));
    Connector.connect(fork2, TOP, same_week, TOP);
    Trim trim = new Trim(1);
    Connector.connect(fork2, BOTTOM, trim, INPUT);
    Connector.connect(trim, OUTPUT, same_week, BOTTOM);
    Lists.Pack pack = new Lists.Pack();
    Connector.connect(fork, TOP, pack, TOP);
    Connector.connect(same_week, OUTPUT, pack, BOTTOM);
    addProcessors(fork, fork2, same_week, trim, pack);
    GroupProcessor add_week = new GroupProcessor(1, 1);
    {
      ApplyFunction get_mins = new ApplyFunction(new FunctionTree(Numbers.numberCast, new FetchAttribute("Minutes")));
      Cumulate sum = new Cumulate(new CumulativeFunction<Number>(Numbers.addition));
      Connector.connect(get_mins, sum);
      add_week.addProcessors(get_mins, sum);
      add_week.associateInput(INPUT, get_mins, INPUT);
      add_week.associateOutput(OUTPUT, sum, OUTPUT);
    }
    RunOn ro = new RunOn(add_week);
    Connector.connect(pack, ro);
        associateInput(INPUT, fork, INPUT);
    associateOutput(OUTPUT, ro, OUTPUT);
    */
  }
}
