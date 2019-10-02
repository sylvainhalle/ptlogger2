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
import java.util.Calendar;

public class GetLastSunday extends UnaryFunction<Calendar,String>
{
  public static final transient GetLastSunday instance = new GetLastSunday();
  
  protected GetLastSunday()
  {
    super(Calendar.class, String.class);
  }
  
  @Override
  public String getValue(Calendar cal)
  {
    int day_of_week = cal.get(Calendar.DAY_OF_WEEK);
    int offset = day_of_week - Calendar.SUNDAY + 1;
    Calendar cal_s = Calendar.getInstance();
    cal_s.setTime(cal.getTime());
    cal_s.add(Calendar.DAY_OF_MONTH, -offset);
    return DateUtils.print(cal_s);
  }
}
