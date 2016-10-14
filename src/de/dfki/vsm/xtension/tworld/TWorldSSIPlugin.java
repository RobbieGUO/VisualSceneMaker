package de.dfki.vsm.xtension.tworld;

import de.dfki.vsm.xtension.ssi.event.SSIEventArray;
import de.dfki.vsm.model.project.PluginConfig;
import de.dfki.vsm.runtime.project.RunTimeProject;
import de.dfki.vsm.util.jpl.JPLEngine;
import de.dfki.vsm.xtension.ssi.SSIRunTimePlugin;
import de.dfki.vsm.xtension.ssi.event.SSIEventEntry;
import de.dfki.vsm.xtension.ssi.event.data.SSIEventData;
import de.dfki.vsm.xtension.ssi.event.data.SSIStringData;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map.Entry;

/**
 * @author Gregor Mehlmann
 * @author Patrick Gebhard
 */
public final class TWorldSSIPlugin extends SSIRunTimePlugin {

    // The map of processes
    private final HashMap<String, Process> mProcessMap
            = new HashMap();
    // The flag if we user the JPL
    private final boolean mUseJPL;

    // Construct SSI plugin
    public TWorldSSIPlugin(
            final PluginConfig config,
            final RunTimeProject project) {
        super(config, project);
        // Get the JPL flag value
        mUseJPL = Boolean.parseBoolean(
                mConfig.getProperty("usejpl"));
    }

    // Launch SSI plugin
    @Override
    public void launch() {
        super.launch();
     
         // Load the plugin configuration
         final String ssidir = mConfig.getProperty("ssidir");
         final String ssibat = mConfig.getProperty("ssibat");
         // Create the plugin's processes
         try {
         mProcessMap.put(ssibat, Runtime.getRuntime().exec(
         "cmd /c start " + ssibat + "" + "", null, new File(ssidir)));
         } catch (final IOException exc) {
         mLogger.failure(exc.toString());
         }
    }

    // Unload SSI plugin
    @Override
    public void unload() {
        super.unload();
        // Wait for pawned processes
        for (final Entry<String, Process> entry : mProcessMap.entrySet()) {
            // Get the process entry
            final String name = entry.getKey();
            final Process process = entry.getValue();
            try {
                // Kill the processes
                final Process killer = Runtime.getRuntime().exec("taskkill /F /IM " + name);
                // Wait for the killer
                killer.waitFor();
                // Print some information 
                mLogger.message("Joining killer " + name + "");
                // Wait for the process
                process.waitFor();
                // Print some information 
                mLogger.message("Joining process " + name + "");
            } catch (final IOException | InterruptedException exc) {
                mLogger.failure(exc.toString());
            }
        }
    }

    // Handle SSI event array
    @Override
    public void handle(final SSIEventArray array) {
        // Print some information 
        mLogger.message("Handling SSI event array:\n " + array.toString());
        for (final SSIEventEntry event : array.getEventList()) {
            final SSIEventData obj = event.getData();
            if (event.getSender().equals("audio")
                    && event.getEvent().equals("vad")) {
                if (event.getState().equalsIgnoreCase("completed")) {
                    // User stopped speaking
                    mLogger.success("User stopped speaking");
                    if (mUseJPL) {
                        JPLEngine.query("now(Time), "
                                + "jdd(["
                                + "type:" + "event" + ","
                                + "name:" + "user" + ","
                                + "mode:" + "voice" + ","
                                + "data:" + "stop" + ","
                                + "time:" + "Time" + ","
                                + "from:" + event.getFrom() + ","
                                + "life:" + event.getDur() + ","
                                + "conf:" + event.getProb()
                                + "]).");
                    } else {
                        // Set speaking variable
                        mProject.setVariable("UserIsSpeaking", false);
                    }
                } else if (event.getState().equalsIgnoreCase("continued")) {
                    // User started speaking
                    mLogger.success("User started speaking");
                    if (mUseJPL) {
                        JPLEngine.query("now(Time), "
                                + "jdd(["
                                + "type:" + "event" + ","
                                + "name:" + "user" + ","
                                + "mode:" + "voice" + ","
                                + "data:" + "start" + ","
                                + "time:" + "Time" + ","
                                + "from:" + event.getFrom() + ","
                                + "life:" + event.getDur() + ","
                                + "conf:" + event.getProb()
                                + "]).");
                    } else {
                        // Set speaking variable
                        mProject.setVariable("UserIsSpeaking", true);
                    }
                } else {
                    // Should not happen
                }
            } else if (event.getSender().equals("speech")
                    && event.getEvent().equals("act")) {
                final String keyword = ((SSIStringData) obj).toString().trim();
                // User started speaking
                mLogger.success("User said '" + keyword + "'");
                if (mUseJPL) {
                    JPLEngine.query("now(Time), "
                            + "jdd(["
                            + "type:" + "event" + ","
                            + "name:" + "user" + ","
                            + "mode:" + "speech" + ","
                            + "data:" + "'" + keyword + "'" + ","
                            + "time:" + "Time" + ","
                            + "from:" + event.getFrom() + ","
                            + "life:" + event.getDur() + ","
                            + "conf:" + event.getProb()
                            + "]).");
                } else {
                    // Set keyword variable
                    mProject.setVariable("UserSaidKeyword", keyword);
                }
            } else {
                // Should not happen
            }
            /*
             if (obj instanceof SSIStringData) {
             final TWorldSSIData mSSIData = new TWorldSSIData(
             ((SSIStringData) array.getEventList().get(0).getData()).toString());

             //mLogger.message("Handling SSI data " + mSSIData);
             final HashMap<String, AbstractValue> values = new HashMap<>();
             values.put("voice_activity", new StringValue(mSSIData.get("voice.activity")));
             String detectedUtterance = mSSIData.get("voice.speechact");
             values.put("voice_keyword", new StringValue(detectedUtterance));

             if (!detectedUtterance.isEmpty()) {
             // PG - added 3.5.2016, check if uttrance is nummeric
             try {
             Integer.parseInt(detectedUtterance);
             values.put("voice_act_is_nummeric", new StringValue("1"));
             } catch (NumberFormatException nfe) {
             values.put("voice_act_is_nummeric", new StringValue("0"));
             }
             } else {
             values.put("voice_act_is_nummeric", new StringValue(""));
             }

             values.put("voice_praat_pitchmean", new StringValue(mSSIData.get("voice.praat.pitchmean")));
             values.put("voice_praat_pitchsd", new StringValue(mSSIData.get("voice.praat.pitchsd")));
             values.put("voice_praat_speechrate", new StringValue(mSSIData.get("voice.praat.speechrate")));
             values.put("voice_praat_intensity", new StringValue(mSSIData.get("voice.praat.intensity")));
             values.put("head_position_x", new StringValue(mSSIData.get("head.position.x")));
             values.put("head_position_y", new StringValue(mSSIData.get("head.position.y")));
             values.put("head_orientation_roll", new StringValue(mSSIData.get("head.orientation.roll")));
             float a = Float.parseFloat(mSSIData.get("head.orientation.pitch")) * -1.0f;
             values.put("head_orientation_pitch", new FloatValue(a));
             values.put("head_orientation_yaw", new StringValue(mSSIData.get("head.orientation.yaw")));
             values.put("head_movement_nod", new StringValue(mSSIData.get("head.movement.nod")));
             values.put("head_movement_shake", new StringValue(mSSIData.get("head.movement.shake")));
             float ba = Float.parseFloat(mSSIData.get("body.activity"));
             values.put("body_activity", new FloatValue(ba));
             values.put("body_energy", new StringValue(mSSIData.get("body.energy")));
             //                values.put("body_posture_leanfront_detected", new StringValue(mSSIData.get("body.posture.leanfront.detected")));
             //                values.put("body_posture_leanfront_duration", new StringValue(mSSIData.get("body.posture.leanfront.duration")));
             //                values.put("body_posture_leanfront_intensity", new StringValue(mSSIData.get("body.posture.leanfront.intensity")));
             //                values.put("body_posture_leanback_detected", new StringValue(mSSIData.get("body.posture.leanback.detected")));
             //                values.put("body_posture_leanback_duration", new StringValue(mSSIData.get("body.posture.leanback.duration")));
             //                values.put("body_posture_leanback_intensity", new StringValue(mSSIData.get("body.posture.leanback.intensity")));
             //                values.put("body_gesture_armsopen_detected", new StringValue(mSSIData.get("body.gesture.armsopen.detected")));
             //                values.put("body_gesture_armsopen_duration", new StringValue(mSSIData.get("body.gesture.armsopen.duration")));
             //                values.put("body_gesture_armsopen_intensity", new StringValue(mSSIData.get("body.gesture.armsopen.intensity")));
             //                values.put("body_gesture_armscrossed_detected", new StringValue(mSSIData.get("body.gesture.armscrossed.detected")));
             //                values.put("body_gesture_armscrossed_duration", new StringValue(mSSIData.get("body.gesture.armscrossed.duration")));
             //                values.put("body_gesture_armscrossed_intensity", new StringValue(mSSIData.get("body.gesture.armscrossed.intensity")));
             //                values.put("body_gesture_lefthandheadtouch_detected", new StringValue(mSSIData.get("body.gesture.lefthandheadtouch.detected")));
             //                values.put("body_gesture_lefthandheadtouch_duration", new StringValue(mSSIData.get("body.gesture.lefthandheadtouch.duration")));
             //                values.put("body_gesture_righthandheadtouch_detected", new StringValue(mSSIData.get("body.gesture.righthandheadtouch.detected")));
             //                values.put("body_gesture_righthandheadtouch_duration", new StringValue(mSSIData.get("body.gesture.righthandheadtouch.duration")));
             values.put("face_expression_smile_detected", new StringValue(mSSIData.get("face.expression.smile.detected")));
             values.put("face_expression_smile_duration", new StringValue(mSSIData.get("face.expression.smile.duration")));
             values.put("face_expression_smile_intensity", new StringValue(mSSIData.get("face.expression.smile.intensity")));

             //mProject.setVariable("usercues", new StructValue(values));
             for (final Entry<String, AbstractValue> value : values.entrySet()) {
             if (mProject.hasVariable(value.getKey())) {

             mProject.setVariable(value.getKey(), value.getValue());
             //mLogger.success("Setting Variable " + value.getKey() + " to " + value.getValue().getConcreteSyntax());
             } else {
             //mLogger.warning("Variable " + value.getKey() + " not defined!");
             }
             }

             long end = System.nanoTime();
             }
             */
        }
    }
}