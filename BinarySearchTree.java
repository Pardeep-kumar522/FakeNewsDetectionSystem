package application;

import java.util.ArrayList;
import java.util.Date;

public class BinarySearchTree {
    
    private BSTNode root;
    
    private static class BSTNode {
        Article article;
        BSTNode left, right;
        BSTNode(Article article) {
            this.article = article;
            left = right = null;
        }
    }
    
    public void insert(Article article) {
        root = insertRec(root, article);
    }
    
    private BSTNode insertRec(BSTNode root, Article article) {
        if (root == null) return new BSTNode(article);
        
        int cmp = article.getSource().compareToIgnoreCase(root.article.getSource());
        if (cmp < 0) {
            root.left = insertRec(root.left, article);
        } else if (cmp > 0) {
            root.right = insertRec(root.right, article);
        }
        return root;
    }
    
    public ArrayList<Article> search(String source) {
        ArrayList<Article> results = new ArrayList<>();
        searchRec(root, source, results);
        return results;
    }
    
    private void searchRec(BSTNode root, String source, ArrayList<Article> results) {
        if (root == null) return;
        int cmp = source.compareToIgnoreCase(root.article.getSource());
        if (cmp == 0) {
            results.add(root.article);
            searchRec(root.left, source, results);
            searchRec(root.right, source, results);
        } else if (cmp < 0) {
            searchRec(root.left, source, results);
        } else {
            searchRec(root.right, source, results);
        }
    }
    
    // Search by date range
    public ArrayList<Article> searchByDateRange(Date startDate, Date endDate) {
        ArrayList<Article> results = new ArrayList<>();
        inorderSearchByDate(root, startDate, endDate, results);
        return results;
    }
    
    private void inorderSearchByDate(BSTNode node, Date startDate, Date endDate, 
                                      ArrayList<Article> results) {
        if (node == null) return;
        inorderSearchByDate(node.left, startDate, endDate, results);
        
        Date articleDate = node.article.getTimestamp();
        if ((startDate == null || articleDate.after(startDate) || articleDate.equals(startDate)) &&
            (endDate == null || articleDate.before(endDate) || articleDate.equals(endDate))) {
            results.add(node.article);
        }
        
        inorderSearchByDate(node.right, startDate, endDate, results);
    }
    
    // Get sorted articles by source (inorder traversal)
    public ArrayList<Article> getArticlesSortedBySource() {
        ArrayList<Article> sortedList = new ArrayList<>();
        inorderCollect(root, sortedList);
        return sortedList;
    }
    
    private void inorderCollect(BSTNode node, ArrayList<Article> list) {
        if (node == null) return;
        inorderCollect(node.left, list);
        list.add(node.article);
        inorderCollect(node.right, list);
    }
}