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

import ca.uqac.lif.cep.UniformProcessor;
import java.util.Calendar;
import java.util.regex.Pattern;

public class ReadDate extends UniformProcessor
{
  /**
   * The pattern to match the date
   */
  protected static final transient Pattern s_pattern = Pattern.compile("^\\d{4}-\\d{2}-\\d{2}$");

  /**
   * The last date output by the processor
   */
  protected Calendar m_lastElement;

  /**
   * Creates a new ReadDate processor
   */
  public ReadDate()
  {
    super(1, 1);
    m_lastElement = DateUtils.valueOf("2010-01-01");
  }

  @Override
  public ReadDate duplicate(boolean with_state)
  {
    ReadDate rd = new ReadDate();
    if (with_state)
    {
      rd.m_lastElement = m_lastElement;
    }
    return rd;
  }

  @Override
  protected boolean compute(Object[] inputs, Object[] outputs)
  {
    String line = ((String) inputs[0]).trim();
    if (line.matches("^\\d{4}-\\d{2}-\\d{2}[\\s\\{]*"))
    {
      m_lastElement = DateUtils.valueOf(line.substring(0, 10));
    }
    outputs[0] = m_lastElement;
    return true;
  }
}
