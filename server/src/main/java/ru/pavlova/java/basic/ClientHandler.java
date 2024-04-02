package ru.pavlova.java.basic;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ClientHandler {
    private Server server;
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;
    private String nickname;

    public String getNickname() {
        return nickname;
    }

    public ClientHandler(Server server, Socket socket) throws IOException {
        this.server = server;
        this.socket = socket;
        this.in = new DataInputStream(socket.getInputStream());
        this.out = new DataOutputStream(socket.getOutputStream());
        sendMessage("Добро пожаловать! Для регистрации введите '/register login password nickname role'");
        new Thread(() -> {
            try {
                if (tryToAuthenticate()) {
                    communicate();
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                disconnect();
            }
        }).start();
    }

    private boolean tryToAuthenticate() throws IOException {
        while (true) {
            String msg = in.readUTF();
            if (msg.startsWith("/auth ")) {
                String[] tokens = msg.split(" ");
                if (tokens.length != 3) {
                    sendMessage("Некорректный формат запроса");
                    continue;
                }
                String login = tokens[1];
                String password = tokens[2];
                String nickname = server.getAuthenticationService().getNicknameByLoginAndPassword(login, password);
                if (nickname == null) {
                    sendMessage("Неправильный логин/пароль");
                    continue;
                }
                if (server.isNicknameBusy(nickname)) {
                    sendMessage("Указанная учетная запись уже занята. Попробуйте зайти позднее");
                    continue;
                }
                this.nickname = nickname;
                server.subscribe(this);
                sendMessage(nickname + ", добро пожаловать в чат!");

                if (server.getAuthenticationService().getUserRole(nickname).equals("ADMIN")) {
                    if (msg.startsWith("/kick ")) {
                        String[] kickTokens = msg.split(" ");
                        if (kickTokens.length == 2) {
                            String userToKick = kickTokens[1];
                            server.kickUser(userToKick);
                            sendMessage("Пользователь " + userToKick + " был отключен от чата.");
                            continue;
                        } else {
                            sendMessage("Некорректный формат команды для отключения пользователя.");
                            continue;
                        }
                    }
                }
                return true;
            } else if (msg.startsWith("/register ")) {
                String[] tokens = msg.split(" ");
                if (tokens.length != 5) {
                    sendMessage("Некорректный формат запроса");
                    continue;
                }
                String login = tokens[1];
                String password = tokens[2];
                String nickname = tokens[3];
                String role = tokens[4];

                if (server.getAuthenticationService().isLoginAlreadyExist(login)) {
                    sendMessage("Указанный логин уже занят");
                    continue;
                }

                if (server.getAuthenticationService().isNicknameAlreadyExist(nickname)) {
                    sendMessage("Указанный никнейм уже занят");
                    continue;
                }

                boolean registered = server.getAuthenticationService().register(login, password, nickname, role);
                if (registered) {
                    sendMessage("Регистрация прошла успешно. Авторизуйтесь для входа в чат.");
                } else {
                    sendMessage("Не удалось зарегистрировать пользователя. Попробуйте еще раз.");
                }
            } else {
                sendMessage("Вам необходимо авторизоваться");
            }
        }
    }

    private void communicate() throws IOException {
        while (true) {
            String msg = in.readUTF();
            if (msg.startsWith("/")) {
                if (msg.startsWith("/exit")) {
                    break;
                } else if (msg.startsWith("/w ")) {
                    String[] parts = msg.split(" ", 3);
                    if (parts.length == 3) {
                        String recipient = parts[1];
                        String message = parts[2];
                        server.sendPrivateMessage(this, recipient, message);
                    } else {
                        sendMessage("Неправильный формат личного сообщения. Используйте /w <ник> <сообщение>");
                    }
                }
                continue;
            }
            server.broadcastMessage(nickname + ": " + msg);
        }
    }

    public void sendMessage(String msg) {
        try {
            out.writeUTF(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void disconnect() {
        server.unsubscribe(this);
        try {
            if (in != null) {
                in.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            if (out != null) {
                out.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getUsername() {
        return nickname;
    }
}

