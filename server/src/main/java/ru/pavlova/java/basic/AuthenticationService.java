package ru.pavlova.java.basic;

public interface AuthenticationService {
    String[] getUserInfoByLoginAndPassword(String login, String password);

    String getNicknameByLoginAndPassword(String login, String password);

    String getUserRole(String nickname);

    boolean register(String login, String password, String nickname, String role);

    boolean isLoginAlreadyExist(String login);

    boolean isNicknameAlreadyExist(String nickname);

}
