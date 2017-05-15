package server.spring.data.model;

/**
 * @author Ilya Ivanov
 */
public enum Role {
    READER(Role.READ),
    WRITER(READER.getPermissions() | Role.WRITE),
    EDITOR(WRITER.getPermissions() | Role.EDIT),
    ADMIN(EDITOR.getPermissions() | Role.ADMINISTRATE);

    private static final int READ = 0x01;
    private static final int WRITE = 0x04;
    private static final int EDIT = 0x10;
    private static final int ADMINISTRATE = 0x40;

    private int permissions;

    Role(int permissions) {
        this.permissions = permissions;
    }

    private int getPermissions() {
        return permissions;
    }

    public boolean hasReadPermission() {
        return (permissions & READ) != 0;
    }

    public boolean hasWritePermission() {
        return (permissions & WRITE) != 0;
    }

    public boolean hasEditPermission() {
        return (permissions & EDIT) != 0;
    }

    public boolean hasAdminPermission() {
        return (permissions & ADMINISTRATE) != 0;
    }

    public boolean hasPermission(Role role) {
        return permissions >= role.getPermissions();
    }
}
