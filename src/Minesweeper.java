import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Random;
import javax.swing.*;
import java.io.File;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;

public class Minesweeper {
    private static class MineTile extends JButton {
        int r;
        int c;

        public MineTile(int r, int c) {
            this.r = r;
            this.c = c;
        }
    }

    int tileSize = 70;
    int numRows = 8;
    int numCols = numRows;
    int boardWidth = numCols * tileSize;
    int boardHeight = numRows * tileSize;

    JFrame frame = new JFrame("Minesweeper");
    JLabel textLabel = new JLabel();
    JPanel textPanel = new JPanel();
    JPanel boardPanel = new JPanel();

    int mineCount = 10;
    MineTile[][] board = new MineTile[numRows][numCols];
    ArrayList<MineTile> mineList;
    Random random = new Random();

    int tilesClicked = 0;
    boolean gameOver = false;

    int gameTime = 0;
    Timer timer;

    int lastGameTime = 0;

    Minesweeper() {
        frame.setVisible(true);
        frame.setSize(boardWidth, boardHeight);
        frame.setLocationRelativeTo(null);
        frame.setResizable(false);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        textLabel.setFont(new Font("Arial", Font.BOLD, 25));
        textLabel.setHorizontalAlignment(JLabel.CENTER);
        textLabel.setText("Minesweeper: " + mineCount + "     (Click to Start)");
        textLabel.setOpaque(true);

        textPanel.setLayout(new BorderLayout());
        textPanel.add(textLabel);
        frame.add(textPanel, BorderLayout.NORTH);

        boardPanel.setLayout(new GridLayout(numRows, numCols));
        frame.add(boardPanel);
        
        for (int r = 0; r < numRows; r++) {
            for (int c = 0; c < numCols; c++) {
                MineTile tile = new MineTile(r, c);
                board[r][c] = tile;

                tile.setFocusable(false);
                tile.setMargin(new Insets(0, 0, 0, 0));
                tile.setFont(new Font("Arial Unicode MS", Font.PLAIN, 45));
                tile.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mousePressed(MouseEvent e) {
                        if (gameOver) {
                            return;
                        }
                        MineTile tile = (MineTile) e.getSource();

                        //left click
                        if (e.getButton() == MouseEvent.BUTTON1) {
                            if (Objects.equals(tile.getText(), "")) {
                                if (mineList.contains(tile)) {
                                    revealMines();
                                }
                                else {
                                    checkMine(tile.r, tile.c);
                                }
                            }
                        }

                        //right click
                        else if (e.getButton() == MouseEvent.BUTTON3) {
                            if (Objects.equals(tile.getText(), "") && tile.isEnabled()) {
                                tile.setText("🚩");
                            }
                            else if (Objects.equals(tile.getText(), "🚩")) {
                                tile.setText("?");
                            }
                            else if (Objects.equals(tile.getText(), "?")) {
                                tile.setText("");
                            }
                        }
                    }
                });
                boardPanel.add(tile);
            }
        }

        timer = new Timer(1000, e -> {
            gameTime++;
            updateTimerLabel();
        });

        frame.setVisible(true);
        setMines();
    }

    void setMines() {
        mineList = new ArrayList<>();

        int mineLeft = mineCount;
        while (mineLeft > 0) {
            int r = random.nextInt(numRows); //0-7
            int c = random.nextInt(numCols);

            MineTile tile = board[r][c];
            if (!mineList.contains(tile)) {
                mineList.add(tile);
                mineLeft -= 1;
            }
        }
    }

    void revealMines() {
        timer.stop();

        if (!gameOver) {
            lastGameTime = gameTime;
        }

        for (MineTile tile : mineList) {
            tile.setText("💣");
        }

        try {
            File file = new File("./audio/explosion.wav");
            AudioInputStream stream = AudioSystem.getAudioInputStream(file);
            Clip clip = AudioSystem.getClip();
            clip.open(stream);
            clip.start();

            // sleep to allow enough time for the clip to play
            Thread.sleep(500);

            stream.close();
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }

        gameOver = true;
        textLabel.setText("Game Over!   Last Time: " + lastGameTime + "s");
    }

    void checkMine(int r, int c) {
        updateTimerLabel();

        if (!timer.isRunning()) {
            timer.start();
        }

        if (r < 0 || r >= numRows || c < 0 || c >= numCols) {
            return;
        }

        MineTile tile = board[r][c];
        if (!tile.isEnabled()) {
            return;
        }
        tile.setEnabled(false);
        tilesClicked += 1;

        int minesFound = 0;
        
        //top 3
        minesFound += countMine(r-1, c-1); //top left
        minesFound += countMine(r-1, c); //top
        minesFound += countMine(r-1, c+1); //top right

        //left and right
        minesFound += countMine(r, c-1); //left
        minesFound += countMine(r, c+1); //right

        //bottom 3
        minesFound += countMine(r+1, c-1); //bottom left
        minesFound += countMine(r+1, c); //bottom
        minesFound += countMine(r+1, c+1); //bottom right

        if (minesFound > 0) {
            tile.setText(Integer.toString(minesFound));
        }
        else {
            tile.setText("");

            //top 3
            checkMine(r-1, c-1); //top left
            checkMine(r-1, c); //top
            checkMine(r-1, c+1); //top right

            //left and right
            checkMine(r, c-1); //left
            checkMine(r, c+1); //right

            //bottom 3
            checkMine(r+1, c-1); //bottom left
            checkMine(r+1, c); //bottom
            checkMine(r+1, c+1); //bottom right
        }

        if (tilesClicked == numRows * numCols - mineList.size()) {
            timer.stop();
            lastGameTime = gameTime;
            gameOver = true;
            textLabel.setText("Mines Cleared!   Last Time: " + lastGameTime + "s");
        }
    }

    int countMine(int r, int c) {
        if (r < 0 || r >= numRows || c < 0 || c >= numCols) {
            return 0;
        }
        if (mineList.contains(board[r][c])) {
            return 1;
        }
        return 0;
    }

    void updateTimerLabel() {
        textLabel.setText("Minesweeper: " + mineCount + "   Time: " + gameTime + "s");
    }
}
