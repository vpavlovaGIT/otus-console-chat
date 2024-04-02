package ru.pavlova.java.basic;

import java.util.ArrayList;
import java.util.List;

public class InMemoryAuthenticationService implements AuthenticationService {
    private class User {
        private String login;
        private String password;
        private String nickname;
        private String role;

        public User(String login, String password, String nickname, String role) {
            this.login = login;
            this.password = password;
            this.nickname = nickname;
            this.role = role;
        }
        public String getRole() {
            return role;
        }
    }

    private List<User> users;

    public InMemoryAuthenticationService() {
        this.users = new ArrayList<>();
        for (int i = 1; i <= 10; i++) {
            this.users.add(new User("login" + i, "pass" + i, "nick" + i, "USER"));
        }
    }

    @Override
    public String[] getUserInfoByLoginAndPassword(String login, String password) {
        for (User u : users) {
            if (u.login.equals(login) && u.password.equals(password)) {
                String[] userInfo = {u.nickname, u.role}; // Возвращаем имя и роль пользователя
                return userInfo;
            }
        }
        return null;
    }
    @Override
    public String getNicknameByLoginAndPassword(String login, String password) {
        for (User u : users) {
            if (u.login.equals(login) && u.password.equals(password)) {
                return u.nickname; // Возвращаем никнейм пользователя
            }
        }
        return null; // Если пользователь не найден, возвращаем null
    }

    @Override
    public String getUserRole(String nickname) {
        for (User user : users) {
            if (user.nickname.equals(nickname)) {
                return user.getRole();
            }
        }
        return null;
    }
    @Override
    public boolean register(String login, String password, String nickname, String role) {
        if (isLoginAlreadyExist(login)) {
            return false;
        }
        if (isNicknameAlreadyExist(nickname)) {
            return false;
        }
        User newUser = new User(login, password, nickname, "USER");
        users.add(newUser);
        return true;
    }

    @Override
    public boolean isLoginAlreadyExist(String login) {
        for (User u : users) {
            if (u.login.equals(login)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isNicknameAlreadyExist(String nickname) {
        for (User u : users) {
            if (u.nickname.equals(nickname)) {
                return true;
            }
        }
        return false;
    }
}
