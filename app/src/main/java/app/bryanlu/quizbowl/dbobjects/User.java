package app.bryanlu.quizbowl.dbobjects;

/**
 * Created by Bryan Lu on 4/16/2017.
 *
 * Java representation of a user that can be utilized by the database.
 */

public class User {
    private String username;
    private String email;
    private Stats personalStats;

    public static final String USERS = "users";
    public static final String NAME = "username";
    public static final String STATS = "personalStats";

    public User() {}

    public User(String username, String email, Stats personalStats) {
        this.username = username;
        this.email = email;
        this.personalStats = personalStats;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public Stats getPersonalStats() {
        return personalStats;
    }
}
