package application;

import java.util.ArrayList;

public class User {
    private int id;
    private String name;
    private String password;
    private SubmissionNode submissionHead;
    private int submissionCount;
    
    public User(int id, String name, String password) {
        this.id = id;
        this.name = name;
        this.password = password;
        this.submissionHead = null;
        this.submissionCount = 0;
    }
    
    public void addToHistory(Article article) {
        SubmissionNode newNode = new SubmissionNode(article);
        if (submissionHead == null) {
            submissionHead = newNode;
        } else {
            SubmissionNode current = submissionHead;
            while (current.next != null) {
                current = current.next;
            }
            current.next = newNode;
        }
        submissionCount++;
    }
    
    public ArrayList<Article> getArticlesAsList() {
        ArrayList<Article> list = new ArrayList<>();
        SubmissionNode current = submissionHead;
        while (current != null) {
            list.add(current.article);
            current = current.next;
        }
        return list;
    }
    
    public int getId() { return id; }
    public String getName() { return name; }
    public String getPassword() { return password; }
    public int getSubmissionCount() { return submissionCount; }
}