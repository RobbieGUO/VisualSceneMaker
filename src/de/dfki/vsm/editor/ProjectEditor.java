package de.dfki.vsm.editor;

//~--- non-JDK imports --------------------------------------------------------
import de.dfki.vsm.editor.event.FunctionSelectedEvent;
import de.dfki.vsm.editor.event.NodeSelectedEvent;
import de.dfki.vsm.editor.event.TreeEntrySelectedEvent;
import de.dfki.vsm.editor.event.WorkSpaceSelectedEvent;
import de.dfki.vsm.editor.script.ScriptEditorPanel;
import de.dfki.vsm.editor.util.Preferences;
import de.dfki.vsm.util.evt.EventCaster;
import de.dfki.vsm.util.evt.EventListener;
import de.dfki.vsm.util.evt.EventObject;
import de.dfki.vsm.util.log.LOGDefaultLogger;

//~--- JDK imports ------------------------------------------------------------
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;

import java.util.Observer;

import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JSplitPane;
import javax.swing.plaf.basic.BasicSplitPaneDivider;
import javax.swing.plaf.basic.BasicSplitPaneUI;

/**
 * @author Gregor Mehlmann
 * @author Patrick Gebhard
 */
public class ProjectEditor extends JSplitPane implements EventListener, Observer {

    // The editor project managed by this editor
    private final EditorProject mEditorProject;
    //
    private final SceneFlowEditor mSceneFlowEditor;
    private final ScriptEditorPanel mSceneDocEditor;
    //
    private final Observable mObservable = new Observable();
    private final LOGDefaultLogger mLogger = LOGDefaultLogger.getInstance();
    private final EventCaster mEventCaster = EventCaster.getInstance();

    private final double topElementRatio = 0.6;
    private JDialog quitDialog;

    /**
     * *************************************************************************
     *
     *
     *
     *************************************************************************
     */
    private class Observable extends java.util.Observable {

        public void update(Object obj) {
            setChanged();
            notifyObservers(obj);
        }
    }

    @Override
    public void update(java.util.Observable obs, Object obj) {
        //mLogger.message("ProjectEditor.update(" + obj + ")");
        mObservable.update(obj);
    }

    /**
     *
     *
     *
     *
     *
     */
    @Override
    public void update(EventObject evt) {
        //System.out.println(evt.getClass());
        if (evt instanceof FunctionSelectedEvent || evt instanceof TreeEntrySelectedEvent) {
            {
                showSceneDocEditor();
            }
        }
        if (evt instanceof NodeSelectedEvent && !mSceneDocEditor.isPinPricked()) {
            hideSceneDocEditor();
        }
    }

    /**
     * *************************************************************************
     *
     *
     *
     *************************************************************************
     */
    public ProjectEditor(EditorProject project) {
        super(JSplitPane.VERTICAL_SPLIT, true);

        mEditorProject = project;
        mSceneDocEditor = new ScriptEditorPanel(mEditorProject.getSceneScript(), mEditorProject.getSceneFlow(), mEditorProject.getEditorConfig(), this);
        mSceneFlowEditor = new SceneFlowEditor(mEditorProject.getSceneFlow(), mEditorProject, mSceneDocEditor);

        mObservable.addObserver(mSceneFlowEditor);
        mObservable.addObserver(mSceneDocEditor);

        mEventCaster.append(this);

        NodeSelectedEvent e = new NodeSelectedEvent(this, mEditorProject.getSceneFlow());
        EventCaster.getInstance().convey(e);

        WorkSpaceSelectedEvent ev = new WorkSpaceSelectedEvent(this);
        EventCaster.getInstance().convey(ev);

        initComponents();
    }

    public SceneFlowEditor getSceneFlowEditor() {
        return mSceneFlowEditor;
    }

    public ScriptEditorPanel getSceneDocEditor() {
        return mSceneDocEditor;
    }

    public EditorProject getEditorProject() {
        return mEditorProject;
    }

    /**
     *
     *
     *
     *
     *
     */
    public void close() {
        if (mEditorProject.hasChanged()) {
            OKButton mYesButton = new OKButton();
            mYesButton.setText(" Yes     ");
            mYesButton.addMouseListener(new java.awt.event.MouseAdapter() {
                public void mouseClicked(java.awt.event.MouseEvent evt) {
                    save();
                    disposeAfterDialog();
                }
            });
            //NO BUTTON
            CancelButton mNoButton = new CancelButton();
            mNoButton.setText("  No       ");
            mNoButton.addMouseListener(new java.awt.event.MouseAdapter() {
                public void mouseClicked(java.awt.event.MouseEvent evt) {
                    disposeAfterDialog();
                }
            });
            //CANCEL BUTTON
//            CancelButton mCancelButton = new CancelButton();
//            mCancelButton.setText("Cancel   ");
//            mCancelButton.addMouseListener(new java.awt.event.MouseAdapter() {
//                public void mouseClicked(java.awt.event.MouseEvent evt) {
//                    quitDialog.;
//                }
//            });
//            int response = JOptionPane.showConfirmDialog(
//                    this, "The project \"" + mProject.getProjectName() + "\" has changed.  Save it?",
//                    "Save before quitting?",
//                    JOptionPane.YES_NO_OPTION);
            JOptionPane optionPane = new JOptionPane();
            optionPane.setBackground(Color.white);
            optionPane.setMessage("The project " + mEditorProject.getProjectName() + " has changed.  Save it?");
            optionPane.setMessageType(JOptionPane.INFORMATION_MESSAGE);
            optionPane.setOptions(new Object[]{mYesButton, mNoButton});
            quitDialog = optionPane.createDialog("Save before quitting?");
            quitDialog.setVisible(true);
//            if (response == JOptionPane.YES_OPTION) {
//                save();
//            } else if (response == JOptionPane.CANCEL_OPTION) {
//            } else if (response == JOptionPane.NO_OPTION) {
//            } else {
//            }
        }

    }

    private void disposeAfterDialog() {
        // Delete all observers
        mObservable.deleteObservers();
        // Close / Cleanup
        mSceneFlowEditor.close();
        mSceneDocEditor.close();
        quitDialog.dispose();
    }

    // Save the project managed by this editor
    public final boolean save() {
        return mEditorProject.save();
    }

    /**
     *
     *
     *
     *
     *
     */
    private void initComponents() {
        //
        setBorder(BorderFactory.createEmptyBorder());
        setBackground(Color.WHITE);
        setResizeWeight(Float.valueOf(Preferences.getProperty("sceneflow_sceneeditor_ratio")));
        setOneTouchExpandable(true);

//        final Polygon pUp = new Polygon();
//        pUp.addPoint(1, 4);
//        pUp.addPoint(5, 0);
//        pUp.addPoint(9, 4);
//
//        final Polygon pDown = new Polygon();
//        pDown.addPoint(13, 0);
//        pDown.addPoint(17, 4);
//        pDown.addPoint(21, 0);
        //ProjectEditor thisPE = this;
        setUI(new BasicSplitPaneUI() {

            @Override
            public BasicSplitPaneDivider createDefaultDivider() {
                return new BasicSplitPaneDivider(this) {
                    /**
                     * Shows the bottom part of the editor when mouse goes over
                     * the border
                     *
                     * @param me
                     */
                    @Override
                    protected void processMouseEvent(MouseEvent me) {
                        super.processMouseEvent(me);
                        switch (me.getID()) {

                            case MouseEvent.MOUSE_ENTERED:
                                if (!mSceneDocEditor.isPinPricked()) {
                                    showSceneDocEditor();
                                }
                                break;
                            case MouseEvent.MOUSE_RELEASED:
                                Preferences.setProperty("propertiesdividerlocation", String.valueOf(((ProjectEditor) this.getParent()).getDividerLocation()));
                                mSceneDocEditor.prickPin();
                                break;
                        }
                    }

                };

            }

        });

        setDividerSize(10);

        setContinuousLayout(true);
        mSceneFlowEditor.setMinimumSize(new Dimension(10, 10));
        mSceneFlowEditor.setMaximumSize(new Dimension(10000, 3000));
        setTopComponent(mSceneFlowEditor);
        mSceneDocEditor.setMinimumSize(new Dimension(10, 10));
        mSceneDocEditor.setMaximumSize(new Dimension(10000, 3000));
        setBottomComponent(mSceneDocEditor);

        // setting size
        boolean showSceneFlowEditor = Boolean.valueOf(Preferences.getProperty("showscenefloweditor"));
        boolean showSceneDocEditor = Boolean.valueOf(Preferences.getProperty("showsceneeditor"));

        if (!showSceneFlowEditor) {
            setDividerLocation(1d);
        }

        if (showSceneDocEditor && showSceneFlowEditor) {
            setDividerLocation(Integer.parseInt(Preferences.getProperty("propertiesdividerlocation")));
        }

        mSceneDocEditor.addComponentListener(
                new ComponentListener() {

                    @Override
                    public void componentResized(ComponentEvent e
                    ) {
                        if (mSceneFlowEditor.getSize().height == 0) {
                            Preferences.setProperty("showscenefloweditor", "false");
                            Preferences.setProperty("showsceneeditor", "true");
                        } else {
                            Preferences.setProperty("showscenefloweditor", "true");
                        }
                        if (mSceneDocEditor.getSize().height == 0) {
                            Preferences.setProperty("showscenefloweditor", "true");
                            Preferences.setProperty("showsceneeditor", "false");
                        } else {
                            Preferences.setProperty("showsceneeditor", "true");
                        }
                        Preferences.save();
                    }

                    @Override
                    public void componentMoved(ComponentEvent e
                    ) {
                    }

                    @Override
                    public void componentShown(ComponentEvent e
                    ) {
                    }

                    @Override
                    public void componentHidden(ComponentEvent e
                    ) {
                    }
                }
        );
    }

    /**
     * Shows the bottom part of the project editor
     */
    public void showSceneDocEditor() {
        setDividerLocation(Integer.parseInt(Preferences.getProperty("propertiesdividerlocation")));
    }

    /*
     * Hides the bottom part of the project editor
     */
    public void hideSceneDocEditor() {
        this.setDividerLocation(1d);
    }

}
