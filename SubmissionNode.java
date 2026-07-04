package application;

public class SubmissionNode {
    public Article article;
    public SubmissionNode next;
    
    public SubmissionNode(Article article) {
        this.article = article;
        this.next = null;
    }
}