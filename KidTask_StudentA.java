// KidTask - Student A
// GUI + Data Structures (Swing)

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

class User {
    private String username;
    private String role; // CHILD, PARENT, TEACHER

    public User(String username, String role) {
        this.username = username;
        this.role = role;
    }

    public String getUsername() { return username; }
    public String getRole() { return role; }
}

class Task {
    private String id;
    private String title;
    private String description;
    private LocalDate dueDate;
    private int basePoints;
    private boolean completed;
    private Integer rating;
    private String childUsername;

    public Task(String id, String title, String description,
                LocalDate dueDate, int basePoints,
                boolean completed, Integer rating, String childUsername) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.dueDate = dueDate;
        this.basePoints = basePoints;
        this.completed = completed;
        this.rating = rating;
        this.childUsername = childUsername;
    }

    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public LocalDate getDueDate() { return dueDate; }
    public int getBasePoints() { return basePoints; }
    public boolean isCompleted() { return completed; }
    public Integer getRating() { return rating; }
    public String getChildUsername() { return childUsername; }

    public void setCompleted(boolean completed) { this.completed = completed; }
    public void setRating(Integer rating) { this.rating = rating; }
}

class Wish {
    private String id;
    private String description;
    private int requiredLevel;
    private boolean approved;
    private boolean purchased;
    private String childUsername;

    public Wish(String id, String description, int requiredLevel,
                boolean approved, boolean purchased, String childUsername) {
        this.id = id;
        this.description = description;
        this.requiredLevel = requiredLevel;
        this.approved = approved;
        this.purchased = purchased;
        this.childUsername = childUsername;
    }

    public String getId() { return id; }
    public String getDescription() { return description; }
    public int getRequiredLevel() { return requiredLevel; }
    public boolean isApproved() { return approved; }
    public boolean isPurchased() { return purchased; }
    public String getChildUsername() { return childUsername; }

    public void setApproved(boolean approved) { this.approved = approved; }
    public void setPurchased(boolean purchased) { this.purchased = purchased; }
}

class ChildProfile {
    private String username;
    private int totalPoints;
    private int level;

    public ChildProfile(String username, int totalPoints, int level) {
        this.username = username;
        this.totalPoints = totalPoints;
        this.level = level;
    }

    public String getUsername() { return username; }
    public int getTotalPoints() { return totalPoints; }
    public int getLevel() { return level; }
    public void setTotalPoints(int totalPoints) { this.totalPoints = totalPoints; }
    public void setLevel(int level) { this.level = level; }
}

public class KidTaskStudentA extends JFrame {

    private JTextField txtUsername;
    private JComboBox<String> cmbRole;
    private JLabel lblCurrentUser;

    private JTable tblMain;
    private DefaultTableModel tableModel;

    private JButton btnLoadTasks;
    private JButton btnCompleteTask;
    private JButton btnAddTask;
    private JButton btnLoadWishes;
    private JButton btnAddWish;

    private User currentUser;

    // simple in-memory storage for Student A
    private final List<Task> allTasks = new ArrayList<>();
    private final List<Wish> allWishes = new ArrayList<>();

    public KidTaskStudentA() {
        setTitle("KidTask - Student A (GUI + Data Structures)");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 600);
        setLocationRelativeTo(null);
        initComponents();
    }

    private void initComponents() {
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.add(new JLabel("Username:"));
        txtUsername = new JTextField(10);
        topPanel.add(txtUsername);

        topPanel.add(new JLabel("Role:"));
        cmbRole = new JComboBox<>(new String[]{"CHILD", "PARENT", "TEACHER"});
        topPanel.add(cmbRole);

        JButton btnLogin = new JButton("Login");
        topPanel.add(btnLogin);

        lblCurrentUser = new JLabel("Not logged in");
        topPanel.add(lblCurrentUser);

        getContentPane().add(topPanel, BorderLayout.NORTH);

        // Center
        tableModel = new DefaultTableModel();
        tblMain = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(tblMain);
        getContentPane().add(scrollPane, BorderLayout.CENTER);

        // Bottom
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        btnLoadTasks = new JButton("Load My Tasks");
        btnCompleteTask = new JButton("Complete Task");
        btnAddTask = new JButton("Add Task");
        btnLoadWishes = new JButton("Load My Wishes");
        btnAddWish = new JButton("Add Wish");

        bottomPanel.add(btnLoadTasks);
        bottomPanel.add(btnCompleteTask);
        bottomPanel.add(btnAddTask);
        bottomPanel.add(btnLoadWishes);
        bottomPanel.add(btnAddWish);

        getContentPane().add(bottomPanel, BorderLayout.SOUTH);

        setButtonsEnabled(false);

        btnLogin.addActionListener(e -> onLogin());
        btnLoadTasks.addActionListener(e -> onLoadTasks());
        btnCompleteTask.addActionListener(e -> onCompleteTask());
        btnAddTask.addActionListener(e -> onAddTask());
        btnLoadWishes.addActionListener(e -> onLoadWishes());
        btnAddWish.addActionListener(e -> onAddWish());
    }

    private void setButtonsEnabled(boolean enabled) {
        btnLoadTasks.setEnabled(enabled);
        btnCompleteTask.setEnabled(enabled);
        btnAddTask.setEnabled(enabled);
        btnLoadWishes.setEnabled(enabled);
        btnAddWish.setEnabled(enabled);
    }

    private void onLogin() {
        String username = txtUsername.getText().trim();
        String role = (String) cmbRole.getSelectedItem();
        if (username.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Username cannot be empty.");
            return;
        }
        currentUser = new User(username, role);
        lblCurrentUser.setText("Logged in as: " + username + " (" + role + ")");
        setButtonsEnabled(true);
        tableModel.setRowCount(0);
        tableModel.setColumnCount(0);
    }

    private boolean requireRole(String role) {
        if (currentUser == null) {
            JOptionPane.showMessageDialog(this, "Login first.");
            return false;
        }
        if (!currentUser.getRole().equals(role)) {
            JOptionPane.showMessageDialog(this, "This action is only for " + role + ".");
            return false;
        }
        return true;
    }

    private void onLoadTasks() {
        if (!requireRole("CHILD")) return;
        tableModel.setRowCount(0);
        tableModel.setColumnCount(0);
        tableModel.setColumnIdentifiers(new Object[]{
                "ID", "Title", "Description", "Due Date",
                "Points", "Completed", "Rating"
        });
        for (Task t : allTasks) {
            if (t.getChildUsername().equals(currentUser.getUsername())) {
                tableModel.addRow(new Object[]{
                        t.getId(),
                        t.getTitle(),
                        t.getDescription(),
                        t.getDueDate().toString(),
                        t.getBasePoints(),
                        t.isCompleted(),
                        t.getRating()
                });
            }
        }
    }

    private void onCompleteTask() {
        if (!requireRole("CHILD")) return;
        int row = tblMain.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select a task first.");
            return;
        }
        String id = (String) tableModel.getValueAt(row, 0);
        for (Task t : allTasks) {
            if (t.getId().equals(id)) {
                t.setCompleted(true);
                JOptionPane.showMessageDialog(this, "Task marked as completed.");
                onLoadTasks();
                return;
            }
        }
        JOptionPane.showMessageDialog(this, "Task not found.");
    }

    private void onAddTask() {
        if (!requireRole("PARENT") && !requireRole("TEACHER")) return;

        String child = JOptionPane.showInputDialog(this, "Child username:");
        if (child == null || child.trim().isEmpty()) return;
        String title = JOptionPane.showInputDialog(this, "Task title:");
        if (title == null) return;
        String desc = JOptionPane.showInputDialog(this, "Task description:");
        if (desc == null) desc = "";
        String due = JOptionPane.showInputDialog(this, "Due date (YYYY-MM-DD):");
        if (due == null) return;
        String pointsStr = JOptionPane.showInputDialog(this, "Base points:");
        if (pointsStr == null) return;

        try {
            LocalDate date = LocalDate.parse(due);
            int points = Integer.parseInt(pointsStr);
            String id = UUID.randomUUID().toString();
            Task task = new Task(id, title, desc, date, points, false, null, child);
            allTasks.add(task);
            JOptionPane.showMessageDialog(this, "Task added.");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error adding task: " + e.getMessage());
        }
    }

    private void onLoadWishes() {
        if (!requireRole("CHILD")) return;
        tableModel.setRowCount(0);
        tableModel.setColumnCount(0);
        tableModel.setColumnIdentifiers(new Object[]{
                "ID", "Description", "Required Level", "Approved", "Purchased"
        });
        for (Wish w : allWishes) {
            if (w.getChildUsername().equals(currentUser.getUsername())) {
                tableModel.addRow(new Object[]{
                        w.getId(),
                        w.getDescription(),
                        w.getRequiredLevel(),
                        w.isApproved(),
                        w.isPurchased()
                });
            }
        }
    }

    private void onAddWish() {
        if (!requireRole("CHILD")) return;
        String description = JOptionPane.showInputDialog(this, "Wish description:");
        if (description == null) return;
        String reqLevelStr = JOptionPane.showInputDialog(this, "Required level:");
        if (reqLevelStr == null) return;
        try {
            int reqLevel = Integer.parseInt(reqLevelStr);
            String id = UUID.randomUUID().toString();
            Wish wish = new Wish(id, description, reqLevel, false, false,
                    currentUser.getUsername());
            allWishes.add(wish);
            JOptionPane.showMessageDialog(this, "Wish added.");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error adding wish: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            KidTaskStudentA gui = new KidTaskStudentA();
            gui.setVisible(true);
        });
    }
}
