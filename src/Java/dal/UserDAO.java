package Java.dal;

import Java.be.User;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class UserDAO {

    private static final List<User> USERS = new ArrayList<>();

    static {
        if (USERS.isEmpty()) {
            USERS.add(new User("Admin User", "admin", "1234", "admin@easv.dk", "Admin"));

            USERS.add(new User("Sarah Jensen", "sarah", "1234", "s.jensen@easv.dk", "Event Coordinator"));
            USERS.add(new User("Mikkel Andersen", "mikkel", "1234", "m.andersen@easv.dk", "Event Coordinator"));
            USERS.add(new User("Laura Nielsen", "laura", "1234", "l.nielsen@easv.dk", "Event Coordinator"));
            USERS.add(new User("Peter Christiansen", "peter", "1234", "p.chris@easv.dk", "Event Coordinator"));
        }
    }

    public List<User> getAllUsers() {
        return new ArrayList<>(USERS);
    }

    public List<User> getUsersByRole(String role) {
        return USERS.stream()
                .filter(user -> user.getRole().equalsIgnoreCase(role))
                .collect(Collectors.toList());
    }

    public void addUser(User user) {
        USERS.add(user);
    }

    public void deleteUser(User user) {
        USERS.remove(user);
    }
}