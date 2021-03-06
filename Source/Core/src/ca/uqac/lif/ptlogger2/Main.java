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

import static ca.uqac.lif.cep.Connector.BOTTOM;
import static ca.uqac.lif.cep.Connector.INPUT;
import static ca.uqac.lif.cep.Connector.OUTPUT;
import static ca.uqac.lif.cep.Connector.TOP;

import ca.uqac.lif.cep.Connector;
import ca.uqac.lif.cep.GroupProcessor;
import ca.uqac.lif.cep.Processor;
import ca.uqac.lif.cep.functions.ApplyFunction;
import ca.uqac.lif.cep.functions.Constant;
import ca.uqac.lif.cep.functions.Cumulate;
import ca.uqac.lif.cep.functions.CumulativeFunction;
import ca.uqac.lif.cep.functions.Function;
import ca.uqac.lif.cep.functions.FunctionTree;
import ca.uqac.lif.cep.functions.RaiseArity;
import ca.uqac.lif.cep.io.Print;
import ca.uqac.lif.cep.io.ReadLines;
import ca.uqac.lif.cep.io.WriteToFile;
import ca.uqac.lif.cep.mtnp.DrawPlot;
import ca.uqac.lif.cep.mtnp.UpdateTableMap;
import ca.uqac.lif.cep.tmf.Filter;
import ca.uqac.lif.cep.tmf.Fork;
import ca.uqac.lif.cep.tmf.KeepLast;
import ca.uqac.lif.cep.tmf.Passthrough;
import ca.uqac.lif.cep.tmf.Pump;
import ca.uqac.lif.cep.tmf.Sink;
import ca.uqac.lif.cep.tmf.Splice;
import ca.uqac.lif.cep.tmf.Trim;
import ca.uqac.lif.cep.util.Booleans;
import ca.uqac.lif.cep.util.Numbers;
import ca.uqac.lif.mtnp.util.FileHelper;
import ca.uqac.lif.tui.AnsiPrinter;
import ca.uqac.lif.util.CliParser;
import ca.uqac.lif.util.CliParser.Argument;
import ca.uqac.lif.util.CliParser.ArgumentMap;
import net.harawata.appdirs.AppDirsFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.List;
import java.util.Properties;

public class Main
{
  /**
   * The major version number
   */
  private static final transient int s_majorVersionNumber = 1;

  /**
   * The minor version number
   */
  private static final transient int s_minorVersionNumber = 90;

  /**
   * The revision version number
   */
  private static final transient int s_revisionVersionNumber = 4;

  public static void main(String[] args) throws FileNotFoundException
  {
    AnsiPrinter out = new AnsiPrinter(System.out);
    AnsiPrinter err = new AnsiPrinter(System.err);
    System.exit(mainLoop(args, out, err));
  }

  public static int mainLoop(String[] args, AnsiPrinter stdout, AnsiPrinter stderr) throws FileNotFoundException
  {
    CliParser parser = new CliParser();
    parser.addArgument(new Argument().withLongName("start").withArgument("date").withDescription("\tStart at date"));
    parser.addArgument(new Argument().withLongName("end").withArgument("date").withDescription("\tEnd at date"));
    parser.addArgument(new Argument().withLongName("today").withDescription("\tShow only for today"));
    parser.addArgument(new Argument().withLongName("this-week").withDescription("\tShow only for this week"));
    parser.addArgument(new Argument().withLongName("week-of").withArgument("date").withDescription("\tShow for week containing date"));
    parser.addArgument(new Argument().withShortName("c").withLongName("categories").withArgument("cats").withDescription("Keeps only a comma-separated list of categories"));
    parser.addArgument(new Argument().withLongName("show").withArgument("x").withDescription("\tPerforms computation x"));
    //parser.addArgument(new Argument().withLongName("debt").withArgument("file").withDescription("\tShow debt instead of time; read account from file"));
    parser.addArgument(new Argument().withShortName("o").withLongName("output").withArgument("file").withDescription("Writes output to file"));
    parser.addArgument(new Argument().withLongName("help").withDescription("\t\tShow command line usage"));
    parser.addArgument(new Argument().withLongName("quiet").withDescription("\tDon't show messages, only data"));
    ArgumentMap arg_map = parser.parse(args);

    // Show help?
    if (arg_map == null)
    {
    	parser.printHelp(getCliHeader() + "Invalid command line arguments", stderr);
    	return 1;
    }
    if (arg_map.containsKey("help"))
    {
      parser.printHelp(getCliHeader(), stderr);
      return 0;
    }

    // Show header
    if (!arg_map.containsKey("quiet"))
    {
      stdout.println(getCliHeader());
    }
    
    // Check for presence of a configuration file
    Properties properties = new Properties();
    String local_config_dir = AppDirsFactory.getInstance().getUserConfigDir("ptlogger2", "", "");
    if (FileHelper.fileExists(local_config_dir + "config.properties"))
    {
    	FileReader reader = new FileReader(local_config_dir + "config.properties");
    	try
			{
				properties.load(reader);
			}
			catch (IOException e)
			{
				stderr.println("Cannot load properties file " + local_config_dir + "config.properties");
			}
    }
    String base_folder = "";
    {
    	String bf = properties.getProperty("basefolder");
    	if (bf != null)
    	{
    		base_folder = bf;
      	if (!base_folder.endsWith("/"))
      	{
      		base_folder += "/";
      	}	
    	}
    }

    // Read list of filenames, and create a source that splices them together
    List<String> filenames = arg_map.getOthers();
    if (filenames == null || filenames.size() == 0)
    {
    	// If no filename, assume currentyear.md
    	// Look out: we assume that year has four digits ;-)
    	filenames.add(DateUtils.getTodayString().substring(0, 4) + ".md");
    }
    ReadLines[] readlines = new ReadLines[filenames.size()];
    for (int i = 0; i < filenames.size(); i++)
    {
    	String filename = filenames.get(i);
    	File f = new File(filename);
    	File f_to_read = null;
    	if (f.isAbsolute())
    	{
    		f_to_read = f;
    	}
    	else
    	{
    		f_to_read = new File(base_folder + filename);
    	}
    	try
    	{
    		readlines[i] = new ReadLines(new FileInputStream(f_to_read));
    	}
    	catch (FileNotFoundException e)
    	{
    		stderr.println("File not found: " + f_to_read.getAbsolutePath());
    		return 1;
    	}
    }
    Splice source = new Splice(readlines);

    // Read lines and create date/time tuples
    GetDateTimeTuples tuples = new GetDateTimeTuples();
    Connector.connect(source, tuples);

    // Pump to activate the computation
    Pump global_pump = new Pump();
    Connector.connect(tuples, global_pump);

    // Apply optional filter; by default, no filtering (condition = true)
    Function filter_condition = new RaiseArity(1, new Constant(true));
    // Start/end dates?
    if (arg_map.containsKey("start"))
    {
      String start_date = arg_map.get("start");
      String end_date = DateUtils.getTodayString();
      if (arg_map.containsKey("end"))
      {
        end_date = arg_map.get("end");
      }
      InDateInterval idi = new InDateInterval(start_date, end_date);
      // Apply the conjunction of this function to the existing condition
      FunctionTree and = new FunctionTree(Booleans.and, idi, filter_condition);
      filter_condition = and;
    }
    if (arg_map.containsKey("today"))
    {
      String start_date = DateUtils.getTodayString();
      String end_date = start_date;
      //stdout.print("Filter: " + start_date);
      InDateInterval idi = new InDateInterval(start_date, end_date);
      // Apply the conjunction of this function to the existing condition
      FunctionTree and = new FunctionTree(Booleans.and, idi, filter_condition);
      filter_condition = and;
    }
    if (arg_map.containsKey("this-week"))
    {
      String week_date = DateUtils.getTodayString();
      //stdout.print("Week of: " + week_date);
      InWeekOf iwo = new InWeekOf(week_date);
      // Apply the conjunction of this function to the existing condition
      FunctionTree and = new FunctionTree(Booleans.and, iwo, filter_condition);
      filter_condition = and;
    }
    // Week of?
    if (arg_map.containsKey("week-of"))
    {
      String week_date = arg_map.get("week-of");
      InWeekOf iwo = new InWeekOf(week_date);
      // Apply the conjunction of this function to the existing condition
      FunctionTree and = new FunctionTree(Booleans.and, iwo, filter_condition);
      filter_condition = and;
    }
    // Filter on categories?
    if (arg_map.containsKey("categories"))
    {
      String[] cats = arg_map.get("categories").split(",");
      InCategory inc = new InCategory(cats);
      // Apply the conjunction of this function to the existing condition
      FunctionTree and = new FunctionTree(Booleans.and, inc, filter_condition);
      filter_condition = and;
    }
    Fork fork1 = new Fork(2);
    ApplyFunction af = new ApplyFunction(filter_condition);
    Filter filter = new Filter();
    Connector.connect(global_pump, fork1);
    Connector.connect(fork1, TOP, filter, TOP);
    Connector.connect(fork1, BOTTOM, af, INPUT);
    Connector.connect(af, OUTPUT, filter, BOTTOM);

    // Determine what to compute
    String to_compute = "categories";
    if (arg_map.containsKey("show"))
    {
      to_compute = arg_map.get("show");
    }
    Processor p_compute = getHoursByCategory();
    if (to_compute.compareTo("weekly-plot") == 0)
    {
      p_compute = getWeeklyHours(true, true);
    }
    if (to_compute.compareTo("weekly") == 0)
    {
      p_compute = getWeeklyHours(false, false);
    }
    if (to_compute.compareTo("weekly-sum") == 0)
    {
      p_compute = getWeeklyHours(true, false);
    }
    // Determine where to send the output
    Sink sink = new Print(stdout).setSeparator("");
    if (arg_map.containsKey("output"))
    {
      String filename = arg_map.get("output");
      sink = new WriteToFile(filename);
    }

    // Connect everything
    Connector.connect(filter, p_compute, sink);

    // Launch computation
    global_pump.run();

    // Exit
    return 0;
  }

  /**
   * Formats the cumulative working time by category
   * @param map
   * @param out
   */
  protected static void formatCumulativeTime(CategoryNode root, PrintStream out)
  {
    out.println(root.toString());
  }

  protected static Processor getHoursByCategory()
  {
    GroupProcessor group = new GroupProcessor(1, 1);
    {
      GetCumulativeTime gct = new GetCumulativeTime();
      KeepLast kl = new KeepLast();
      Connector.connect(gct, kl);
      ApplyFunction to_tree = new ApplyFunction(MapToCategoryTree.instance);
      Connector.connect(kl, to_tree);
      group.addProcessors(gct, kl, to_tree);
      group.associateInput(INPUT, gct, INPUT);
      group.associateOutput(OUTPUT, to_tree, OUTPUT);
    }
    return group;
  }

  protected static Processor getWeeklyHours(boolean cumulate, boolean with_plot)
  {
    GroupProcessor group = new GroupProcessor(1, 1);
    {
      Processor aggregate = new Passthrough();
      if (cumulate)
      {
        aggregate = new Cumulate(new CumulativeFunction<Number>(Numbers.addition));
      }
      GetTimePerPeriod tpw = new GetTimePerPeriod(GetLastSunday.instance, 
          aggregate);
      Trim trim = new Trim(1);
      Connector.connect(tpw, trim);
      UpdateTableMap utm = new UpdateTableMap("Period", "Minutes");
      Connector.connect(trim, utm);
      group.associateInput(INPUT, tpw, INPUT);
      KeepLast last = new KeepLast();
      Connector.connect(utm, last);
      group.addProcessors(tpw, trim, utm, last);
      if (with_plot)
      {
        TimeScatterplot plot = new TimeScatterplot();
        plot.setTitle("Cumulative hours");
        DrawPlot dp = new DrawPlot(plot);
        Connector.connect(last, dp);
        group.addProcessor(dp);
        group.associateOutput(OUTPUT, dp, OUTPUT);
      }
      else
      {
        group.associateOutput(OUTPUT, last, OUTPUT);
      }
    }
    return group;
  }

  /**
   * Gets the command line header
   * @return The header
   */
  protected static String getCliHeader()
  {
    return "PartTime Logger II v" + formatVersion() + " - A time tracking application\n(C) 2005-2021 Sylvain Hallé, all rights reserved\n";
  }
  
  /**
   * Gets the major version number
   * 
   * @return The number
   */
  public static final int getMajor()
  {
    return s_majorVersionNumber;
  }

  /**
   * Gets the minor version number
   * 
   * @return The number
   */
  public static final int getMinor()
  {
    return s_minorVersionNumber;
  }

  /**
   * Gets the revision version number
   * 
   * @return The number
   */
  public static final int getRevision()
  {
    return s_revisionVersionNumber;
  }

  /**
   * Formats the version number into a string
   * 
   * @return The version string
   */
  protected static String formatVersion()
  {
    if (getRevision() == 0)
    {
      return s_majorVersionNumber + "." + s_minorVersionNumber;
    }
    return s_majorVersionNumber + "." + s_minorVersionNumber + "." + s_revisionVersionNumber;
  }
}
