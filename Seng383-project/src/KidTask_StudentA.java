// KidTask - Student A (FINAL v3)
// GUI + Data Structures (Java Swing) + Persistence (Tasks.txt, Wishes.txt)
// Fix v3: Points & progress bar now correctly update based on approved+rated tasks.
//
// Run: Right click KidTask_StudentA.java -> Run As -> Java Application
// Files created/used in project folder: Tasks.txt, Wishes.txt
package KidTask;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

class User {
    private final String username;
    private final String role; // CHILD, PARENT, TEACHER
    public User(String username, String role) {
        this.username = username;
        this.role = role;
    }
    public String getUsername() { return username; }
    public String getRole() { return role; }
}

class Task {
    private final String id;
    private final String title;
    private final String description;
    private final LocalDate dueDate;
    private final int basePoints;
    private boolean completed;
    private Integer rating; // null until approved
    private final String childUsername;

    public Task(String id, String title, String description, LocalDate dueDate,
                int basePoints, boolean completed, Integer rating, String childUsername) {
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
    private final String id;
    private final String description;
    private final int requiredLevel;
    private boolean approved;
    private boolean purchased;
    private final String childUsername;

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

public class KidTask_StudentA extends JFrame {

    // Persistence files (created in project folder)
    private static final String TASKS_FILE = "Tasks.txt";
    private static final String WISHES_FILE = "Wishes.txt";

    // UI
    private JTextField txtUsername;
    private JComboBox<String> cmbRole;
    private JLabel lblCurrentUser;

    private JProgressBar progress;
    private JLabel lblPoints;
    private JLabel lblLevel;

    private JTable tblMain;
    private DefaultTableModel tableModel;

    // Buttons
    private JButton btnLoadTasks;
    private JButton btnCompleteTask;
    private JButton btnAddTask;
    private JButton btnApproveTask;

    private JButton btnLoadWishes;
    private JButton btnAddWish;
    private JButton btnApproveWish;

    // State
    private User currentUser = null;
    private final List<Task> allTasks = new ArrayList<>();
    private final List<Wish> allWishes = new ArrayList<>();

    public KidTask_StudentA() {
        setTitle("KidTask - Task & Wish Management");
        setSize(1100, 650);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        initComponents();
        loadDataFromFiles();
        refreshHeader();
    }

    // ---------------- UI build ----------------

    private void initComponents() {
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.add(new JLabel("Username:"));
        txtUsername = new JTextField(12);
        top.add(txtUsername);

        top.add(new JLabel("Role:"));
        cmbRole = new JComboBox<>(new String[]{"CHILD", "PARENT", "TEACHER"});
        top.add(cmbRole);

        JButton btnLogin = new JButton("Login");
        btnLogin.addActionListener(e -> onLogin());
        top.add(btnLogin);

        lblCurrentUser = new JLabel("Not logged in");
        top.add(lblCurrentUser);

        // Progress panel
        JPanel progPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        lblPoints = new JLabel("Points: 0");
        lblLevel = new JLabel("Level: 1");
        progress = new JProgressBar(0, 800);
        progress.setValue(0);
        progress.setStringPainted(true);
        progPanel.add(lblPoints);
        progPanel.add(Box.createHorizontalStrut(15));
        progPanel.add(lblLevel);
        progPanel.add(Box.createHorizontalStrut(15));
        progPanel.add(progress);

        // Table
        tableModel = new DefaultTableModel();
        tblMain = new JTable(tableModel);
        JScrollPane scroll = new JScrollPane(tblMain);

        // Buttons
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT));

        btnLoadTasks = new JButton("Load My Tasks");
        btnLoadTasks.addActionListener(e -> showMyTasks());
        buttons.add(btnLoadTasks);

        btnCompleteTask = new JButton("Complete Task");
        btnCompleteTask.addActionListener(e -> onCompleteTask());
        buttons.add(btnCompleteTask);

        btnAddTask = new JButton("Add Task (Parent/Teacher)");
        btnAddTask.addActionListener(e -> onAddTask());
        buttons.add(btnAddTask);

        btnApproveTask = new JButton("Approve & Rate (Parent/Teacher)");
        btnApproveTask.addActionListener(e -> onApproveTask());
        buttons.add(btnApproveTask);

        buttons.add(Box.createHorizontalStrut(25));

        btnLoadWishes = new JButton("Load My Wishes");
        btnLoadWishes.addActionListener(e -> showMyWishes());
        buttons.add(btnLoadWishes);

        btnAddWish = new JButton("Add Wish (Child)");
        btnAddWish.addActionListener(e -> onAddWish());
        buttons.add(btnAddWish);

        btnApproveWish = new JButton("Approve Wish (Parent)");
        btnApproveWish.addActionListener(e -> onApproveWish());
        buttons.add(btnApproveWish);

        JButton btnSave = new JButton("Save");
        btnSave.addActionListener(e -> saveDataToFiles());
        buttons.add(Box.createHorizontalStrut(25));
        buttons.add(btnSave);

        // Layout
        setLayout(new BorderLayout());
        add(top, BorderLayout.NORTH);

        JPanel center = new JPanel(new BorderLayout());
        center.add(progPanel, BorderLayout.NORTH);
        center.add(scroll, BorderLayout.CENTER);
        add(center, BorderLayout.CENTER);

        add(buttons, BorderLayout.SOUTH);
    }

    // ---------------- role helpers ----------------

    private boolean requireRole(String role) {
        if (currentUser == null) {
            JOptionPane.showMessageDialog(this, "Login first.");
            return false;
        }
        if (!currentUser.getRole().equals(role)) {
            JOptionPane.showMessageDialog(this, "This action is only for: " + role);
            return false;
        }
        return true;
    }

    private boolean requireAnyRole(String... roles) {
        if (currentUser == null) {
            JOptionPane.showMessageDialog(this, "Login first.");
            return false;
        }
        for (String r : roles) {
            if (currentUser.getRole().equals(r)) return true;
        }
        JOptionPane.showMessageDialog(this, "This action is only for: " + String.join(", ", roles));
        return false;
    }

    // ---------------- points/level logic ----------------

    // Rule: gainedPoints = basePoints * (rating / 3.0)
    // rating 1..5
    private int computeTotalPoints(String childUsername) {
        int total = 0;
        for (Task t : allTasks) {
            if (!t.getChildUsername().equals(childUsername)) continue;
            if (t.getRating() == null) continue; // not approved yet
            double mult = t.getRating() / 3.0;
            int gained = (int) Math.round(t.getBasePoints() * mult);
            total += gained;
        }
        return total;
    }

    private int computeLevel(int points) {
        if (points < 100) return 1;
        if (points < 250) return 2;
        if (points < 500) return 3;
        if (points < 800) return 4;
        return 5;
    }

    private void refreshHeader() {
        if (currentUser == null) {
            lblCurrentUser.setText("Not logged in");
            lblPoints.setText("Points: 0");
            lblLevel.setText("Level: 1");
            progress.setValue(0);
            progress.setString("0 / 800");
            return;
        }

        lblCurrentUser.setText("Logged in: " + currentUser.getUsername() + " (" + currentUser.getRole() + ")");

        if (currentUser.getRole().equals("CHILD")) {
            int total = computeTotalPoints(currentUser.getUsername());
            int level = computeLevel(total);
            lblPoints.setText("Points: " + total);
            lblLevel.setText("Level: " + level);
            progress.setValue(Math.min(total, 800));
            progress.setString(total + " / 800");
        } else {
            lblPoints.setText("Points: -");
            lblLevel.setText("Level: -");
            progress.setValue(0);
            progress.setString("");
        }
    }

    // ---------------- actions ----------------

    private void onLogin() {
        String username = txtUsername.getText().trim();
        String role = (String) cmbRole.getSelectedItem();
        if (username.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Username cannot be empty.");
            return;
        }
        currentUser = new User(username, role);
        refreshHeader();
        JOptionPane.showMessageDialog(this, "Welcome " + username + " (" + role + ")");
    }

    private void showMyTasks() {
        if (currentUser == null) {
            JOptionPane.showMessageDialog(this, "Login first.");
            return;
        }
        tableModel.setColumnIdentifiers(new Object[]{"ID", "Title", "Due Date", "Points", "Completed", "Rating"});
        tableModel.setRowCount(0);

        for (Task t : allTasks) {
            if (t.getChildUsername().equals(currentUser.getUsername())) {
                tableModel.addRow(new Object[]{
                        t.getId(),
                        t.getTitle(),
                        t.getDueDate(),
                        t.getBasePoints(),
                        t.isCompleted(),
                        t.getRating() == null ? "" : t.getRating()
                });
            }
        }
        refreshHeader();
    }

    private void onAddTask() {
        if (!requireAnyRole("PARENT", "TEACHER")) return;

        String childUsername = JOptionPane.showInputDialog(this, "Child username:");
        if (childUsername == null) return;
        childUsername = childUsername.trim();
        if (childUsername.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Child username cannot be empty.");
            return;
        }

        String title = JOptionPane.showInputDialog(this, "Task title:");
        if (title == null) return;

        String desc = JOptionPane.showInputDialog(this, "Task description:");
        if (desc == null) return;

        String dueStr = JOptionPane.showInputDialog(this, "Due date (YYYY-MM-DD):");
        if (dueStr == null) return;

        String pointsStr = JOptionPane.showInputDialog(this, "Base points:");
        if (pointsStr == null) return;

        try {
            LocalDate due = LocalDate.parse(dueStr.trim());
            int points = Integer.parseInt(pointsStr.trim());
            if (points <= 0) {
                JOptionPane.showMessageDialog(this, "Base points must be positive.");
                return;
            }

            String id = UUID.randomUUID().toString();
            Task t = new Task(id, title, desc, due, points, false, null, childUsername);
            allTasks.add(t);
            saveDataToFiles();
            JOptionPane.showMessageDialog(this, "Task added.");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error adding task: " + e.getMessage());
        }
    }

    private void onCompleteTask() {
        if (!requireRole("CHILD")) return;

        int row = tblMain.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select a task row first.");
            return;
        }

        String taskId = String.valueOf(tableModel.getValueAt(row, 0));
        Task t = findTaskById(taskId);
        if (t == null) {
            JOptionPane.showMessageDialog(this, "Task not found.");
            return;
        }
        if (!t.getChildUsername().equals(currentUser.getUsername())) {
            JOptionPane.showMessageDialog(this, "You can only complete your own tasks.");
            return;
        }

        t.setCompleted(true);
        saveDataToFiles();
        JOptionPane.showMessageDialog(this, "Task marked completed. Wait for approval.");
        showMyTasks();
    }

    private void onApproveTask() {
        if (!requireAnyRole("PARENT", "TEACHER")) return;

        int row = tblMain.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select a task row first.");
            return;
        }

        String taskId = String.valueOf(tableModel.getValueAt(row, 0));
        Task t = findTaskById(taskId);
        if (t == null) {
            JOptionPane.showMessageDialog(this, "Task not found.");
            return;
        }
        if (!t.isCompleted()) {
            JOptionPane.showMessageDialog(this, "Task must be completed before approval.");
            return;
        }

        String ratingStr = JOptionPane.showInputDialog(this, "Rating (1-5):");
        if (ratingStr == null) return;

        try {
            int rating = Integer.parseInt(ratingStr.trim());
            if (rating < 1 || rating > 5) {
                JOptionPane.showMessageDialog(this, "Rating must be between 1 and 5.");
                return;
            }
            t.setRating(rating);
            saveDataToFiles();
            JOptionPane.showMessageDialog(this, "Task approved with rating " + rating + ".");
            // If a child is currently logged in, update UI immediately
            refreshHeader();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error approving task: " + e.getMessage());
        }
    }

    private void showMyWishes() {
        if (currentUser == null) {
            JOptionPane.showMessageDialog(this, "Login first.");
            return;
        }
        tableModel.setColumnIdentifiers(new Object[]{"ID", "Wish", "Required Level", "Approved", "Purchased"});
        tableModel.setRowCount(0);

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
        refreshHeader();
    }

    private void onAddWish() {
        if (!requireRole("CHILD")) return;

        String desc = JOptionPane.showInputDialog(this, "Wish description:");
        if (desc == null) return;

        String reqLevelStr = JOptionPane.showInputDialog(this, "Required level:");
        if (reqLevelStr == null) return;

        try {
            int reqLevel = Integer.parseInt(reqLevelStr.trim());
            if (reqLevel < 1) {
                JOptionPane.showMessageDialog(this, "Required level must be at least 1.");
                return;
            }

            String id = UUID.randomUUID().toString();
            Wish w = new Wish(id, desc, reqLevel, false, false, currentUser.getUsername());
            allWishes.add(w);
            saveDataToFiles();
            JOptionPane.showMessageDialog(this, "Wish added.");
            showMyWishes();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error adding wish: " + e.getMessage());
        }
    }

    private void onApproveWish() {
        if (!requireRole("PARENT")) return;

        int row = tblMain.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select a wish row first.");
            return;
        }

        String wishId = String.valueOf(tableModel.getValueAt(row, 0));
        Wish w = findWishById(wishId);
        if (w == null) {
            JOptionPane.showMessageDialog(this, "Wish not found.");
            return;
        }

        w.setApproved(true);
        saveDataToFiles();
        JOptionPane.showMessageDialog(this, "Wish approved.");
        showMyWishes();
    }

    private Task findTaskById(String id) {
        for (Task t : allTasks) if (t.getId().equals(id)) return t;
        return null;
    }

    private Wish findWishById(String id) {
        for (Wish w : allWishes) if (w.getId().equals(id)) return w;
        return null;
    }

    // ---------------- persistence ----------------

    // Escape for pipe-delimited format
    private static String esc(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\n", "\\n").replace("|", "\\p");
    }
    private static String unesc(String s) {
        if (s == null) return "";
        return s.replace("\\p", "|").replace("\\n", "\n").replace("\\\\", "\\");
    }

    private void loadDataFromFiles() {
        loadTasks();
        loadWishes();
    }

    private void saveDataToFiles() {
        saveTasks();
        saveWishes();
    }

    private void loadTasks() {
        Path p = Paths.get(TASKS_FILE);
        if (!Files.exists(p)) return;

        try {
            List<String> lines = Files.readAllLines(p);
            allTasks.clear();
            for (String line : lines) {
                if (line.trim().isEmpty()) continue;
                // id|title|desc|dueDate|basePoints|completed|rating|childUsername
                String[] parts = line.split("\\|", -1);
                if (parts.length < 8) continue;

                String id = unesc(parts[0]);
                String title = unesc(parts[1]);
                String desc = unesc(parts[2]);
                LocalDate due = LocalDate.parse(parts[3]);
                int base = Integer.parseInt(parts[4]);
                boolean completed = Boolean.parseBoolean(parts[5]);
                String ratingStr = parts[6];
                Integer rating = (ratingStr == null || ratingStr.isEmpty()) ? null : Integer.parseInt(ratingStr);
                String child = unesc(parts[7]);

                Task t = new Task(id, title, desc, due, base, completed, rating, child);
                allTasks.add(t);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Failed to load Tasks.txt: " + e.getMessage());
        }
    }

    private void saveTasks() {
        Path p = Paths.get(TASKS_FILE);
        List<String> out = new ArrayList<>();
        for (Task t : allTasks) {
            String ratingStr = (t.getRating() == null) ? "" : String.valueOf(t.getRating());
            out.add(String.join("|",
                    esc(t.getId()),
                    esc(t.getTitle()),
                    esc(t.getDescription()),
                    t.getDueDate().toString(),
                    String.valueOf(t.getBasePoints()),
                    String.valueOf(t.isCompleted()),
                    ratingStr,
                    esc(t.getChildUsername())
            ));
        }
        try {
            Files.write(p, out);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Failed to save Tasks.txt: " + e.getMessage());
        }
    }

    private void loadWishes() {
        Path p = Paths.get(WISHES_FILE);
        if (!Files.exists(p)) return;

        try {
            List<String> lines = Files.readAllLines(p);
            allWishes.clear();
            for (String line : lines) {
                if (line.trim().isEmpty()) continue;
                // id|desc|requiredLevel|approved|purchased|childUsername
                String[] parts = line.split("\\|", -1);
                if (parts.length < 6) continue;

                String id = unesc(parts[0]);
                String desc = unesc(parts[1]);
                int req = Integer.parseInt(parts[2]);
                boolean approved = Boolean.parseBoolean(parts[3]);
                boolean purchased = Boolean.parseBoolean(parts[4]);
                String child = unesc(parts[5]);

                Wish w = new Wish(id, desc, req, approved, purchased, child);
                allWishes.add(w);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Failed to load Wishes.txt: " + e.getMessage());
        }
    }

    private void saveWishes() {
        Path p = Paths.get(WISHES_FILE);
        List<String> out = new ArrayList<>();
        for (Wish w : allWishes) {
            out.add(String.join("|",
                    esc(w.getId()),
                    esc(w.getDescription()),
                    String.valueOf(w.getRequiredLevel()),
                    String.valueOf(w.isApproved()),
                    String.valueOf(w.isPurchased()),
                    esc(w.getChildUsername())
            ));
        }
        try {
            Files.write(p, out);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Failed to save Wishes.txt: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new KidTask_StudentA().setVisible(true));
    }
}
