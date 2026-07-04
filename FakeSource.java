package application;

public class FakeSource {
    private String sourceName;
    private int reportCount;
    private boolean flagged;
    
    public FakeSource(String sourceName) {
        this.sourceName = sourceName;
        this.reportCount = 1;
        this.flagged = false;
    }
    
    public void incrementReport() {
        reportCount++;
        if (reportCount > 5 && !flagged) {
            flagged = true;
        }
    }
    
    public String getSourceName() { return sourceName; }
    public int getReportCount() { return reportCount; }
    public boolean isFlagged() { return flagged; }
    public void setFlagged(boolean flagged) { this.flagged = flagged; }
}