package com.company.task3008.client;


import com.company.task3008.ConsoleHelper;
import com.company.task3008.Message;
import com.company.task3008.MessageType;
import com.company.task3008.Connection;

import java.io.IOException;

public class Client extends Thread{

    protected Connection connection;
    private volatile boolean  clientConnected = false;

    public static void main(String[] args) {
        Client cl = new Client();
        cl.run();
    }

    protected String getServerAddress()
    {
        System.out.println("server address");
        String address = ConsoleHelper.readString();
        return address;
    }

    protected int getServerPort() {
        System.out.println("server port");
        int port = ConsoleHelper.readInt();
        return port;
    }

    protected String getUserName()
    {
        System.out.println("user name");
        String userName = ConsoleHelper.readString();
        return userName;
    }

    protected boolean shouldSendTextFromConsole()
    {
        return true;
    }

    protected SocketThread getSocketThread()
    {
       // clientConnected = true;
        return new SocketThread();
    }

    protected  void sendTextMessage(String text)
    {
        try {
            connection.send(new Message(MessageType.TEXT, text));
        } catch (IOException e) {
            System.out.println("Error. text don,t sent");
            clientConnected = false;
        }
    }

    public void run() {
        SocketThread st = getSocketThread();
        st.setDaemon(true);
        st.start();
        synchronized (this) {
            try {
                this.wait();
            } catch (InterruptedException e) {
                ConsoleHelper.writeMessage("Exit of program");
            }
        }
        if (clientConnected == true) {
            ConsoleHelper.writeMessage("Соединение установлено. Для выхода наберите команду - exit");
        } else ConsoleHelper.writeMessage("Произошла ошибка во время работы клиента.");

        while (clientConnected == true)
        {
            String str = ConsoleHelper.readString();
            if (str.equals("exit"))
            {
                clientConnected = false;
            }
            if (shouldSendTextFromConsole() == true) {
                sendTextMessage(str);
            }
        }

    }


    public class SocketThread extends Thread
    {
        protected void processIncomingMessage(String message)
        {
            ConsoleHelper.writeMessage(message);
        }

        protected void informAboutAddingNewUser(String userName)
        {
            ConsoleHelper.writeMessage(userName + " Присоединился к чату");
        }

        protected void informAboutDeletingNewUser(String userName)
        {
            ConsoleHelper.writeMessage(userName + " Покинул чат");
        }

        protected void notifyConnectionStatusChanged(boolean clientConnected)
        {
            synchronized (Client.this) {
                Client.this.clientConnected = clientConnected;
                Client.this.notify();
            }
        }

        protected void clientHandshake() throws IOException, ClassNotFoundException
        {
            while (!clientConnected)
            {
                Message msg = connection.receive();
                if (msg.getType() == MessageType.NAME_REQUEST)
                {
                    String userName = getUserName();
                    connection.send(new Message(MessageType.USER_NAME, userName));
                } else if (msg.getType() == MessageType.NAME_ACCEPTED)
                {
                    notifyConnectionStatusChanged(true);
                    break;
                } else throw new IOException("Unexpected MessageType");
            }
        }

        protected void clientMainLoop() throws IOException, ClassNotFoundException
        {
            while (true) {
                Message msg = connection.receive();
                if (msg.getType() == MessageType.TEXT) {
                    processIncomingMessage(msg.getData());
                } else if (msg.getType() == MessageType.USER_ADDED) {
                    informAboutAddingNewUser(msg.getData());
                } else if (msg.getType() == MessageType.USER_REMOVED) {
                    informAboutDeletingNewUser(msg.getData());
                } else throw new IOException("Unexpected MessageType");
            }

        }

        public void run()
        {
            String address = getServerAddress();
            int port = getServerPort();
            try {
                java.net.Socket socket = new java.net.Socket(address, port);
                connection = new Connection(socket);
                clientHandshake();
                clientMainLoop();

            } catch (IOException | ClassNotFoundException e) {
                notifyConnectionStatusChanged(false);
                e.printStackTrace();
            }
        }


    }

}
