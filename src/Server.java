import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

public class Server {
    private static final int DEFAULT_PORT = 8888;
    private static final int DEFAULT_PLAYERS_PER_TABLE = 4;
    private static final int DEFAULT_STARTING_MONEY = 12;
    private static final int DEFAULT_MINIMUM_BET = 1;
    private static final int DEFAULT_NUMBER_OF_DECKS = 6;
    private static final int DEFAULT_MINIMUM_CARDS_BEFORE_SHUFFLE = 78;
    private final int serverPort;
    private final int playersPerTable;
    private final int startingStakes;
    private final int minimumWager;
    private final int numberOfDecks;

    /**
     *
     * @param serverPort  The server port
     * @param playersPerTable The number of players required at each table.  Recommended to lower the number for testing.
     * @param startingStakes Number of starting stakes.
     * @param minimumWager Minimum wager.
     * @param numberOfDecks The number of decks that will be put in the deckHolder.
     */

    public Server(int serverPort, int playersPerTable, int startingStakes, int minimumWager, int numberOfDecks) {
        this.serverPort = serverPort;
        this.playersPerTable = playersPerTable;
        this.startingStakes = startingStakes;
        this.minimumWager = minimumWager;
        this.numberOfDecks = numberOfDecks;
    }

    public void start() {
        System.out.println("Starting server\nServer port: " + serverPort + "\nPlayers per table: " + playersPerTable + "\nStarting money: " + startingStakes + "\nMinimum bet: " + minimumWager + "\nNumber of decks: " + numberOfDecks);
        ServerSocket serverSocket = null;
        try {
            System.out.println("Creating server socket");
            serverSocket = new ServerSocket(serverPort);
        } catch (IOException e) {
            System.err.println("Could not start server on port " + serverPort);
            System.exit(1);
        }
        System.out.println("Listening on port " + serverPort);
        int playerCount = 1;
        while (true) {
            //Creates a Game object and adds it to its own thread.
            Game game = new Game(minimumWager, numberOfDecks, startingStakes, playersPerTable);
            Thread gameThread = new Thread(game);
            for (int i = 0; i < playersPerTable; i++) {
                // Allows the specified number of connections.
                try {
                    Socket socket = serverSocket.accept();
                    OutputStream outputStream = socket.getOutputStream();
                    PrintWriter printWriter = new PrintWriter(outputStream, true);
                    printWriter.write(playerCount);
                    System.out.println("Received request from port " + socket.getPort());
                    Player newPlayer = new Player(socket, game, startingStakes, playerCount);
                    game.addPlayer(newPlayer);
                    Thread newPlayerThread = new Thread(newPlayer);
                    newPlayerThread.start();
                    playerCount++;
                } catch (SocketException e) {
                    System.out.println("Player " + playerCount + " disconnected.");
                    game.removePlayerByID(playerCount);
                } catch (IOException e) {
                    System.out.println("IOException on socket.");
                }
            }
            gameThread.start();
        }
    }

    public static void main(String[] args) {
        int serverPort = DEFAULT_PORT;
        int playersPerTable = DEFAULT_PLAYERS_PER_TABLE;
        int startingMoney = DEFAULT_STARTING_MONEY;
        int minimumWager = DEFAULT_MINIMUM_BET;
        int numberOfDecks = DEFAULT_NUMBER_OF_DECKS;
        int minimumCardsBeforeShuffle = DEFAULT_MINIMUM_CARDS_BEFORE_SHUFFLE;

        if (playersPerTable < 2) {
            System.err.println("Number of players per table must be at least 2");
            System.exit(1);
        } else if (startingMoney < minimumWager) {
            System.err.println("Amount of starting money cannot be less than minimum bet");
            System.exit(1);
        } else if (numberOfDecks < 1) {
            System.err.println("Number of decks must be at least 1");
            System.exit(1);
        } else if (minimumCardsBeforeShuffle < 0) {
            System.err.println("Minimum cards before shuffle cannot be less than 0");
            System.exit(1);
        }
        Server server = new Server(serverPort, playersPerTable, startingMoney, minimumWager, numberOfDecks);
        server.start();
    }
}
