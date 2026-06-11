package se.devsecops.backend.model;

import java.util.List;

public class ListUsersResponse {

    private List<UserResponse> users;

    public ListUsersResponse() {
    }

    public ListUsersResponse(List<UserResponse> users) {
        this.users = users;
    }

    public List<UserResponse> getUsers() {
        return users;
    }

    public void setUsers(List<UserResponse> users) {
        this.users = users;
    }
}
