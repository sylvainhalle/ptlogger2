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
import java.util.Map;

@SuppressWarnings("rawtypes")
public class MapToCategoryTree extends UnaryFunction<Map,CategoryNode>
{
  public static final transient MapToCategoryTree instance = new MapToCategoryTree();
  
  protected MapToCategoryTree()
  {
    super(Map.class, CategoryNode.class);
  }
  
  @Override
  public CategoryNode getValue(Map map)
  {
    CategoryNode root = new CategoryNode("Ecole", 0);
    if (map == null)
    {
      return root;
    }
    for (Object o : map.entrySet())
    {
      Map.Entry<?,?> entry = (Map.Entry<?,?>) o;
      String key = (String) entry.getKey();
      int value = ((Number) entry.getValue()).intValue();
      root.addChild(key, value, 0);
    }
    return root;
  }
}
