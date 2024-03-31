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

    public void setUsername(String username) {
        this.nickname = username;
    }

    public String getNickname() {
        return nickname;
    }

    public ClientHandler(Server server, Socket socket) throws IOException {
        this.server = server;
        this.socket = socket;
        this.in = new DataInputStream(socket.getInputStream());
        this.out = new DataOutputStream(socket.getOutputStream());
        sendMessage("Введите имя пользователя:");
        String inputName = in.readUTF();
        setUsername(inputName);
        new Thread(() -> {
            try {
                System.out.println("Подключился новый клиент " + nickname);
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

    private void communicate() throws IOException {
        while (true) {
            String msg = in.readUTF();
            if (msg.startsWith("/")) {
                if (msg.startsWith("/exit")) {
                    break;
                }
                else if (msg.startsWith("/w ")) {
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
                return true;
            } else if (msg.startsWith("/register ")) {
                // /register login pass nickname
                String[] tokens = msg.split(" ");
                if (tokens.length != 4) {
                    sendMessage("Некорректный формат запроса");
                    continue;
                }
                String login = tokens[1];
                String password = tokens[2];
                String nickname = tokens[3];
                if (server.getAuthenticationService().isLoginAlreadyExist(login)) {
                    sendMessage("Указанный логин уже занят");
                    continue;
                }
                if (server.getAuthenticationService().isNicknameAlreadyExist(nickname)) {
                    sendMessage("Указанный никнейм уже занят");
                    continue;
                }
                if (!server.getAuthenticationService().register(login, password, nickname)) {
                    sendMessage("Не удалось пройти регистрацию");
                    continue;
                }
                this.nickname = nickname;
                server.subscribe(this);
                sendMessage("Вы успешно зарегистрировались! " + nickname + ", добро пожаловать в чат!");
                return true;
            } else if (msg.equals("/exit")) {
                return false;
            } else {
                sendMessage("Вам необходимо авторизоваться");
            }
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

