package de.dfki.vsm.xtension.remotesender.factories;

import de.dfki.vsm.model.project.PluginConfig;
import de.dfki.vsm.xtension.remotesender.sender.Clientable;
import de.dfki.vsm.xtension.remotesender.sender.clients.DummyClient;
import de.dfki.vsm.xtension.remotesender.sender.clients.TCPIPClient;

/**
 * Created by alvaro on 5/2/17.
 */
public class ClientsFactory {
    private final PluginConfig config;
    private final String rHost;
    private final String type;
    private final int rPort;

    public ClientsFactory(PluginConfig mConfig) {
        this.config = mConfig;
        rHost = config.getProperty("rHost");
        type = config.getProperty("connection_type");
        String port = config.getProperty("rPort");
        if(port != null)
            rPort = Integer.parseInt(port);
        else
            rPort = 0;
    }

    public Clientable buildClient() {
        if(type.equals("tcp/ip")){
            return new TCPIPClient(rHost, rPort);
        }

        return new DummyClient();
    }
}
