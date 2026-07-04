package application;
 
import java.util.concurrent.CompletableFuture;
 
/**
 * RealTimeVerifier — delegates to RealInternetVerifier.
 * Kept for backward compatibility with RealTimeVerificationService.java
 */
public class RealTimeVerifier {
 
    public static CompletableFuture<VerificationResult> verifyInRealTime(
            String source, String title, String content) {
        // Fully delegate to the real internet verifier
        return RealInternetVerifier.verifyFromInternet(source, title, content);
    }
}
 