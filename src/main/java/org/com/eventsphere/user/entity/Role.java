package org.com.eventsphere.user.entity;

public enum Role {
    USER("Regular User"),
    ORGANIZER("Event Organizer"),
    ADMIN("System Administrator");

    private final String displayName;

    Role(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean hasAdminPrivileges() {
        return this == ADMIN;
    }

    public boolean canCreateEvents() {
        return this == ORGANIZER || this == ADMIN;
    }

    public boolean canManageUsers() {
        return this == ADMIN;
    }
}