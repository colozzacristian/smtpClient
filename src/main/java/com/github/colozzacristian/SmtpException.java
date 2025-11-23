package com.github.colozzacristian;

import java.io.IOException;

public class SmtpException extends IOException{
    private SmtpResponse response;

    public SmtpException(SmtpResponse response,String message){
        super(message);
        this.response = response;
    }

    //found it on IOException.class so i copied it.
    public SmtpException(SmtpResponse response,String message,Throwable cause){
        super(message,cause);
        this.response = response;
    }

    public SmtpResponse getResponse(){
        return response;
    }
    
}
