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
import ca.uqac.lif.cep.tuples.Tuple;
import java.util.Calendar;

public class InDateInterval extends UnaryFunction<Tuple,Boolean>
{
  /**
   * The starting date
   */
  protected Calendar m_startDate;
  
  /**
   * The ending date
   */
  protected Calendar m_endDate;
  
  protected InDateInterval()
  {
    super(Tuple.class, Boolean.class);
  }
  
  /**
   * Creates a new date interval filtering function
   * @param start_date The start date (YYYY-mm-dd)
   * @param end_date The end date (YYYY-mm-dd)
   */
  public InDateInterval(String start_date, String end_date)
  {
    super(Tuple.class, Boolean.class);
    m_startDate = Calendar.getInstance();
    m_startDate.setTime(DateUtils.valueOf(start_date).getTime());
    m_endDate = Calendar.getInstance();
    m_endDate.setTime(DateUtils.valueOf(end_date).getTime());
  }
  
  @Override
  public Boolean getValue(Tuple tup)
  {
    Calendar d_date = (Calendar) tup.get("Date");
    return m_startDate.compareTo(d_date) <=0 && m_endDate.compareTo(d_date) >= 0;
  }
}
