package application;
 
import javafx.concurrent.Service;
import javafx.concurrent.Task;
 
public class RealTimeVerificationService extends Service<VerificationResult> {
    private String source;
    private String title;
    private String content;
 
    public RealTimeVerificationService(String source, String title, String content) {
        this.source  = source;
        this.title   = title;
        this.content = content;
    }
 
    @Override
    protected Task<VerificationResult> createTask() {
        return new Task<VerificationResult>() {
            @Override
            protected VerificationResult call() throws Exception {
                updateMessage("🌐 Connecting to DuckDuckGo API...");
                updateProgress(0, 100);
 
                updateMessage("📖 Querying Wikipedia for source credibility...");
                updateProgress(25, 100);
 
                updateMessage("🔍 Checking domain reputation database...");
                updateProgress(50, 100);
 
                updateMessage("📝 Analysing content for red flags...");
                updateProgress(75, 100);
 
                updateMessage("⚖️ Calculating final verdict...");
                updateProgress(90, 100);
 
                VerificationResult result =
                    RealInternetVerifier.verifyFromInternet(source, title, content).get();
 
                updateProgress(100, 100);
                return result;
            }
        };
    }
}