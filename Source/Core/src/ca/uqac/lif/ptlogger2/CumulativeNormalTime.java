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

import ca.uqac.lif.cep.functions.UnaryFunction;
import ca.uqac.lif.cep.tuples.FixedTupleBuilder;
import ca.uqac.lif.cep.tuples.Tuple;
import java.util.Calendar;

public class CumulativeNormalTime extends UnaryFunction <Tuple,Tuple>
{
  public static final transient CumulativeNormalTime instance = new CumulativeNormalTime();
  
  protected static final transient FixedTupleBuilder s_builder = new FixedTupleBuilder("Period", "40h");
  
  protected static final int[] s_hours40 = {0, 40, 80, 120, 160, 200, 240, 280, 320, 360, 400, 440, 472, 504, 544, 584, 624, 664, 704, 744, 776, 816, 856, 896, 936, 968, 1000, 1000, 1000, 1000, 1000, 1040, 1080, 1120, 1152, 1192, 1232, 1272, 1312, 1352, 1384, 1424, 1464, 1504, 1544, 1584, 1624, 1664, 1704, 1744, 1784, 1824, 1824};
  
  protected CumulativeNormalTime()
  {
    super(Tuple.class, Tuple.class);
  }
  
  @Override
  public Tuple getValue(Tuple t)
  {
    String sunday = (String) t.get("Period");
    Calendar cal = DateUtils.valueOf(sunday);
    int week = cal.get(Calendar.WEEK_OF_YEAR);
    int cumul_hours = s_hours40[week];
    return s_builder.createTuple(sunday, cumul_hours);
  }

}
