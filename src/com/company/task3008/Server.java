package com.company.task3008;

import java.io.IOException;
import java.net.Socket;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by i.lapshinov on 06.09.2018.
 */
public class Server {
    private static Map<String, Connection> connectionMap = new ConcurrentHashMap<>();

    public static void main(String[] args) {
        int number = ConsoleHelper.readInt();
        try (java.net.ServerSocket serverSocket = new java.net.ServerSocket(number)) {
            System.out.println("Сервер запущен");
            while(true)
            {
                Socket socket = serverSocket.accept();
                new Handler(socket).start();
            }
        } catch (IOException e) {
            System.out.println("Something wrong, Server socket closed.");
        }
    }

    public static void sendBroadcastMessage(Message message)
    {
        Iterator<Map.Entry<String, Connection>> it = connectionMap.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, Connection> pair = it.next();
            try {
                pair.getValue().send(message);
            } catch (IOException e) {
                System.out.println("Message don't send");
            }
        }
    }



    private static class Handler extends Thread{
        private Socket socket;

        public Handler(Socket socket) {
            this.socket = socket;
        }

        private String serverHandshake(Connection connection) throws IOException, ClassNotFoundException {
            while (true) {
                connection.send(new Message(MessageType.NAME_REQUEST));
            Message answer = connection.receive();
            if (answer.getType() == (MessageType.USER_NAME)) {
                if (!(answer.getData().isEmpty())) {
                    if (!(connectionMap.containsKey(answer.getData()))) {
                        connectionMap.put(answer.getData(), connection);
                        connection.send(new Message(MessageType.NAME_ACCEPTED));
                        return answer.getData();
                    }
                }

            }

            }

        }

        private void sendListOfUsers(Connection connection, String userName) throws IOException
        {
            Iterator<Map.Entry<String, Connection>> it = connectionMap.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<String, Connection> pair = it.next();
                if (!(pair.getKey() == userName))
                connection.send(new Message(MessageType.USER_ADDED, pair.getKey()));
            }
        }

        private void serverMainLoop(Connection connection, String userName) throws IOException, ClassNotFoundException
        {
            while (true)
            {
                Message message = connection.receive();
                if (message !=null && message.getType() == MessageType.TEXT) {
                    sendBroadcastMessage(new Message(MessageType.TEXT,userName + ": " + message.getData()));
                } else ConsoleHelper.writeMessage("Error!");
            }
        }

        public void run()
        {
            ConsoleHelper.writeMessage("установлено новое соединение с " + socket.getRemoteSocketAddress());
            String userName = null;
            try {
                Connection connection = new Connection(socket);
                userName = serverHandshake(connection);
                sendBroadcastMessage(new Message(MessageType.USER_ADDED, userName));
                sendListOfUsers(connection, userName);
                serverMainLoop(connection, userName);

            } catch (IOException  | ClassNotFoundException e) {
                ConsoleHelper.writeMessage("error. an exchange of data error to remote socket address");
            }
            finally {
                if (userName!= null) {
                    connectionMap.remove(userName);
                    sendBroadcastMessage(new Message(MessageType.USER_REMOVED, userName));
                }
            }
            ConsoleHelper.writeMessage("Server is running");
        }


    }




}
