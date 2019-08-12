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

import ca.uqac.lif.mtnp.plot.gnuplot.Scatterplot;

/**
 * Custom scatterplot with dates on the horizontal axis.
 */
public class TimeScatterplot extends Scatterplot
{
  @Override
  public String toGnuplot(ImageType term, String lab_title, boolean with_caption)
  {
    // Add a few header lines to the plot to handle dates on the x axis
    String s = super.toGnuplot(term, lab_title, with_caption);
    s = "set xdata time\nset timefmt \"%Y-%m-%d\"\n" + s;
    return s;
  }
}
