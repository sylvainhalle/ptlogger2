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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;

/**
 * Utility methods for handling dates. 
 */
public class DateUtils
{
  /**
   * A simple date format
   */
  protected static final transient SimpleDateFormat s_dateFormat;
  
  static
  {
    s_dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    s_dateFormat.setTimeZone(TimeZone.getTimeZone("EDT"));
  }
  
  /**
   * Gets a string corresponding to the current date in the current timezone
   * @return The string
   */
  public static String getTodayString()
  {
    Calendar cal = Calendar.getInstance();
    return print(cal);
  }

  /**
   * Gets a Calendar from a string representing a date
   * @param line The date
   * @return The calendar
   */
  public static Calendar valueOf(String line)
  {
    Calendar cal = Calendar.getInstance();
    try
    {
      cal.setTime(s_dateFormat.parse(line));
    }
    catch (ParseException e)
    {
      return null;
    }
    return cal;
  }
  
  /**
   * Prints the date of a Calendar object
   * @param cal The calendar
   * @return The printed date
   */
  public static String print(Calendar cal)
  {
    return s_dateFormat.format(cal.getTime());
  }
  
  /**
   * Formats an int with a leading zero
   * @param value The int
   * @return The formatted string
   */
  public static String formatDigits(int value)
  {
    if (value < 10)
    {
      return "0" + value;
    }
    return "" + value;
  }
}
