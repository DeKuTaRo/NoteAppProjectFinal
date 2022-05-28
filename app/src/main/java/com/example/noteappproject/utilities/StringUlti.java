package com.example.noteappproject.utilities;

public class StringUlti {
    public final static String getSubEmailName(String email){
        return email.substring(0, email.indexOf("@"));
    }
}
