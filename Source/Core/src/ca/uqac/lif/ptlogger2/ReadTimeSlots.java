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
import ca.uqac.lif.cep.tuples.FixedTupleBuilder;
import ca.uqac.lif.cep.tuples.TupleFixed;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ReadTimeSlots extends UniformProcessor
{
  /**
   * The pattern to match the time slots
   */
  protected static final transient Pattern s_pattern = Pattern.compile("^\\[(.*?)\\]\\s*':\\s*(.*)$");
  
  /**
   * The pattern to match the time slots
   */
  protected static final transient Pattern s_patternNoCategory = Pattern.compile("^\\[(.*?)\\]\\s*$");

  /**
   * A builder that creates tuples
   */
  protected static final transient FixedTupleBuilder s_builder = new FixedTupleBuilder("Category", "Minutes"); 

  /**
   * A tuple for the empty result
   */
  protected static final transient TupleFixed s_emptyTuple = s_builder.createTuple("", 0);

  public ReadTimeSlots()
  {
    super(1, 1);
  }

  @Override
  public ReadTimeSlots duplicate(boolean with_state)
  {
    return new ReadTimeSlots();
  }

  @Override
  protected boolean compute(Object[] inputs, Object[] outputs)
  {
    String line = (String) inputs[0];
    Matcher mat = s_pattern.matcher(line);
    String slots = "", category = "";
    if (mat.find())
    {
      slots = mat.group(1).trim();
      category = mat.group(2).trim();
    }
    else
    {
      mat = s_patternNoCategory.matcher(line);
      if (!mat.find())
      {
        outputs[0] = s_emptyTuple;
        return true;
      }
      slots = mat.group(1).trim();
    }
    int minutes = 0;
    String[] slot_parts = slots.split(";");
    for (String part : slot_parts)
    {
      minutes += getDuration(part);
    }
    TupleFixed t = s_builder.createTuple(category, minutes);
    outputs[0] = t;
    return true;
  }
  
  protected static int getDuration(String s)
  {
    String[] parts = s.split("-");
    if (parts.length != 2)
    {
      return 0;
    }
    int min_start = toMinutes(parts[0]);
    int min_end = toMinutes(parts[1]);
    if (min_start < 0 || min_end < 0)
    {
      return 0;
    }
    return min_end - min_start;
  }
  
  protected static int toMinutes(String time)
  {
    if (!time.contains("h"))
    {
      return -1;
    }
    String[] parts = time.split("h");
    int mins = Integer.parseInt(parts[0].trim()) * 60;
    if (parts.length == 2)
    {
      mins += Integer.parseInt(parts[1].trim());
    }
    return mins;
  }
}
