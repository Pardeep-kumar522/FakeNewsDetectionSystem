package application;

public class VerificationResult {
    public String status;
    public String message;
    public long verificationTime;
    
    public VerificationResult(String status, String message) {
        this.status = status;
        this.message = message;
        this.verificationTime = System.currentTimeMillis();
    }
}