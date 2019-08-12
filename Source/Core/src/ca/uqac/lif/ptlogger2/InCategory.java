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

public class InCategory extends UnaryFunction<Tuple,Boolean>
{
  /**
   * The categories one wishes to keep
   */
  protected String[] m_categories;
  
  public InCategory(String ... categories)
  {
    super(Tuple.class, Boolean.class);
    m_categories = new String[categories.length];
    for (int i = 0; i < categories.length; i++)
    {
      String cat = categories[i];
      if (cat.contains("*"))
      {
        cat = cat.replaceAll("\\*", ".*?");
      }
      m_categories[i] = cat;
    }
  }
  
  @Override
  public Boolean getValue(Tuple tup)
  {
    String t_category = (String) tup.get("Category");
    for (String cat : m_categories)
    {
      if (t_category.matches(cat))
      {
        return true;
      }
    }
    return false;
  }
}
