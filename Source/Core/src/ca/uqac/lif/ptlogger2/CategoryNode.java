/*
    Part-time Logger 2, a time tracking application
    Copyright (C) 2019-2021  Sylvain Hallé

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

import java.util.ArrayList;
import java.util.List;

/**
 * A node in a tree of work categories, each associated to a number of
 * minutes worked.
 */
public class CategoryNode
{
  protected String m_label;
  
  protected Object m_value;
  
  protected List<CategoryNode> m_children;
  
  public CategoryNode(String label, Object value)
  {
    super();
    m_label = label;
    m_value = value;
    m_children = new ArrayList<CategoryNode>();
  }
  
  public void addChild(String path, Object value, Object default_value)
  {
    String[] parts = path.split("/");
    List<String> l_parts = new ArrayList<String>(parts.length);
    for (String part : parts)
    {
      l_parts.add(part);
    }
    addChild(l_parts, value, default_value);
  }
  
  protected void addChild(List<String> path, Object value, Object default_value)
  {
    if (path.size() == 0)
    {
      return;
    }
    if (path.size() == 1)
    {
    	String label = path.get(0);
    	for (CategoryNode child : m_children)
      {
        if (child.m_label.compareTo(label) == 0)
        {
        	int c_value = (int) child.m_value;
        	child.m_value = c_value + (int) value;
          return;
        }
      }
      m_children.add(new CategoryNode(path.get(0), value));
      return;
    }
    assert path.size() >= 2;
    String label = path.get(0);
    List<String> new_path = new ArrayList<String>();
    new_path.addAll(path);
    new_path.remove(0);
    for (CategoryNode child : m_children)
    {
      if (child.m_label.compareTo(label) == 0)
      {
        child.addChild(new_path, value, default_value);
        return;
      }
    }
    CategoryNode node = new CategoryNode(label, default_value);
    m_children.add(node);
    node.addChild(new_path, value, default_value);
  }
  
  @Override
  public String toString()
  {
    StringBuilder out = new StringBuilder();
    format(out, "", 32);
    return out.toString();
  }
  
  protected float format(StringBuilder out, String indent, int width)
  {
    String new_indent = indent + "  ";
    StringBuilder c_out = new StringBuilder();
    float weight = 0;
    for (CategoryNode node : m_children)
    {
      weight += node.format(c_out, new_indent, width);
    }
    float new_weight = ((Number) m_value).floatValue() / 60f + weight;
    out.append(indent);
    String label = m_label;
    if (m_label.isEmpty())
    {
      label = "Ecole";
    }
    out.append(label);
    for (int i = indent.length() + label.length(); i < width; i++)
    {
      out.append(" ");
    }
    out.append(String.format("%6.2f", new_weight));
    if (!m_children.isEmpty() && ((Number) m_value).floatValue() > 0)
    {
      out.append("\n");
      label = "(root)";
      out.append(indent).append("  ").append(label);
      for (int i = indent.length() + label.length(); i < width - 2; i++)
      {
        out.append(" ");
      }
      out.append(String.format("%6.2f", ((Number) m_value).intValue() / 60f));  
    }
    out.append("\n");
    out.append(c_out);
    return new_weight;
  }
}
