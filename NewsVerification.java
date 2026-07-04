package application;

public class NewsVerification {
    public String source;
    public String status;
    public String description;
    public String url;
    
    public NewsVerification(String source, String status, String description, String url) {
        this.source = source;
        this.status = status;
        this.description = description;
        this.url = url;
    }
}