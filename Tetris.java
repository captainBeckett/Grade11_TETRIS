import jaco.mp3.player.MP3Player;
import java.awt.Dimension;
import java.awt.Graphics;
import javax.swing.JComponent;
import javax.swing.JFrame;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.Timer;

/**
 *
 * @author ratif5596
 */
public class TetrisGame extends JComponent implements ActionListener {

    // Height and Width of our game
    static final int WIDTH = 460;
    static final int HEIGHT = 600;

    //Title of the window
    String title = "TETRIS";

    // sets the framerate and delay for our game
    // this calculates the number of milliseconds per frame
    // you just need to select an approproate framerate
    int desiredFPS = 60;
    int desiredTime = Math.round((1000 / desiredFPS));

    // timer used to run the game loop
    // this is what keeps our time running smoothly :)
    Timer gameTimer;

    // YOUR GAME VARIABLES WOULD GO HERE
    boolean startPage = true;
    
    boolean settingPage;
    Font instructionsFont = new Font("SansSerif", Font.BOLD, 35);
    
    boolean gamePage = false;
    Color darkPurple = new Color(62, 1, 56, 255);

    public static final int BOARD_WIDTH = 10;
    public static final int BOARD_HEIGHT = 20;
    public static final int BLOCK_WIDTH = 30;
    Color[][] board = new Color[BOARD_HEIGHT][BOARD_WIDTH];
    Color[] colors = {Color.BLACK, Color.MAGENTA, Color.RED, Color.PINK, Color.YELLOW, Color.GREEN, Color.CYAN, Color.BLUE};
    int[][][][] shapes = {
        //iBlock 
        {{{1, 1, 1, 1}},
        {{1}, {1}, {1}, {1}},
        {{1, 1, 1, 1}},
        {{1}, {1}, {1}, {1}}},
        //tBlock
        {{{1, 1, 1}, {0, 1, 0}},
        {{0, 1}, {1, 1}, {0, 1}},
        {{0, 1, 0}, {1, 1, 1}},
        {{1, 0}, {1, 1}, {1, 0}}},
        //lBlock
        {{{1, 1, 1}, {1, 0, 0}},
        {{1, 1}, {0, 1}, {0, 1}},
        {{0, 0, 1}, {1, 1, 1}},
        {{1, 0}, {1, 0}, {1, 1}}},
        //jBlock
        {{{1, 1, 1}, {0, 0, 1}},
        {{0, 1}, {0, 1}, {1, 1}},
        {{1, 0, 0}, {1, 1, 1}},
        {{1, 1}, {1, 0}, {1, 0}}},
        //oBlock
        {{{1, 1}, {1, 1}},
        {{1, 1}, {1, 1}},
        {{1, 1}, {1, 1}},
        {{1, 1}, {1, 1}}},
        //sBlock
        {{{0, 1, 1}, {1, 1, 0}},
        {{1, 0}, {1, 1}, {0, 1}},
        {{0, 1, 1}, {1, 1, 0}},
        {{1, 0}, {1, 1}, {0, 1}}},
        //zBlock
        {{{1, 1, 0}, {0, 1, 1}},
        {{0, 1}, {1, 1}, {1, 0}},
        {{1, 1, 0}, {0, 1, 1}},
        {{0, 1}, {1, 1}, {1, 0}}}
    };
    int[] shapeGenerator = new int[2];
    int currentShape = 0;
    int blockPosition = 0;
    int currentColor = 1;
    int nextColor;
    
    int x = 4;
    int y = 0;

    //create 2 booleans for down movement to ensure key is released after block collision
    boolean moveDown = false;
    boolean downReleased = true;

    //game default timer
    double defaultGameTimer;

    int deltaX = 0;
    
    boolean collision = false;
    boolean youLose = false;

    int lineCounter = 0;
    int numberOfLines = 0;
    int totalNumberOfLines = 0;
    int playerScore = 0;
    int playerLevel;

    int mouseX = 0;
    int mouseY = 0;

    BufferedImage tetrisLogo;
    BufferedImage spaceBackground;
    BufferedImage playButton;
    BufferedImage settingButton;
    BufferedImage quitButton;
    BufferedImage mainMenuButton;
    BufferedImage curvedLine;
    BufferedImage newGameButton;
    BufferedImage musicButton;
    BufferedImage offButton;

    BufferedImage pauseButton;
    boolean paused = false;

    Font scoreFont1 = new Font("arial", Font.BOLD, 72);
    Font scoreFont2 = new Font("arial", Font.BOLD, 72);
    Font scoreFont3 = new Font("arial", Font.BOLD, 50);
    Font scoreFont4 = new Font("arial", Font.BOLD, 40);
    Font scoreFont5 = new Font("arial", Font.BOLD, 35);
    Font labelFont = new Font("arial", Font.PLAIN, 18);

    MP3Player tetrisMusic = new MP3Player(new File("src/tetrisSoundtrack.mp3"));
    boolean music = true;
    // GAME VARIABLES END HERE    

    // Constructor to create the Frame and place the panel in
    // You will learn more about this in Grade 12 :)
    public TetrisGame() throws IOException {
        // creates a windows to show my game
        JFrame frame = new JFrame(title);

        // sets the size of my game
        this.setPreferredSize(new Dimension(WIDTH, HEIGHT));
        // adds the game to the window
        frame.add(this);

        // sets some options and size of the window automatically
        frame.setResizable(false);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        // shows the window to the user
        frame.setVisible(true);

        // add listeners for keyboard and mouse
        frame.addKeyListener(new Keyboard());
        Mouse m = new Mouse();
        this.addMouseMotionListener(m);
        this.addMouseWheelListener(m);
        this.addMouseListener(m);

        // Set things up for the game at startup
        setup();

        // Start the game loop
        gameTimer = new Timer(desiredTime, this);
        gameTimer.setRepeats(true);
        gameTimer.start();
    }

    // drawing of the game happens in here
    // we use the Graphics object, g, to perform the drawing
    // NOTE: This is already double buffered!(helps with framerate/speed)
    @Override
    public void paintComponent(Graphics g) {
        // always clear the screen first!
        g.clearRect(0, 0, WIDTH, HEIGHT);

        // GAME DRAWING GOES HERE
        Graphics2D g2d = (Graphics2D) g;

        //draw game pages 
        if (startPage == true){
            startPage(g);
        }else if(settingPage == true){
            settingPage(g);
        }else if (gamePage == true){
            gamePage(g);
        }

        // GAME DRAWING ENDS HERE
    }

    public void startPage(Graphics g) {
        //draw background and tetris logo
        g.drawImage(spaceBackground, 0, 0, 460, 600, this);
        g.drawImage(tetrisLogo, 130, 50, 200, 110, this);

        //draw buttons and enlarge when mouse hovers over them 
        if (mouseOnPlayButton()) {
            g.drawImage(playButton, 70, 195, 310, 60, this);
        } else {
            g.drawImage(playButton, 75, 200, 300, 50, this);
        }

        if (mouseOnSettingButton()) {
            g.drawImage(settingButton, 70, 275, 310, 60, this);
        } else {
            g.drawImage(settingButton, 75, 280, 300, 50, this);
        }

        if (mouseOnQuitButton()) {
            g.drawImage(quitButton, 70, 355, 310, 60, this);
        } else {
            g.drawImage(quitButton, 75, 360, 300, 50, this);
        }

    }

    public void settingPage(Graphics g) {
        //draw background
        g.setColor(darkPurple);
        g.fillRect(0, 0, 460, 600);

        //draw text and game instructions
        g.setColor(Color.WHITE);
        g.setFont(instructionsFont);
        g.drawString("HOW TO PLAY", 100, 50);

        g.setColor(Color.WHITE);
        g.setFont(labelFont);
        g.drawString("Tetris is a perennial experience known and loved all", 5, 100);
        g.drawString("over the world! The game works as follows:", 5, 120);
        g.drawString("Tetriminos will fall down from the screen. Every time", 5, 140);
        g.drawString("the current falling block collides with the bottom", 5, 160);
        g.drawString("of the grid or another block, a new tetrimino will appear", 5, 180);
        g.drawString("The goal of the game is to prevent the blocks from", 5, 200);
        g.drawString("stacking up to the top of the screen for as long as", 5, 220);
        g.drawString("possible. Each time a row fills up with blocks, the line", 5, 240);
        g.drawString("will clear and all above rows will fall down. Every 10", 5, 260);
        g.drawString("line clears, the speed level will increase until you", 5, 280);
        g.drawString("reach level 9.", 5, 300);
        g.drawString("To manually speed the block downwards, press the", 5, 340);
        g.drawString("\"down\" or \"s\" key.", 5, 360);
        g.drawString("To move the block left or right, press the \"left\" or \"right\"", 5, 400);
        g.drawString("arrow keys, or \"a\" and \"d\".", 5, 420);
        g.drawString("To rotate the block clockwise, press the \"up\" or \"w\" key.", 5, 460);

        //draw mainMenu button and enlarge when mouse hovers over it
        if (mouseOnMainMenuButton()) {
            g.drawImage(mainMenuButton, 215, 510, 220, 60, this);
        } else {
            g.drawImage(mainMenuButton, 220, 515, 200, 50, this);
        }

        //draw music button and off button according to if music is playing or not
        //enlarge buttons when mouse hovers over them
        if (mouseOnMusicButton() && music == true) {
            g.drawImage(musicButton, 155, 515, 60, 60, this);
        } else if (!mouseOnMusicButton() && music == true) {
            g.drawImage(musicButton, 160, 520, 50, 50, this);
        } else if (mouseOnMusicButton() && music == false) {
            g.drawImage(offButton, 155, 515, 60, 60, this);
        } else if (!mouseOnMusicButton() && music == false) {
            g.drawImage(offButton, 160, 520, 50, 50, this);
        }

    }

    public void gamePage(Graphics g) {
        //draw background
        g.drawImage(spaceBackground, 0, 0, 460, 600, this);
        g.setColor(darkPurple);
        g.fillRect(0, 0, 300, HEIGHT);

        //draw darkPurple box for next shape
        g.setColor(darkPurple);
        g.fillRect(315, 170, 135, 190);
        g.setColor(Color.WHITE);
        g.setFont(labelFont);
        g.drawString("NEXT", 360, 195);

        //update shape on board
        render(g);

        //draw current shape on board
        for (int row = 0; row < board.length; row++) {
            for (int column = 0; column < board[row].length; column++) {
                if (board[row][column] != null) {
                    g.setColor(board[row][column]);
                    g.fillRect(column * BLOCK_WIDTH, row * BLOCK_WIDTH, BLOCK_WIDTH, BLOCK_WIDTH);
                }
            }
        }

        //draw board rows and columns
        g.setColor(Color.WHITE);
        //rows
        for (int row = 0; row < BOARD_HEIGHT + 1; row++) {
            g.drawLine(0, BLOCK_WIDTH * row, BLOCK_WIDTH * BOARD_WIDTH, BLOCK_WIDTH * row);

        }
        //columns
        for (int column = 0; column < BOARD_WIDTH + 1; column++) {
            g.drawLine(column * BLOCK_WIDTH, 0, column * BLOCK_WIDTH, BLOCK_WIDTH * BOARD_HEIGHT);
        }

        //draw playerScore according to score value
        if (playerScore == 0) {
            g.setFont(scoreFont1);
            g.drawString("" + playerScore, 360, 75);
        } else if (playerScore > 0 && playerScore < 100) {
            g.setFont(scoreFont1);
            g.drawString("" + playerScore, 340, 75);
        } else if (playerScore >= 100 && playerScore < 1000) {
            g.setFont(scoreFont2);
            g.drawString("" + playerScore, 320, 75);
        } else if (playerScore >= 1000 && playerScore < 10000) {
            g.setFont(scoreFont3);
            g.drawString("" + playerScore, 325, 60);
        } else if (playerScore >= 10000 && playerScore < 100000) {
            g.setFont(scoreFont4);
            g.drawString("" + playerScore, 325, 60);
        } else if (playerScore >= 100000 && playerScore < 10000000) {
            g.setFont(scoreFont5);
            g.drawString("" + playerScore, 325, 60);
        }

        //draw playerLevel and linesCompleted label
        g.setFont(labelFont);
        g.drawString("Level:  " + playerLevel, 320, 130);
        g.drawString("Lines:  " + totalNumberOfLines, 320, 155);

        //draw you lose page when currentShape stays on top of game screen
        if (youLose == true) {
            youLosePage(g);
        }
        
        //draw pause page if button is pressed
        if (paused == true && youLose == false) {
            pausePage(g);
        }

        //draw white curved line beneath next shape box
        g.drawImage(curvedLine, 325, 380, 150, 200, this);

        //draw buttons and enlarge if mouse hovers over them
        //pause button
        if (mouseOnPauseButton()) {
            g.drawImage(pauseButton, 340, 390, 70, 70, this);
        } else {
            g.drawImage(pauseButton, 345, 395, 60, 60, this);
        }
        //mainMenu button
        if (mouseOnMainMenuButton2()) {
            g.drawImage(mainMenuButton, 305, 505, 155, 55, this);
        } else {
            g.drawImage(mainMenuButton, 310, 510, 150, 50, this);
        }
    }

    public void render(Graphics g) {
        //draw currentShape on screen
        for (int row = 0; row < shapes[currentShape][blockPosition].length; row++) {
            for (int column = 0; column < shapes[currentShape][blockPosition][0].length; column++) {
                //color in coordinates of shape
                if (shapes[currentShape][blockPosition][row][column] != 0) {
                    g.setColor(colors[currentColor]);
                    g.fillRect(column * BLOCK_WIDTH + x * BLOCK_WIDTH, row * BLOCK_WIDTH + y * BLOCK_WIDTH, BLOCK_WIDTH, BLOCK_WIDTH);
                }
            }
        }

        //draw next shape in shapeGenerator box
        for (int row = 0; row < shapes[shapeGenerator[1]][0].length; row++) {
            for (int column = 0; column < shapes[shapeGenerator[1]][0][row].length; column++) {
                if (shapes[shapeGenerator[1]][0][row][column] != 0) {
                    //change color of next shape
                    updateNextColor();
                    g.setColor(colors[nextColor]);
                    
                    //draw next block
                    //draw iBlock and oBlock differently in nextShape box to account for different sizes 
                    if (shapeGenerator[1] == 0) {
                        //iBlock
                        g.fillRect(column * BLOCK_WIDTH + 320, row * BLOCK_WIDTH + 210, BLOCK_WIDTH, BLOCK_WIDTH);
                        //draw block outline
                        g.setColor(Color.WHITE);
                        g.drawLine(column * BLOCK_WIDTH + 320, row * BLOCK_WIDTH + 210, column * BLOCK_WIDTH + 320, row * BLOCK_WIDTH + 210 + BLOCK_WIDTH);
                        g.drawLine(column * BLOCK_WIDTH + 320, row * BLOCK_WIDTH + 210, column * BLOCK_WIDTH + 320 + BLOCK_WIDTH, row * BLOCK_WIDTH + 210);
                        g.drawLine(column * BLOCK_WIDTH + 320 + BLOCK_WIDTH, row * BLOCK_WIDTH + 210, column * BLOCK_WIDTH + 320 + BLOCK_WIDTH, row * BLOCK_WIDTH + 210 + BLOCK_WIDTH);
                        g.drawLine(column * BLOCK_WIDTH + 320, row * BLOCK_WIDTH + 210 + BLOCK_WIDTH, column * BLOCK_WIDTH + 320 + BLOCK_WIDTH, row * BLOCK_WIDTH + 210 + BLOCK_WIDTH);
                    } else if (shapeGenerator[1] == 4) {
                        //oBlock
                        g.fillRect(column * BLOCK_WIDTH + 350, row * BLOCK_WIDTH + 210, BLOCK_WIDTH, BLOCK_WIDTH);
                        //draw block outline
                        g.setColor(Color.WHITE);
                        g.drawLine(column * BLOCK_WIDTH + 350, row * BLOCK_WIDTH + 210, column * BLOCK_WIDTH + 350, row * BLOCK_WIDTH + 210 + BLOCK_WIDTH);
                        g.drawLine(column * BLOCK_WIDTH + 350, row * BLOCK_WIDTH + 210, column * BLOCK_WIDTH + 350 + BLOCK_WIDTH, row * BLOCK_WIDTH + 210);
                        g.drawLine(column * BLOCK_WIDTH + 350 + BLOCK_WIDTH, row * BLOCK_WIDTH + 210, column * BLOCK_WIDTH + 350 + BLOCK_WIDTH, row * BLOCK_WIDTH + 210 + BLOCK_WIDTH);
                        g.drawLine(column * BLOCK_WIDTH + 350, row * BLOCK_WIDTH + 210 + BLOCK_WIDTH, column * BLOCK_WIDTH + 350 + BLOCK_WIDTH, row * BLOCK_WIDTH + 210 + BLOCK_WIDTH);
                    } else {
                        //all other shapes
                        g.fillRect(column * BLOCK_WIDTH + 340, row * BLOCK_WIDTH + 210, BLOCK_WIDTH, BLOCK_WIDTH);
                        //draw block outline
                        g.setColor(Color.WHITE);
                        g.drawLine(column * BLOCK_WIDTH + 340, row * BLOCK_WIDTH + 210, column * BLOCK_WIDTH + 340, row * BLOCK_WIDTH + 210 + BLOCK_WIDTH);
                        g.drawLine(column * BLOCK_WIDTH + 340, row * BLOCK_WIDTH + 210, column * BLOCK_WIDTH + 340 + BLOCK_WIDTH, row * BLOCK_WIDTH + 210);
                        g.drawLine(column * BLOCK_WIDTH + 340 + BLOCK_WIDTH, row * BLOCK_WIDTH + 210, column * BLOCK_WIDTH + 340 + BLOCK_WIDTH, row * BLOCK_WIDTH + 210 + BLOCK_WIDTH);
                        g.drawLine(column * BLOCK_WIDTH + 340, row * BLOCK_WIDTH + 210 + BLOCK_WIDTH, column * BLOCK_WIDTH + 340 + BLOCK_WIDTH, row * BLOCK_WIDTH + 210 + BLOCK_WIDTH);
                    }
                }
            }
        }
    }

    // This method is used to do any pre-setup you might need to do
    // This is run before the game loop begins!
    public void setup() throws IOException {
        // Any of your pre setup before the loop starts should go here
        
        //set default shape as iBlock
        shapeGenerator[0] = 0;
        
        //randomize next shape in shape generator array
        int lowest = 0;
        int highest = 6;
        int randNum = (int) (Math.random() * (highest - lowest + 1) + lowest);
        shapeGenerator[1] = randNum;

        //load in all images
        try {
            pauseButton = ImageIO.read(new File("src/pauseButton.png"));
            tetrisLogo = ImageIO.read(new File("src/tetrisLogo.png"));
            spaceBackground = ImageIO.read(new File("src/spaceBackground.jpg"));
            playButton = ImageIO.read(new File("src/playButton.png"));
            settingButton = ImageIO.read(new File("src/settingButton.png"));
            quitButton = ImageIO.read(new File("src/quitButton.png"));
            mainMenuButton = ImageIO.read(new File("src/mainMenuButton.png"));
            curvedLine = ImageIO.read(new File("src/curvedLine.png"));
            newGameButton = ImageIO.read(new File("src/newGameButton.png"));
            musicButton = ImageIO.read(new File("src/musicButton.jpg"));
            offButton = ImageIO.read(new File("src/offButton.jpg"));
        } catch (IOException ex) {
            Logger.getLogger(TetrisGame.class.getName()).log(Level.SEVERE, null, ex);
        }

        //initialize defaultTimer
        updateGameSpeed();
        
        //tell the sound to repeat
        tetrisMusic.setRepeat(true);
        //play soundtrack
        tetrisMusic.play();
    }

    // The main game loop
    // In here is where all the logic for my game will go
    public void loop() {
        //run game loop if on game page
        if (gamePage == true) {
            //timed vertical movement   
            if (defaultGameTimer <= 0 && paused == false && youLose == false) {
                //check if currentShape position falls within board
                if (!(y + shapes[currentShape][blockPosition].length >= BOARD_HEIGHT)) {
                    //check if next y position of currentShape is colored in or not
                    for (int row = 0; row < shapes[currentShape][blockPosition].length; row++) {
                        for (int column = 0; column < shapes[currentShape][blockPosition][row].length; column++) {
                            if (shapes[currentShape][blockPosition][row][column] != 0) {
                                //if colored, currentShape collided with other shape on board
                                if (board[y + 1 + row][x + column] != null) {
                                    collision = true;
                                }
                            }
                        }
                    }
                    if (collision == false) {
                        //increase currentShape y position
                        y++;
                    }
                } else {
                    //if not within bounds, stop currentShape
                    collision = true;
                }
                //reset timer value to begin new countdown
                updateGameSpeed();
            } else {
                //countdown
                defaultGameTimer--;
            }

            //if currentShape encounters collision, fill shape to board
            if (collision == true) {
                for (int row = 0; row < shapes[currentShape][blockPosition].length; row++) {
                    for (int column = 0; column < shapes[currentShape][blockPosition][row].length; column++) {
                        if (shapes[currentShape][blockPosition][row][column] != 0) {
                            board[y + row][x + column] = colors[currentColor];
                        }
                    }
                }
                //make sure key is released before next shape can fall down fast
                moveDown = false;
                
                //complete game functions before next tetrimino falls down
                checkForLine();
                updateScore();
                updateCurrentShape();
                updateCurrentColor();
                resetCoordinates();
                updateGameSpeed();
                checkForLoss();
            }

            //call on moveDown method in loop for more efficient movement
            if (moveDown == true && paused == false && youLose == false) {
                moveDown();
            }
            
        }

        //play soundtrack if music button is clickled
        if (music == true) {
            //start the music
            if (tetrisMusic.isPaused()) {
                tetrisMusic.play();
            }
        }else if(music == false){
            //pause soundtrack if pause button is clicked
            tetrisMusic.pause();
        }
    }

    public void updateCurrentShape() {
        //change currentShape to next block in shapeGenerator and generate new random next shape
        if (currentShape == shapeGenerator[0]) {
            shapeGenerator[0] = shapeGenerator[1];
            currentShape = shapeGenerator[0];
            //randomize next shape in generator
            int lowest = 0;
            int highest = 6;
            int randNum = (int) (Math.random() * (highest - lowest + 1) + lowest);
            shapeGenerator[1] = randNum;
        }
        //set default position of next shape to 0
        blockPosition = 0;
    }

    public void updateCurrentColor() {
        //assign each type of block a different color
        if (currentShape == 0) {
            currentColor = 1;
        } else if (currentShape == 1) {
            currentColor = 2;
        } else if (currentShape == 2) {
            currentColor = 3;
        } else if (currentShape == 3) {
            currentColor = 4;
        } else if (currentShape == 4) {
            currentColor = 5;
        } else if (currentShape == 5) {
            currentColor = 6;
        } else if (currentShape == 6) {
            currentColor = 7;
        }
    }

    public void updateNextColor() {
        //reassign color to next block
        if (shapeGenerator[1] == 0) {
            nextColor = 1;
        } else if (shapeGenerator[1] == 1) {
            nextColor = 2;
        } else if (shapeGenerator[1] == 2) {
            nextColor = 3;
        } else if (shapeGenerator[1] == 3) {
            nextColor = 4;
        } else if (shapeGenerator[1] == 4) {
            nextColor = 5;
        } else if (shapeGenerator[1] == 5) {
            nextColor = 6;
        } else if (shapeGenerator[1] == 6) {
            nextColor = 7;
        }
    }

    //this method resets the coordinates of the current shape to top of screen after collision
    public void resetCoordinates() {
        x = 4;
        y = 0;
        //reset collision
        collision = false;
    }

    //this method clears board of completed rows after collision
    public void checkForLine() {
        int bottomLine = board.length - 1;
        int count = 0;
        lineCounter = 0;
        //check if row is completely filled in
        for (int topLine = board.length - 1; topLine > 0; topLine--) {
            //create counter to track colored boxes
            count = 0;
            for (int column = 0; column < board[topLine].length; column++) {
                //if colored, increase counter
                if (board[topLine][column] != null) {
                    count++;
                }
                //fill in bottomline row of board with currentTopline colors
                board[bottomLine][column] = board[topLine][column];
            }
            //check if row is full
            /*if row is not full, bottomLine int will decrease to indicate transition to next row.
            Otherwise, if row is full, bottomLine int will remain the same. Therefore, when topline increases, and bottomline row stays the same,
            bottomline row will update with the colors of topline. Hence, a "row" will be removed.*/
            if (count < board[topLine].length) {
                bottomLine--;
            } else {
                //increase line tracker
                lineCounter++;
            }
        }

    }

    //this method updates player score according to number of lines cleared and player level
    public void updateScore() {
        int roundScore = 0;
        if (lineCounter == 1) {
            roundScore = 40 * (playerLevel + 1);
        } else if (lineCounter == 2) {
            roundScore = 100 * (playerLevel + 1);
        } else if (lineCounter == 3) {
            roundScore = 300 * (playerLevel + 1);
        } else if (lineCounter == 4) {
            roundScore = 1200 * (playerLevel + 1);
        }

        //update line trackers
        numberOfLines += lineCounter;
        totalNumberOfLines += lineCounter;

        //update level every 10 lines
        if (numberOfLines >= 10) {
            playerLevel++;
            numberOfLines = numberOfLines - 10;
        }

        //update player score
        playerScore += roundScore;
    }

    //this method checks if player loses
    public void checkForLoss() {
        //check if there are any blocks directly underneath new shape
        for (int row = 0; row < shapes[currentShape][blockPosition].length; row++) {
            for (int column = 0; column < shapes[currentShape][blockPosition][row].length; column++) {
                //check if bottom row of current shape is colored in
                if (shapes[currentShape][blockPosition][row][column] != 0) {
                    //check if block underneath shape in board is colored in
                    if (board[y + row + 1][x + column] != null) {
                        youLose = true;
                    }
                }
            }
        }
    }

    //this method updates game page graphics when player loses
    public void youLosePage(Graphics g) {
        //draw youLose label with shadow
        g.setColor(Color.BLACK);
        g.setFont(scoreFont3);
        g.drawString("YOU LOSE!", 10, 110);
        g.setColor(Color.CYAN);
        g.setFont(scoreFont3);
        g.drawString("YOU LOSE!", 15, 120);

        //draw newGameButton and enlarge when mouse hovers over it
        if (mouseOnNewGameButton()) {
            g.drawImage(newGameButton, 45, 145, 210, 60, this);
        } else {
            g.drawImage(newGameButton, 50, 150, 200, 50, this);
        }
    }

    //this method resets game
    public void resetGame() {
        youLose = false;
        collision = false;

        //clear board
        for (int row = 0; row < board.length; row++) {
            for (int column = 0; column < board[row].length; column++) {
                board[row][column] = null;
            }
        }

        //set all score-related integers to 0
        playerScore = 0;
        totalNumberOfLines = 0;
        playerLevel = 0;
        lineCounter = 0;
        numberOfLines = 0;

        updateGameSpeed();
        updateCurrentShape();
        resetCoordinates();

    }

    //this method updates game speed according to player level
    public void updateGameSpeed() {
        //increase game speed as player level increases
        if (playerLevel == 0) {
            defaultGameTimer = 0.40 * desiredFPS;
        } else if (playerLevel == 1) {
            defaultGameTimer = 0.37 * desiredFPS;
        } else if (playerLevel == 2) {
            defaultGameTimer = 0.34 * desiredFPS;
        } else if (playerLevel == 3) {
            defaultGameTimer = 0.31 * desiredFPS;
        } else if (playerLevel == 4) {
            defaultGameTimer = 0.28 * desiredFPS;
        } else if (playerLevel == 5) {
            defaultGameTimer = 0.25 * desiredFPS;
        } else if (playerLevel == 6) {
            defaultGameTimer = 0.23 * desiredFPS;
        } else if (playerLevel == 7) {
            defaultGameTimer = 0.20 * desiredFPS;
        } else if (playerLevel >= 8) {
            defaultGameTimer = 0.17 * desiredFPS;
        }
    }
    
    //this method moves the current shape to the right
    public void moveRight() {
        boolean moveX = true;
        deltaX = 1;
        //check if currentShape falls within board and doesn't collide with other shapes
        if (x + deltaX + shapes[currentShape][blockPosition][0].length <= 10) {
            for (int row = 0; row < shapes[currentShape][blockPosition].length; row++) {
                for (int column = 0; column < shapes[currentShape][blockPosition][row].length; column++) {
                    if (shapes[currentShape][blockPosition][row][column] != 0) {
                        if (board[y + row][x + deltaX + column] != null) {
                            moveX = false;
                        }
                    }
                }
            }
            //move shape to the right
            if (moveX == true) {
                x += deltaX;
            }
        }
    }
    
    //this method moves the current shape to the left
    public void moveLeft() {
        boolean moveX = true;
        deltaX = -1;
        //check if currentShape falls within board and doesn't collide with other shapes
        if (x > 0) {
            for (int row = 0; row < shapes[currentShape][blockPosition].length; row++) {
                for (int column = 0; column < shapes[currentShape][blockPosition][row].length; column++) {
                    if (shapes[currentShape][blockPosition][row][column] != 0) {
                        if (board[y + row][x + deltaX + column] != null) {
                            moveX = false;
                        }
                    }
                }
            }
            //move shape to the left
            if (moveX == true) {
                x += deltaX;
            }
        }
    }

    //this method moves the current shape down at a faster rate than defaultTimerSpeed
    public void moveDown() {
        //check if currentShape position is within board
        if (!(y + shapes[currentShape][blockPosition].length >= BOARD_HEIGHT)) {
            //check if next y position is colored in or not
            for (int row = 0; row < shapes[currentShape][blockPosition].length; row++) {
                for (int column = 0; column < shapes[currentShape][blockPosition][row].length; column++) {
                    if (shapes[currentShape][blockPosition][row][column] != 0) {
                        //if colored, currentShape collided with another shape on board
                        if (board[y + 1 + row][x + column] != null) {
                            collision = true;
                        }
                    }
                }
            }
            if (collision == false) {
                //increase currentShape y position
                y++;
            }
        } else {
            //if not in bounds, stop currentShape in its tracks
            collision = true;
        }
        //delay game timer to not influence manual movement
        defaultGameTimer = 100 * desiredFPS;
    }

    //this method rotates the current shape on board in a clockwise direciton
    public void rotateShape() {
        boolean rotateShape = true;
        //check if currentShape in blockPositions 0-2 can rotate within the bounds of the board
        if (blockPosition >= 0 && blockPosition < 3 && x + shapes[currentShape][blockPosition + 1][0].length <= 10 && y + shapes[currentShape][blockPosition + 1].length <= 20) {
            //check that new rotation doesn't collide with any colored in blocks
            for (int row = 0; row < shapes[currentShape][blockPosition + 1].length; row++) {
                for (int column = 0; column < shapes[currentShape][blockPosition + 1][row].length; column++) {
                    //if collision detection is present, don't rotate shape
                    if (board[y + row][x + column] != null) {
                        rotateShape = false;
                    }
                }
            }
            //increase block position to rotate shape clockwise
            if (rotateShape == true) {
                blockPosition++;
            }
        //check if current shape in blockPosition 3 can rotate within bounds of board
        } else if (blockPosition == 3 && x + shapes[currentShape][0][0].length <= 10 && y + shapes[currentShape][0].length <= 20) {
            //check that new rotation doesn't collide with any colored in blocks
            for (int row = 0; row < shapes[currentShape][0].length; row++) {
                for (int column = 0; column < shapes[currentShape][0][row].length; column++) {
                    //if collision detection, don't rotate shape
                    if (board[y + row][x + column] != null) {
                        rotateShape = false;
                    }
                }
            }
            //set block position to default(0)
            if (rotateShape == true) {
                blockPosition = 0;
            }
        }
    }

    //this method updates game page graphics when player pauses game
    public void pausePage(Graphics g) {
        //draw pause label
        g.setColor(Color.CYAN);
        g.setFont(scoreFont3);
        g.drawString("PAUSED", 50, 120);
    }

    //this method checks if mouse is on play button
    public boolean mouseOnPlayButton() {
        if (mouseX > 75
                && mouseX < 75 + 300
                && mouseY > 200
                && mouseY < 200 + 50) {
            return true;
        } else {
            return false;
        }
    }

    //this method checks if mouse in on setting button
    public boolean mouseOnSettingButton() {
        if (mouseX > 75
                && mouseX < 75 + 300
                && mouseY > 280
                && mouseY < 280 + 50) {
            return true;
        } else {
            return false;
        }
    }

    //this method checks if mouse is on main menu button in the setting page
    public boolean mouseOnMainMenuButton() {
        if (mouseX > 220
                && mouseX < 220 + 200
                && mouseY > 515
                && mouseY < 515 + 50) {
            return true;
        } else {
            return false;
        }
    }

    //this method checks if mouse is on quit button
    public boolean mouseOnQuitButton() {
        if (mouseX > 75
                && mouseX < 75 + 300
                && mouseY > 360
                && mouseY < 360 + 50) {
            return true;
        } else {
            return false;
        }
    }

    //this method checks if mouse is on pause button
    public boolean mouseOnPauseButton() {
        if (mouseX > 345
                && mouseX < 345 + 60
                && mouseY > 395
                && mouseY < 395 + 60) {
            return true;
        } else {
            return false;
        }
    }

    //this method checks if mouse is on music button
    public boolean mouseOnMusicButton() {
        if (mouseX > 160
                && mouseX < 160 + 50
                && mouseY > 520
                && mouseY < 520 + 50) {
            return true;
        } else {
            return false;
        }
    }

    //this method checks if mouse is on main menu button in game page
    public boolean mouseOnMainMenuButton2() {
        if (mouseX > 345
                && mouseX < 345 + 150
                && mouseY > 510
                && mouseY < 510 + 50) {
            return true;
        } else {
            return false;
        }
    }

    //this method checks if mouse is on new game button
    public boolean mouseOnNewGameButton() {
        if (mouseX > 50
                && mouseX < 50 + 200
                && mouseY > 150
                && mouseY < 150 + 50) {
            return true;
        } else {
            return false;
        }
    }

    // Used to implement any of the Mouse Actions
    private class Mouse extends MouseAdapter {

        // if a mouse button has been pressed down
        @Override
        public void mousePressed(MouseEvent e) {
            mouseX = e.getX();
            mouseY = e.getY();
            
            //pause game if mouse clicks on pause button
            if (mouseOnPauseButton() && gamePage == true && paused == false) {
                paused = true;
            } else if (mouseOnPauseButton() && gamePage == true && paused == true) {
                //unpause if pause button is clicked when paused
                paused = false;
                defaultGameTimer = 0.4 * desiredFPS;
            }

            //change screen to game page if play button is clicked
            if (mouseOnPlayButton() && startPage == true) {
                startPage = false;
                gamePage = true;
                paused = false;
            }

            //change screen to setting page if setting button is clicked
            if (mouseOnSettingButton() && startPage == true) {
                startPage = false;
                settingPage = true;
            }

            //change screen back to start page if mainMenu button on setting page is clicked
            if (mouseOnMainMenuButton() && settingPage == true) {
                settingPage = false;
                startPage = true;
            }

            //end program if quitButton is pressed
            if (mouseOnQuitButton() && startPage == true) {
                System.exit(0);
            }

            //turn music on or off when music button is clicked
            if (mouseOnMusicButton() && settingPage == true) {
                if (music == true) {
                    //turn off
                    music = false;
                } else if (music == false) {
                    //turn on
                    music = true;
                }
            }
            
            //pause game and redirect user to start page if main menu button on game page is pressed
            if (mouseOnMainMenuButton2() && gamePage == true) {
                paused = true;
                gamePage = false;
                startPage = true;
            }

            //reset game if newGameButton is pressed
            if (mouseOnNewGameButton() && gamePage == true && youLose == true) {
                resetGame();
            }
        }

        // if a mouse button has been released
        @Override
        public void mouseReleased(MouseEvent e) {

        }

        // if the scroll wheel has been moved
        @Override
        public void mouseWheelMoved(MouseWheelEvent e) {

        }

        // if the mouse has moved positions
        @Override
        public void mouseMoved(MouseEvent e) {
            //get coordinates of mouse
            mouseX = e.getX();
            mouseY = e.getY();
        }
    }

    // Used to implements any of the Keyboard Actions
    private class Keyboard extends KeyAdapter {

        // if a key has been pressed down
        @Override
        public void keyPressed(KeyEvent e) {
            //get the key code
            int key = e.getKeyCode();
            //move currentShape down if s key or down key is pressed
            if (key == KeyEvent.VK_S && downReleased == true
                    || key == KeyEvent.VK_DOWN && downReleased == true) {
                moveDown = true;
                downReleased = false;
            } //rotate currentShape if w key or up key is pressed 
            else if (key == KeyEvent.VK_W && gamePage == true && paused == false && youLose == false
                    || key == KeyEvent.VK_UP && gamePage == true && paused == false && youLose == false) {
                rotateShape();
            } //move currentShape to the right if d key or right key is pressed
            else if (key == KeyEvent.VK_D && gamePage == true && paused == false && youLose == false
                    || key == KeyEvent.VK_RIGHT && gamePage == true && paused == false && youLose == false) {
                moveRight();
            } //move currentShape to the left if a key or left key is pressed
            else if (key == KeyEvent.VK_A && gamePage == true && paused == false && youLose == false
                    || key == KeyEvent.VK_LEFT && gamePage == true && paused == false && youLose == false) {
                moveLeft();
            }
        }

        // if a key has been released
        @Override
        public void keyReleased(KeyEvent e) {
            //get the key code
            int key = e.getKeyCode();
            //stop currentShape from manually falling down if s key or down key is released
            if (key == KeyEvent.VK_S || key == KeyEvent.VK_DOWN) {
                updateGameSpeed();
                moveDown = false;
                downReleased = true;
            }
        }
    }

    @Override
    public void actionPerformed(ActionEvent ae) {
        loop();
        repaint();
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException {
        // creates an instance of my game
        TetrisGame game = new TetrisGame();
    }
}
