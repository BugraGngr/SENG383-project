// KidTask - Student B
// Algorithm + Error Handling (console-based demo)

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

class ChildProfileB {
    private String username;
    private int totalPoints;
    private int level;

    public ChildProfileB(String username, int totalPoints, int level) {
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

class TaskB {
    private String id;
    private String childUsername;
    private int basePoints;
    private boolean completed;
    private Integer rating;
    private LocalDate dueDate;

    public TaskB(String id, String childUsername, int basePoints,
                 boolean completed, Integer rating, LocalDate dueDate) {
        this.id = id;
        this.childUsername = childUsername;
        this.basePoints = basePoints;
        this.completed = completed;
        this.rating = rating;
        this.dueDate = dueDate;
    }

    public String getId() { return id; }
    public String getChildUsername() { return childUsername; }
    public int getBasePoints() { return basePoints; }
    public boolean isCompleted() { return completed; }
    public Integer getRating() { return rating; }
    public LocalDate getDueDate() { return dueDate; }

    public void setCompleted(boolean completed) { this.completed = completed; }
    public void setRating(Integer rating) { this.rating = rating; }
}

class ProfileServiceB {
    private final List<ChildProfileB> profiles = new ArrayList<>();

    public ChildProfileB getOrCreateProfile(String username) {
        for (ChildProfileB p : profiles) {
            if (p.getUsername().equals(username)) {
                return p;
            }
        }
        ChildProfileB p = new ChildProfileB(username, 0, 1);
        profiles.add(p);
        return p;
    }

    public void applyTaskRating(String username, int basePoints, int rating) {
        if (rating < 1 || rating > 5) {
            throw new IllegalArgumentException("Rating must be between 1 and 5.");
        }
        ChildProfileB profile = getOrCreateProfile(username);
        int gained = (int) Math.round(basePoints * (rating / 3.0));
        int newPoints = profile.getTotalPoints() + gained;
        profile.setTotalPoints(newPoints);
        profile.setLevel(calculateLevel(newPoints));
    }

    private int calculateLevel(int totalPoints) {
        if (totalPoints < 100) return 1;
        if (totalPoints < 250) return 2;
        if (totalPoints < 500) return 3;
        if (totalPoints < 800) return 4;
        return 5;
    }
}

class TaskServiceB {
    private final List<TaskB> tasks = new ArrayList<>();
    private final ProfileServiceB profileService;

    public TaskServiceB(ProfileServiceB profileService) {
        this.profileService = profileService;
    }

    public TaskB addTask(String id, String childUsername, int basePoints, LocalDate dueDate) {
        if (basePoints <= 0) {
            throw new IllegalArgumentException("Base points must be positive.");
        }
        TaskB t = new TaskB(id, childUsername, basePoints, false, null, dueDate);
        tasks.add(t);
        return t;
    }

    public void completeTask(TaskB task) {
        task.setCompleted(true);
    }

    public void approveTask(TaskB task, int rating) {
        if (!task.isCompleted()) {
            throw new IllegalStateException("Task must be completed before approval.");
        }
        profileService.applyTaskRating(task.getChildUsername(), task.getBasePoints(), rating);
        task.setRating(rating);
    }

    public List<TaskB> getTasks() { return tasks; }
}

public class KidTaskStudentB {
    public static void main(String[] args) {
        ProfileServiceB profileService = new ProfileServiceB();
        TaskServiceB taskService = new TaskServiceB(profileService);

        // Example usage
        TaskB t1 = taskService.addTask("T1", "alice", 100, LocalDate.now().plusDays(1));
        taskService.completeTask(t1);
        taskService.approveTask(t1, 5); // high rating

        ChildProfileB profile = profileService.getOrCreateProfile("alice");
        System.out.println("Child: " + profile.getUsername());
        System.out.println("Total points: " + profile.getTotalPoints());
        System.out.println("Level: " + profile.getLevel());
    }
}
