package ru.pavlova.java.basic;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Server {
    private int port;
    private List<ClientHandler> clients;
    private AuthenticationService authenticationService;

    private JdbcUsersAuthenticationService jdbcUsersAuthenticationService;

    public AuthenticationService getAuthenticationService() {
        return authenticationService;
    }


    public Server(int port) {
        this.port = port;
        this.clients = new ArrayList<>();
    }

    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            this.jdbcUsersAuthenticationService = new JdbcUsersAuthenticationService();
            System.out.println("Сервис аутентификации запущен: " + jdbcUsersAuthenticationService.getClass().getSimpleName());
            System.out.printf("Сервер запущен на порту: %d, ожидаем подключения клиентов\n", port);
            while (true) {
                try {
                    Socket socket = serverSocket.accept();
                    new ClientHandler(this, socket);
                } catch (Exception e) {
                    System.out.println("Возникла ошибка при обработке подключившегося клиента");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public synchronized void subscribe(ClientHandler clientHandler) {
        broadcastMessage("К чату присоединился " + clientHandler.getNickname());
        clients.add(clientHandler);
    }

    public synchronized void unsubscribe(ClientHandler clientHandler) {
        clients.remove(clientHandler);
        broadcastMessage("Из чата вышел " + clientHandler.getNickname());
    }

    public synchronized void broadcastMessage(String message) {
        for (ClientHandler c : clients) {
            c.sendMessage(message);
        }

    }

    public synchronized void sendPrivateMessage(ClientHandler sender, String recipientName, String message) {
        for (ClientHandler recipient : clients) {
            if (recipient.getUsername().equals(recipientName)) {
                recipient.sendMessage(sender.getUsername() + " (private): " + message);
                sender.sendMessage("Message sent to " + recipientName);
                return;
            }
        }
        sender.sendMessage("Error! User " + recipientName + " not found.");
    }
    public synchronized boolean isNicknameBusy(String nickname) {
        for (ClientHandler c : clients) {
            if (c.getNickname().equals(nickname)) {
                return true;
            }
        }
        return false;
    }
}
