package de.dfki.vsm.model.configs;

//~--- non-JDK imports --------------------------------------------------------

import de.dfki.vsm.util.ios.IndentWriter;
import de.dfki.vsm.util.xml.XMLParseAction;
import de.dfki.vsm.util.xml.XMLParseError;
import de.dfki.vsm.util.xml.XMLWriteError;

import org.w3c.dom.Element;

//~--- JDK imports ------------------------------------------------------------

import java.util.ArrayList;

/**
 *
 * @author Gregor Mehlmann
 */
public class ProjectConfig extends BasicConfig {

    ////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////
    public ProjectConfig() {
        super();
    }

    ////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////
    public ProjectConfig(final ArrayList<ConfigEntry> list) {
        super(list);
    }

    ////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////
    @Override
    public final void writeXML(final IndentWriter stream) throws XMLWriteError {
        stream.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        stream.println("<ProjectConfig>");
        stream.push();

        for (final ConfigEntry entry : mEntryList) {
            entry.writeXML(stream);
            stream.endl();
        }

        stream.pop().println("</ProjectConfig>").flush();
    }

    ////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////
    @Override
    public final void parseXML(final Element element) throws XMLParseError {
        XMLParseAction.processChildNodes(element, "Entry", new XMLParseAction() {
            @Override
            public void run(final Element element) throws XMLParseError {
                final ConfigEntry entry = new ConfigEntry();

                entry.parseXML(element);
                mEntryList.add(entry);
            }
        });
    }

    ////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////
    @Override
    public ProjectConfig getCopy() {
        return new ProjectConfig(copyEntryList());
    }
}
