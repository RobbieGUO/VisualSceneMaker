package de.dfki.vsm.util.jpl;

//~--- non-JDK imports --------------------------------------------------------

import de.dfki.vsm.util.log.LOGDefaultLogger;
import de.dfki.vsm.util.sys.SYSUtilities;

import jpl.JPL;
import jpl.Query;
import jpl.Term;
import jpl.Variable;

//~--- JDK imports ------------------------------------------------------------

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

/**
 * @author Gregor Mehlmann
 */
public final class JPLEngine {

    // The System File Logger
    private static LOGDefaultLogger sLogger = LOGDefaultLogger.getInstance();

    // The Liveliness Flag
    private static volatile boolean sAlive;

    // Load Default Source Files
    //public static void load() {
    //    load(SYSUtilities.sPROLOG_FILE_BASE);
    //}

    // Load A Prolog Source File
    public static void load(final String source) {

        // Construct A JPL Loader
        final JPLLoader loader = new JPLLoader(source);

        // Start The JPL Loader
        loader.start();

        // Await The JPL Loader
        try {

            // Join The JPL Loader
            loader.join();
        } catch (Exception exc) {

            // Print Debug Information
            sLogger.failure(exc.toString());
        }
    }

    // Get String Representation
    public static synchronized String string() {
        if (sAlive) {
            String[] arg    = JPL.getActualInitArgs();
            String   argstr = JPL.version_string();

            argstr += ",[";

            for (int i = 0; i < arg.length; i++) {
                argstr += arg[i];

                if (i < arg.length - 1) {
                    argstr += ",";
                }
            }

            argstr += "]";

            return "JPLEngine(" + argstr + ")";
        }

        return null;
    }

    // Initialize The JPL Engine
    public static synchronized void init() {
        if (!sAlive) {

            // Initialize JPL Only Once
            JPL.init();

            // Set The Liveliness Flag
            sAlive = true;

            // Print Debug Information
            sLogger.message("Initializing JPL Engine '" + string() + "'");
        }
    }

    // Call A Query On The Engine
    public static synchronized JPLResult query(final String strquery) {

        // Print Debug Information
        // sLogger.message("Executing Query '" + strquery + "' In JPL Engine '" + JPLEngine.string() + "'");
        // Eventually Initialize JPL
        init();

        // The New Query To The JPL
        Query jplquery = null;

        // The Result Of The Query
        JPLResult result = new JPLResult(strquery);

        // Get Solutions/Substitutions
        try {

            // Replace All Whitespaces
            String querystr = strquery;    // .replaceAll("\\s", "");

            // Append A Period At End
            if (!querystr.endsWith(".")) {
                querystr += ".";
            }

            // Execute The JPL Query
            jplquery = new Query(querystr);

            // Process All Solutions
            while (jplquery.hasMoreElements()) {

                // Get Next Possible Solution
                Hashtable solution = (Hashtable) jplquery.nextElement();

                // Create The Substitutions
                HashMap<String, String> substitutions = new HashMap<String, String>();

                // Inspect The Solution
                if (!solution.isEmpty()) {

                    // Inspect The Substitutions
                    for (Object object : solution.entrySet()) {
                        Map.Entry entry = (Map.Entry) object;

                        // Get The Substitution Term
                        Term substterm = (Term) entry.getValue();

                        // Check If The Term Is Valid
                        if (!(substterm instanceof Variable)) {

                            // Get The Name Of The Variable
                            String varname = (String) entry.getKey();

                            // Compute The Term String Value
                            String subname = entry.getValue().toString();

                            // Insert The Pair To The Result
                            substitutions.put(varname, subname);
                        }
                    }

                    // Add The Substitutions
                    result.add(substitutions);
                } else {

                    // Add An Empty Solution
                    result.add(new HashMap<String, String>());
                }
            }
        } catch (Exception exc) {

            // Print Debug Information
            // sLogger.failure(exc.toString());
            exc.printStackTrace();
        } finally {

            // Close The New Query
            if (jplquery != null) {
                jplquery.rewind();
                jplquery.close();
            }

            // Print Debug Information
            // sLogger.message("Terminating Query '" + strquery + "' In JPL Engine '" + JPLEngine.string() + "'");
            // Always Return Result
            return result;
        }
    }
}
