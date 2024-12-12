import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import javax.swing.*;

// Superclass untuk objek game
class GameObject {
    int x, y, width, height;
    Image img;

    GameObject(int x, int y, int width, int height, Image img) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.img = img;
    }

    void draw(Graphics g) {
        g.drawImage(img, x, y, width, height, null);
    }
}

// Subclass untuk Bird
class Bird extends GameObject {
    Bird(int x, int y, int width, int height, Image img) {
        super(x, y, width, height, img);
    }
}

// Subclass untuk Pipe
class Pipe extends GameObject {
    boolean passed = false;

    Pipe(int x, int y, int width, int height, Image img) {
        super(x, y, width, height, img);
    }
}

// Kelas utama FlappyBird
public class FlappyBird extends JPanel implements ActionListener, KeyListener {
    int boardWidth = 360;
    int boardHeight = 640;

    // Images
    Image backgroundImg;
    Image birdImg;
    Image topPipeImg;
    Image bottomPipeImg;

    // Bird dan Pipes
    Bird bird;
    ArrayList<Pipe> pipes;

    // Game logic
    int velocityX = -4; // Kecepatan pipa ke kiri
    int velocityY = 0; // Kecepatan burung naik/turun
    int gravity = 1;

    Timer gameLoop;
    Timer placePipeTimer;
    boolean gameOver = false;
    double score = 0;

    public FlappyBird() {
        setPreferredSize(new Dimension(boardWidth, boardHeight));
        setFocusable(true);
        addKeyListener(this);

        // Load images
        backgroundImg = new ImageIcon(getClass().getResource("./flappybirdbg.png")).getImage();
        birdImg = new ImageIcon(getClass().getResource("./flappybird.png")).getImage();
        topPipeImg = new ImageIcon(getClass().getResource("./toppipe.png")).getImage();
        bottomPipeImg = new ImageIcon(getClass().getResource("./bottompipe.png")).getImage();

        // Initialize bird dan pipes
        bird = new Bird(boardWidth / 8, boardWidth / 2, 34, 24, birdImg);
        pipes = new ArrayList<>();

        // Timer untuk menambahkan pipa baru
        placePipeTimer = new Timer(1500, e -> placePipes());
        placePipeTimer.start();

        // Timer untuk game loop
        gameLoop = new Timer(1000 / 60, this);
        gameLoop.start();
    }

    void placePipes() {
        int randomPipeY = (int) (-512 / 4 - Math.random() * (512 / 2));
        int openingSpace = boardHeight / 4;

        Pipe topPipe = new Pipe(boardWidth, randomPipeY, 64, 512, topPipeImg);
        pipes.add(topPipe);

        Pipe bottomPipe = new Pipe(boardWidth, randomPipeY + 512 + openingSpace, 64, 512, bottomPipeImg);
        pipes.add(bottomPipe);
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        draw(g);
    }

    void draw(Graphics g) {
        // Background
        g.drawImage(backgroundImg, 0, 0, boardWidth, boardHeight, null);

        // Bird
        bird.draw(g);

        // Pipes
        for (Pipe pipe : pipes) {
            pipe.draw(g);
        }

        // Score
        g.setColor(Color.white);
        g.setFont(new Font("Arial", Font.PLAIN, 32));
        if (gameOver) {
            g.drawString("Game Over: " + (int) score, 10, 35);
            showGameOverDialog();
        } else {
            g.drawString(String.valueOf((int) score), 10, 35);
        }
    }

    void showGameOverDialog() {
    // Buat dialog kustom
    JDialog dialog = new JDialog((JFrame) SwingUtilities.getWindowAncestor(this), "Game Over", true);
    dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
    dialog.setSize(300, 200);
    dialog.setLayout(new BorderLayout());
    dialog.setLocationRelativeTo(this);

    // Panel utama
    JPanel panel = new JPanel();
    panel.setBackground(Color.WHITE);
    panel.setLayout(new BorderLayout());

    // Label untuk menampilkan pesan
    JLabel messageLabel = new JLabel("<html><div style='text-align: center;'>Game Over!<br>Your Score: " + (int) score + "</div></html>");
    messageLabel.setFont(new Font("Poppins", Font.BOLD, 20));
    messageLabel.setForeground(new Color(0, 51, 102)); // Biru tua
    messageLabel.setHorizontalAlignment(SwingConstants.CENTER);
    messageLabel.setVerticalAlignment(SwingConstants.CENTER);

    // Tombol
    JPanel buttonPanel = new JPanel();
    buttonPanel.setBackground(Color.WHITE);
    JButton restartButton = new JButton("Restart");
    restartButton.setFont(new Font("Poppins", Font.PLAIN, 14));
    restartButton.setForeground(Color.WHITE);
    restartButton.setBackground(new Color(0, 51, 102)); // Biru tua
    restartButton.setFocusPainted(false);

    JButton quitButton = new JButton("Quit");
    quitButton.setFont(new Font("Poppins", Font.PLAIN, 14));
    quitButton.setForeground(Color.WHITE);
    quitButton.setBackground(new Color(0, 51, 102)); // Biru tua
    quitButton.setFocusPainted(false);

    buttonPanel.add(restartButton);
    buttonPanel.add(quitButton);

    // Tambahkan komponen ke panel utama
    panel.add(messageLabel, BorderLayout.CENTER);
    panel.add(buttonPanel, BorderLayout.SOUTH);

    // Tambahkan panel ke dialog
    dialog.add(panel);

    // Event handler untuk tombol
    restartButton.addActionListener(e -> {
        dialog.dispose();
        restartGame();
    });

    quitButton.addActionListener(e -> {
        dialog.dispose();
        System.exit(0);
    });

    // Tampilkan dialog
    dialog.setVisible(true);
}

    void move() {
        // Bird movement
        velocityY += gravity;
        bird.y += velocityY;
        bird.y = Math.max(bird.y, 0);

        // Pipes movement
        for (Pipe pipe : pipes) {
            pipe.x += velocityX;

            if (!pipe.passed && bird.x > pipe.x + pipe.width) {
                score += 0.5;
                pipe.passed = true;
            }

            if (collision(bird, pipe)) {
                gameOver = true;
            }
        }

        if (bird.y > boardHeight) {
            gameOver = true;
        }
    }

    boolean collision(GameObject a, GameObject b) {
        return a.x < b.x + b.width &&
               a.x + a.width > b.x &&
               a.y < b.y + b.height &&
               a.y + a.height > b.y;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (!gameOver) {
            move();
            repaint();
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_SPACE) {
            velocityY = -9;
            if (gameOver) {
                restartGame();
            }
        }
    }

    void restartGame() {
        bird.y = boardWidth / 2;
        velocityY = 0;
        pipes.clear();
        score = 0;
        gameOver = false;
        placePipeTimer.start();
        gameLoop.start();
    }

    @Override
    public void keyReleased(KeyEvent e) {}

    @Override
    public void keyTyped(KeyEvent e) {}

    public static void main(String[] args) {
        JFrame frame = new JFrame("Flappy Bird");
        FlappyBird game = new FlappyBird();
        frame.add(game);
        frame.setSize(360, 640);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
