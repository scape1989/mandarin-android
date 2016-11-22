package com.tomclaw.mandarin.im.icq.dto;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

public class HistDlgState implements Serializable {

    private String sn;
    private Boolean starting;
    private long lastMsgId;
    private Long delUpto;
    private long unreadCnt;
    private Yours yours;
    private Theirs theirs;
    private List<Message> messages = Collections.emptyList();
    private long olderMsgId;
    private List<Person> persons = Collections.emptyList();

    public String getSn() {
        return sn;
    }

    public boolean isStarting() {
        if (starting != null) {
            return starting;
        }
        return false;
    }

    public Long getDelUpTo() {
        return delUpto;
    }

    public long getLastMsgId() {
        return lastMsgId;
    }

    public long getUnreadCnt() {
        return unreadCnt;
    }

    public Yours getYours() {
        return yours;
    }

    public Theirs getTheirs() {
        return theirs;
    }

    public List<Message> getMessages() {
        return messages;
    }

    public long getOlderMsgId() {
        return olderMsgId;
    }

    public List<Person> getPersons() {
        return persons;
    }

}
