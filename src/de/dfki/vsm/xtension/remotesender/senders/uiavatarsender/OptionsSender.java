package de.dfki.vsm.xtension.remotesender.senders.uiavatarsender;

import de.dfki.vsm.xtension.remotesender.sender.DataSendable;

import java.util.Arrays;
import java.util.LinkedList;

/**
 * Created by alvaro on 5/2/17.
 */
public class OptionsSender extends AvatarSender {
    public static final String MESSAGE_TYPE = "options";

    public OptionsSender(String message, String separator) {
       super(message, separator);
    }

    @Override
    public void prepareData() {
        if(hasSeparator()){
            separateOptions();
        }else{
            options.add(message);
        }
    }

    @Override
    protected String getSendingType() {
        return MESSAGE_TYPE;
    }

    private void separateOptions() {
        String [] messages = message.split(separator);
        options.addAll(Arrays.asList(messages));
    }

    private boolean hasSeparator() {
        return !separator.equals("");
    }



}
