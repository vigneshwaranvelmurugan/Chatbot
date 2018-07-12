package com.systimanx.chatbot;

/**
 * Created by systimanx on 9/7/18.
 */

public class chatmodel {

    private String msgText;
    private String msgUser;




    public chatmodel(String msgText, String msgUser){
        this.msgText = msgText;
        this.msgUser = msgUser;

    }


    public chatmodel(){

    }

    public String getMsgText() {
        return msgText;
    }

    public void setMsgText(String msgText) {
        this.msgText = msgText;
    }

    public String getMsgUser() {
        return msgUser;
    }

    public void setMsgUser(String msgUser) {
        this.msgUser = msgUser;
    }
}
