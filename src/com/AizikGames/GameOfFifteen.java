package com.AizikGames;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Random;

// This is a swing puzzle game piece of numbers ranging from 1 to 15
public class GameOfFifteen extends JPanel {

    // size of our game of 15 instance
    private int size;
    // Number of tiles
    private int nbTiles;
    //Grid UI dimension
    private int dimension;
    // ForeGround color
    private static final Color FOREGROUND_COLOR = new Color(239,83,80);
    // Random object to shuffle tiles
    private static final Random RANDOM = new Random();
    // Storing the tiles in a 1D array of integers
    private int[] tiles;
    // size of the tile on the UI
    private int tileSize;
    // position of a blank tile on the game canvas
    private int blankPosition;
    // margin of the grid on the UI
    private int margin;
    // Grid UI size
    private int gridSize;
    // Game status
    private boolean isGameOver;

    //Constructor
    public GameOfFifteen(int size, int dim, int mar){
        this.size = size;
        margin = mar;
        dimension = dim;

        // init tiles
        nbTiles = size * size -1; //  we used -1 because we don't count blank tiles
        tiles = new int[size * size]; // number of tiles

        // calculate the grid size and tile size
        gridSize = (dim - 2 * margin);
        tileSize = gridSize / size;

        // Styling the UI
        setPreferredSize( new Dimension(dimension, dimension + margin) );
        setBackground(Color.WHITE); // The Background color of the canvas or panel
        setForeground(FOREGROUND_COLOR);
        setFont( new Font("Roboto", Font.BOLD, 60) );

        // set state of game over to true by default
        isGameOver = true;

        // add mouse event listener
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                //super.mousePressed(e);
                //used to let users interact on the grid by clicking
                if (isGameOver){
                    newGame();
                } else {
                    // get the position of the click
                    int ex = e.getX() - margin;
                    int ey = e.getY() - margin;

                    // check if the grid is clicked
                    if( ex < 0 || ex > gridSize || ey < 0 || ey > gridSize )
                        return;;

                     // get the position in the grid
                    int c1 = ex / tileSize;
                    int r1 = ey / tileSize;

                    // get the position of the blank cell
                    int c2 = blankPosition % size;
                    int r2 = blankPosition / size;

                    int clickPos = r1 * size + c1;

                    int dir = 0;

                    // search direction for multiple tiles moves at once
                    if ( c1 == c2 && Math.abs(r1 - r2) > 0 )
                        dir = ( r1 - r2 ) > 0 ? size : -size;
                    else if( r1 == r2 && Math.abs(c1 - c2) > 0 )
                        dir = ( c1 - c2 ) > 0 ? 1 : -1;

                    if(dir != 0){
                        // we move the tiles in the direction
                        do{
                            int newBlankPosition = blankPosition + dir;
                            tiles[blankPosition] = tiles[newBlankPosition];
                            blankPosition = newBlankPosition;
                        }while(blankPosition != clickPos);

                        tiles[blankPosition] = 0;
                    }
                    // we check if game is solved
                    isGameOver = isSolved();
                }
                // we repaint the canvas or panel
                repaint();
            }
        });

        newGame();
    }

    private void newGame(){
        do{
            reset(); // reset in initial state
            shuffle(); // shuffle
        }while (! isSolvable()) ; //make it until game is solved

        isGameOver = false;
    }

    private void reset(){
        for (int  i = 0; i < tiles.length; i++){
            tiles[i] = (i + 1) % tiles.length;
        }
        // set the blank tiles to the last one
        blankPosition = tiles.length - 1;
    }

    private void shuffle(){
        // we don't shuffle the blank tiles, we leave it in the solved position
        int n = nbTiles;

        while (n > 1) {
            int r = RANDOM.nextInt(n--);
            int temp = tiles[r];
            tiles[r] = tiles[n];
            tiles[n] = temp;
        }
    }

    // note that only half permutations of the puzzle are solvable
    // whenever a tile is preceded by a tile with a higher number it counts as an Inversion
    // since the blank tile is in solved position by default hence the number of inversions must be even for the game to be solvable
    private boolean isSolvable(){
        int countInversions = 0;

        for (int i = 0; i< nbTiles; i++){
            for (int j = 0; j < i; j++){
                if(tiles[j] > tiles[i]){
                    countInversions++;
                }
            }
        }
        return countInversions % 2 == 0;
    }

    private boolean isSolved(){
        if (tiles[ tiles.length - 1 ] != 0)
            return false;

        for(int i = nbTiles -1; i >= 0; i--){
            if (tiles[i] != i+1)
                return false;
        }
        return  true;
    }

    private void drawGrid(Graphics2D g){
        for (int i = 0; i < tiles.length; i++){
            // we convert 1D coo-ordinates to 2D co-ordinates given the size of the 2D Array
            int r = i / size;
            int c = i % size;

            // we convert the co-ordinates on the UI
            int x = margin + c * tileSize;
            int y = margin  + r * tileSize;

            // check case if the tile is blank
            if (tiles[i] == 0){
                if (isGameOver){
                    g.setColor(FOREGROUND_COLOR);
                    drawCenteredString(g, "YaY", x, y);
                }
                continue;
            }
            // for the other tiles
            g.setColor(getForeground());
            g.fillRoundRect(x, y, tileSize, tileSize, 25, 25);
            g.setColor(Color.BLACK);
            g.drawRoundRect(x, y, tileSize, tileSize, 25, 25);
            g.setColor(Color.WHITE);

            drawCenteredString(g, String.valueOf(tiles[i]), x, y);
        }
    }

    private void drawStartMessage(Graphics2D g){
        if (isGameOver){
            g.setFont(getFont().deriveFont(Font.BOLD, 14));
            g.setColor(FOREGROUND_COLOR);
            String s = "Click to start again";
            g.drawString(s, ( getWidth() - g.getFontMetrics().stringWidth(s) / 2 ), getHeight() - margin);
        }
    }

    private void drawCenteredString(Graphics2D g, String s, int x, int y){
        FontMetrics fm = g.getFontMetrics();
        int asc = fm.getAscent();
        int des = fm.getDescent();
        g.drawString(s, x + ( tileSize - fm.stringWidth(s) ) / 2, y + ( asc +  ( tileSize - (asc + des) ) / 2 ) );
    }

    @Override
    protected void paintComponent(Graphics g){
        super.paintComponent(g);
        Graphics2D g2D = (Graphics2D) g;
        g2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        drawGrid(g2D);
        drawStartMessage(g2D);
    }

    public static void main(String[] args) {
	// write your code here
        SwingUtilities.invokeLater( () ->{
            JFrame frame = new JFrame();
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setResizable(false);
            frame.setTitle("Puzzle Game of Fifteen");
            frame.add(new GameOfFifteen(4, 550, 30), BorderLayout.CENTER);
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        } );
    }
}
