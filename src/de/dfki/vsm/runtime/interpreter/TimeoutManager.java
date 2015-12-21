package de.dfki.vsm.runtime.interpreter;

//~--- non-JDK imports --------------------------------------------------------
import de.dfki.vsm.model.sceneflow.language.command.Assignment;
import de.dfki.vsm.model.sceneflow.language.command.Command;
import de.dfki.vsm.model.sceneflow.language.command.invocation.PlayDialogAct;
import de.dfki.vsm.model.sceneflow.language.command.invocation.PlaySceneGroup;
import de.dfki.vsm.model.sceneflow.language.command.invocation.UnblockSceneGroup;
import de.dfki.vsm.model.sceneflow.language.command.expression.BinaryExpression;
import de.dfki.vsm.model.sceneflow.language.command.expression.TernaryExpression;
import de.dfki.vsm.model.sceneflow.language.command.Expression;
import de.dfki.vsm.model.sceneflow.language.command.expression.UnaryExpression;
import de.dfki.vsm.model.sceneflow.language.command.expression.JavaCallExpression;
import de.dfki.vsm.model.sceneflow.language.command.expression.record.ListRecord;
import de.dfki.vsm.model.sceneflow.language.command.expression.record.StructRecord;
import de.dfki.vsm.model.sceneflow.language.command.expression.variable.ArrayVariable;
import de.dfki.vsm.model.sceneflow.language.command.expression.invocation.TimeoutFunction;
import de.dfki.vsm.model.sceneflow.language.command.definition.VariableDefinition;
import de.dfki.vsm.runtime.exceptions.InterpretException;
import de.dfki.vsm.runtime.values.AbstractValue;
import de.dfki.vsm.runtime.values.IntValue;
import de.dfki.vsm.runtime.values.StringValue;
import de.dfki.vsm.util.log.LOGConsoleLogger;
import de.dfki.vsm.util.tpl.TPLTuple;

//~--- JDK imports ------------------------------------------------------------
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

/**
 * @author Not me
 */
public class TimeoutManager {

    private final LOGConsoleLogger mLogger = LOGConsoleLogger.getInstance();
    private final HashMap<TimeoutFunction, TPLTuple<Boolean, TimerTask>> mTimeoutCondList = new HashMap<TimeoutFunction, TPLTuple<Boolean, TimerTask>>();
    private final Timer mTimer = new Timer("Timeout-Manager-Timer");
    private Interpreter mInterpreter;

    public TimeoutManager(Interpreter interpreter) {
        mInterpreter = interpreter;
    }

    public boolean contains(final TimeoutFunction cond) {
        return mTimeoutCondList.containsKey(cond);
    }

    public synchronized void cancel() {
        mTimer.cancel();
    }

    public void clear() {
        for (TPLTuple<Boolean, TimerTask> pair : mTimeoutCondList.values()) {
            pair.setFirst(false);
            pair.getSecond().cancel();
        }

        mTimeoutCondList.clear();
        mLogger.message("Clearing timeout manager");
    }

    public boolean expired(final TimeoutFunction cond) {
        return mTimeoutCondList.get(cond).getFirst();
    }

    public void remove(final TimeoutFunction cond) {

        // mTimeoutCondList.get(cond).
        mTimeoutCondList.get(cond).getSecond().cancel();
        mTimeoutCondList.remove(cond);
        mLogger.message("removing " + cond.getConcreteSyntax() + " ");
    }

    public void start(final TimeoutFunction cond, int timeout) {
        if (contains(cond)) {
            mLogger.message("Already contained " + cond.getConcreteSyntax() + " -> restart");
            remove(cond);
        }

        final TimerTask task = new TimerTask() {
            public void run() {
                mInterpreter.lock();

                if (contains(cond)) {
                    mTimeoutCondList.get(cond).setFirst(true);
                    mLogger.message("Setting true " + cond.getConcreteSyntax());
                }

                // if (mEventObserver != null) {
                mInterpreter.getEventObserver().update();

                // }
                cancel();
                mInterpreter.unlock();
            }
        };

        mTimeoutCondList.put(cond, new TPLTuple<Boolean, TimerTask>(false, task));
        mTimer.schedule(task, timeout);
    }

    ///////////////////////////////////////////////////////////////////////////
    public void startTimeoutHandler(VariableDefinition def, Environment env) throws InterpretException {
        startTimeoutHandler(def.getExp(), env);
    }

    public void startTimeoutHandler(Command cmd, Environment env) throws InterpretException {
        if (cmd instanceof PlaySceneGroup) {
            startTimeoutHandler(((PlaySceneGroup) cmd).getArg(), env);

            for (Expression arg : ((PlaySceneGroup) cmd).getArgList()) {
                startTimeoutHandler(arg, env);
            }
        } else if (cmd instanceof PlayDialogAct) {
            startTimeoutHandler(((PlayDialogAct) cmd).getArg(), env);

            for (Expression arg : ((PlayDialogAct) cmd).getArgList()) {
                startTimeoutHandler(arg, env);
            }
        } else if (cmd instanceof UnblockSceneGroup) {
            startTimeoutHandler(((UnblockSceneGroup) cmd).getArg(), env);
        } else if (cmd instanceof Assignment) {
            startTimeoutHandler(((Assignment) cmd).getVariable(), env);
            startTimeoutHandler(((Assignment) cmd).getInitializer(), env);
        } else if (cmd instanceof Expression) {
            startTimeoutHandler((Expression) cmd, env);
        }
    }

    public void startTimeoutHandler(Expression exp, Environment env) throws InterpretException {
        if (exp instanceof ListRecord) {
            for (Expression arg : ((ListRecord) exp).getExpList()) {
                startTimeoutHandler(arg, env);
            }
        } else if (exp instanceof StructRecord) {
            for (Assignment arg : ((StructRecord) exp).getExpList()) {
                startTimeoutHandler(arg, env);
            }
        } else if (exp instanceof UnaryExpression) {
            startTimeoutHandler(((UnaryExpression) exp).getExp(), env);
        } else if (exp instanceof BinaryExpression) {
            startTimeoutHandler(((BinaryExpression) exp).getLeftExp(), env);
            startTimeoutHandler(((BinaryExpression) exp).getRightExp(), env);
        } else if (exp instanceof TernaryExpression) {
            startTimeoutHandler(((TernaryExpression) exp).getCondition(), env);
            startTimeoutHandler(((TernaryExpression) exp).getThenExp(), env);
            startTimeoutHandler(((TernaryExpression) exp).getElseExp(), env);
        } else if (exp instanceof ArrayVariable) {
            startTimeoutHandler(((ArrayVariable) exp).getExp(), env);
        } else if (exp instanceof TimeoutFunction) {

            /**
             * START TIMEOUT CONDITION TIMER
             */
            startTimeoutHandler(((TimeoutFunction) exp).getExpression(), env);

            AbstractValue value = mInterpreter.getEvaluator().evaluate(((TimeoutFunction) exp).getExpression(), env);

            if (value.getType() == AbstractValue.Type.INT) {
                start((TimeoutFunction) exp, ((IntValue) value).getValue());
            } else if (value.getType() == AbstractValue.Type.STRING) {
                start((TimeoutFunction) exp, Integer.parseInt(((StringValue) value).getValue()));
            } else {
                throw new InterpretException(this, "timeout argument not integer");
            }
        } else if (exp instanceof JavaCallExpression) {
            for (Expression arg : ((JavaCallExpression) exp).getArgList()) {
                startTimeoutHandler(arg, env);
            }
        } else {
            return;
        }
    }
}
