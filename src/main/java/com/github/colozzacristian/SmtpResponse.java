package com.github.colozzacristian;

public class SmtpResponse {

    private int code;
    private String message;

    public SmtpResponse(int code, String message) {
        this.code = code;
        this.message = message;
    }

    /**
     * Constructs an SmtpResponse from a raw SMTP response string.
     * @param rawResponse
     */
    public SmtpResponse(String rawResponse) {
        String[] parts = rawResponse.split(" ", 2);
        if (parts.length == 0 || !parts[0].matches("\\d{3}")) {
            throw new IllegalArgumentException(String.format("Invalid SMTP response: %s", rawResponse));
        }
        this.code = Integer.parseInt(parts[0]);
        this.message = parts.length > 1 ? parts[1] : "";
    }

    public int getCode() {
        return code;
    }
    public String getMessage() {
        return message;
    }

    
}
