import java.util.Scanner;


public class XOGame {
    private static char[][] board = new char[3][3];
    private static Object lock = new Object();
    private static boolean xTurn = true;
    private static boolean gameOver = false;
    private static String X;
    private static String O;
    private static int p1Score;
    private static int p2Score;
    private static int count;

    public static void main(String[] args){
        Scanner input = new Scanner(System.in);
        String response;

        System.out.println("Welcome to    ✖️⭕️game\n");
        System.out.println("Enter first Player Name:");
        String p1Name = input.next().toUpperCase();
        System.out.println("Enter second Player Name:");
        String p2Name = input.next().toUpperCase();

        System.out.println("The game will chose who will start Fighters!🥷 \n");
        // choose randomly who starts:
        int num = (int) (Math.random()*2);
        if(num == 0){
            // These extra strings is used to make the text bold
            System.out.println("Get ready " + "\033[0;1m" + p1Name + "\033[0;0m" + ", You will start.");
            X = p1Name;
            O = p2Name;
        }
        else{
            System.out.println("Get ready " + "\033[0;1m" + p2Name + "\033[0;0m" + ", You will start.");
            X = p2Name;
            O = p1Name;
        }
        System.out.println("-------------------------------------Let's Start--------------------------------------------");

        do {
            Thread xThread = new Thread(new XPlayer());
            Thread oThread = new Thread(new OPlayer());

            xThread.start();
            oThread.start();

            // Wait for the threads to finish
            try {
                xThread.join();
                oThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            // Print the result of the game
            if (gameOver) {
                count ++;
                if (checkWin('X')) {
                    System.out.println(X + " WINS!\n " + "\033[0;1m" + "Congratulations! 🥇" + "\033[0;0m" +"\n");
                    if(count % 2 == 1)
                        p1Score++;
                    else
                        p2Score++;

                } else if (checkWin('O')) {
                    System.out.println(O + " WINS!\n" + "\033[0;1m" + "Congratulations! 🥇" + "\033[0;0m" +"\n");
                    if(count % 2 == 1)
                        p2Score++;
                    else
                        p1Score++;
                } else {
                    System.out.println("It's a tie!\n");
                }

                System.out.println("\033[0;1m" + "Scores:" + "\033[0;0m");
                System.out.println(X + "\t" + p1Score + "\n" + O + "\t" + p2Score +"\n");
            }
            System.out.println("Do you want to play more? Y|N");
            response = input.next();
            if(response.toLowerCase().equals("y") || response.toLowerCase().equals("yes")){
                xTurn = true;
                gameOver = false;
                board = new char[3][3];
                String temp = X;
                X = O;
                O = temp;
            }
            else{
                if(p1Score == p2Score)
                    System.out.println("YOU BOTH GOT THE SAME SCORES📍");
                else
                    System.out.print("The winner is: " + (p1Score > p2Score ? X : O ) + ", With score: " + (Math.max(p1Score, p2Score)));

            }
        }while(response.toLowerCase().equals("y") || response.toLowerCase().equals("yes"));




    }

    private static void switchTurn(){
        String t = X;
        X = O;
        O = t;
    }

    private static class XPlayer implements Runnable {
        @Override
        public void run() {
            while (!gameOver) {
                synchronized (lock) {
                    while (!xTurn && !gameOver) {
                        try {
                            lock.wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }

                    if (!gameOver) {
                        // Get input from X player
                        int cell = getInput(X);

                        // Place 'X' on the board and check for win/tie
                        if (placePiece('X', cell)) {
                            gameOver = true;
                        }

                        xTurn = false;
                        displayBoard();
                        lock.notify();
                    }
                }
            }
        }
    }

    private static class OPlayer implements Runnable {
        @Override
        public void run() {
            while (!gameOver) {
                synchronized (lock) {
                    while (xTurn && !gameOver) {
                        try {
                            lock.wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }

                    if (!gameOver) {
                        // Get input from O player
                        int cell = getInput(O);

                        // Place 'O' on the board and check for win/tie
                        if (placePiece('O', cell)) {
                            gameOver = true;
                        }

                        xTurn = true;
                        displayBoard();
                        lock.notify();
                    }
                }
            }
        }
    }
    private static int getInput(String player) {
        Scanner input = new Scanner(System.in);
        int cell;
        do {
            System.out.println(player + ", choose a cell (1-9):");
            while (!input.hasNextInt()) {
                System.out.println("Invalid input. " + player + ", choose a cell (1-9):");
                input.next();
            }
            cell = input.nextInt();
            if (cell < 1 || cell > 9) {
                System.out.println("Invalid cell. Choose a cell within the range 1-9.");
            } else if (board[(cell - 1) / 3][(cell - 1) % 3] != '\0') {
                System.out.println("Cell is already occupied. Choose an empty cell.");
            }
        } while (cell < 1 || cell > 9 || board[(cell - 1) / 3][(cell - 1) % 3] != '\0');
        return cell;
    }

    private static boolean placePiece(char piece, int cell) {
        int row = (cell - 1) / 3;
        int col = (cell - 1) % 3;
        board[row][col] = piece;
        // Check for win
        if (checkWin(piece)) {
            return true;
        }

        // Check for tie
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (board[i][j] == '\0') {
                    return false;
                }
            }
        }
        return true;
    }

    // Check if there are winner
    private static boolean checkWin(char piece) {
        // Check rows
        for (int i = 0; i < 3; i++) {
            if (board[i][0] == piece && board[i][1] == piece && board[i][2] == piece) {
                return true;
            }
        }
        // Check columns
        for (int i = 0; i < 3; i++) {
            if (board[0][i] == piece && board[1][i] == piece && board[2][i] == piece) {
                return true;
            }
        }

        // Check diagonals
        if (board[0][0] == piece && board[1][1] == piece && board[2][2] == piece) {
            return true;
        }
        if (board[0][2] == piece && board[1][1] == piece && board[2][0] == piece) {
            return true;
        }

        return false;
    }


    // Displaying the board
    private static void displayBoard() {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                char cell = board[i][j];
                if (cell == '\0') {
                    System.out.print("- ");
                } else {
                    System.out.print(cell + " ");
                }
            }
            System.out.println();
        }
    }
}