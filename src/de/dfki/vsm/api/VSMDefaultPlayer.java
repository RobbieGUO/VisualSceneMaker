package de.dfki.vsm.api;

import de.dfki.vsm.model.project.PlayerConfig;
import de.dfki.vsm.runtime.project.RunTimeProject;
import de.dfki.vsm.runtime.value.AbstractValue;

import java.util.LinkedList;

/**
 * @author Gregor Mehlmann
 */
public class VSMDefaultPlayer extends VSMScenePlayer {

    // The Default Scene Player
    public static VSMDefaultPlayer sInstance;

    ////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////
    private VSMDefaultPlayer(
            final RunTimeProject project,
            final PlayerConfig config) {

        // Initialize The Scene Player
        super(project, config);

        // Print Some Debug Information
        mVSM3Log.message("Creating VSM Default Scene Player");
    }

    ////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////
    public static synchronized VSMDefaultPlayer getInstance(
            final RunTimeProject project,
            final PlayerConfig config) {
        if (sInstance == null) {
            sInstance = new VSMDefaultPlayer(project, config);
        }
        // Return The Singelton Instance
        return sInstance;
    }

    ////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////
    @Override
    public void launch() {

        //
        super.launch();

        //
        mVSM3Log.message("Launching Default Scene Player");
    }

    ////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////
    @Override
    public void unload() {

        //
        mVSM3Log.message("Unloading Default Scene Player");

        //
        super.unload();
    }

    ////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////
    @Override
    public void handle(final VSMAgentClient client) {
    }

    ////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////
    @Override
    public void play(final String name, final LinkedList<AbstractValue> args) {
    }
}
