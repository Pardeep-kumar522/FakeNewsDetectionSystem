package application;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class VerificationLog {
    private String source;
    private String status;
    private String timestamp;
    
    public VerificationLog(String source, String status) {
        this.source = source;
        this.status = status;
        this.timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }
    
    public String getSource() { return source; }
    public String getStatus() { return status; }
    public String getTimestamp() { return timestamp; }
}