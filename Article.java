package application;

import java.util.Date;
import java.text.SimpleDateFormat;

public class Article implements Comparable<Article> {
    private int id;
    private String title;
    private String summary;
    private String author;
    private String source;
    private Date timestamp;
    private String status;
    
    public Article(int id, String title, String summary, String author, 
                   String source, Date timestamp, String status) {
        this.id = id;
        this.title = title;
        this.summary = summary;
        this.author = author;
        this.source = source;
        this.timestamp = timestamp;
        this.status = status;
    }
    
    public int getId() { return id; }
    public String getTitle() { return title; }
    public String getSummary() { return summary; }
    public String getAuthor() { return author; }
    public String getSource() { return source; }
    public Date getTimestamp() { return timestamp; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    // Get formatted date/time string
    public String getFormattedDateTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sdf.format(timestamp);
    }
    
    // Get just the date
    public String getFormattedDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        return sdf.format(timestamp);
    }
    
    // Get just the time
    public String getFormattedTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        return sdf.format(timestamp);
    }
    
    // For sorting by source (alphabetical)
    @Override
    public int compareTo(Article other) {
        return this.source.compareToIgnoreCase(other.source);
    }
    
    // Comparator for sorting by timestamp (newest first)
    public static java.util.Comparator<Article> sortByTimestampDesc() {
        return (a1, a2) -> a2.timestamp.compareTo(a1.timestamp);
    }
    
    // Comparator for sorting by timestamp (oldest first)
    public static java.util.Comparator<Article> sortByTimestampAsc() {
        return (a1, a2) -> a1.timestamp.compareTo(a2.timestamp);
    }
    
    // Comparator for sorting by source A-Z
    public static java.util.Comparator<Article> sortBySourceAsc() {
        return (a1, a2) -> a1.source.compareToIgnoreCase(a2.source);
    }
    
    // Comparator for sorting by source Z-A
    public static java.util.Comparator<Article> sortBySourceDesc() {
        return (a1, a2) -> a2.source.compareToIgnoreCase(a1.source);
    }
}