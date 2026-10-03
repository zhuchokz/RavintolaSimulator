package org.example.view;

import org.example.simu.model.CustomerType;

import javafx.application.Platform;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;

import java.util.Random;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.io.InputStream;

public class PizzaVisualisation extends Canvas implements IVisualisation {
    private GraphicsContext gc;
    private Image backgroundImage;
    private Map<Integer, CustomerSprite> customers = new ConcurrentHashMap<>();
    private Random random = new Random();
    private double currentBallSpeed = 3.5;

    private Image redCustomerImage;
    private Image blueCustomerImage;
    private Image greenCustomerImage;

    private static final int[][] PATH_COMMON_WALK = {
            {90, 200, 0},       // Arrival
            {230, 200, 5000},   // Order Queue
            {380, 200, 0},      // Ordering
            {500, 200, 5000},   // Payment Queue
            {650, 200, 0},      // Payment
            {780, 200, 5000}    // Oven Queue
    };

    private static final int[][] PATH_COMMON_ONLINE = {
            {90, 200, 0},       // Spawn on Arrival
            {90, 90, 0},        // Ascending to the upper orange dashed line
            {780, 90, 0},       // Flying right above the queues and counters
            {780, 200, 5000}    // Descending straight down into Oven Queue
    };

    // --- УНИКАЛЬНЫЕ ОДИНОЧНЫЕ ТОЧКИ ПЕЧЕЙ ---
    private static final int[][] START_OVEN1 = {{915, 140, 0}}; // Oven 1  Takeaway/Online
    private static final int[][] START_OVEN2 = {{915, 260, 0}}; // Oven 2 Takeaway/Online

    private static final int[][] START_DINEIN_OVEN1 = {{915, 140, 0}}; // Oven 1  Dine-in
    private static final int[][] START_DINEIN_OVEN2 = {{915, 260, 0}}; // Oven 2  Dine-in

    private static final int[][] END_TAKEAWAY_COMMON = {
            {1050, 260, 0},  // Pickup
            {1180, 260, 0}   // Exit Circle
    };

    private static final int[][] END_ONLINE_COMMON = {
            {1080, 140, 0},  // Courier
            {1180, 140, 0}   // Delivered Circle
    };

    private static final int[][] END_DINEIN_WITH_DRINK = {
            {1050, 260, 0},     // Pickup
            {1050, 400, 0},     // Vertical move down along the dashed line
            {630, 400, 0},   // Table area
            {630, 330, 0},      // Drink kiosk
            {630, 400, 5000},   // Table area
            {630, 450, 0},      // Moving straight DOWN from Table area
            {1180, 450, 0},     // Moving right along the bottom arrow under the Exit circle
            {1180, 260, 0}      // Moving straight up into the green Exit circle
    };

    // without drink
    private static final int[][] END_DINEIN_NO_DRINK = {
            {1050, 260, 0},     // Pickup
            {1050, 400, 0},     // Vertical move down along the dashed line
            {630, 400, 5000},   // Table area
            {630, 450, 0},      // Moving straight DOWN from Table area
            {1180, 450, 0},     // Moving right along the bottom arrow under the Exit circle
            {1180, 260, 0}      // Moving straight up into the green Exit circle
    };

    public PizzaVisualisation(int w, int h) {
        super(w, h);
        gc = this.getGraphicsContext2D();
        try {
            URL imageUrl = PizzaVisualisation.class.getResource("/pizzeria3.png");
            if (imageUrl != null) {
                backgroundImage = new Image(imageUrl.toExternalForm());
                System.out.println("The pizzeria background has been successfully loaded.");
            } else {
                Path imagePath = Path.of("ravintola", "src", "main", "resources", "pizzeria.png");
                if (!Files.exists(imagePath)) {
                    imagePath = Path.of("src", "main", "resources", "pizzeria.png");
                }
                backgroundImage = Files.exists(imagePath) ? new Image(imagePath.toUri().toString()) : null;
            }
        } catch (Exception e) {
            backgroundImage = null;
        }

        redCustomerImage = loadImage("takeaway.png");
        blueCustomerImage = loadImage("customer.png");
        greenCustomerImage = loadImage("online.png");

        clearDisplay();
        startAnimationLoop();
    }

    private Image loadImage(String fileName) {
        String resourceName = "/" + fileName;
        try (InputStream imageStream = PizzaVisualisation.class.getResourceAsStream(resourceName)) {
            if (imageStream != null) {
                Image image = new Image(imageStream);
                if (!image.isError()) return image;
            }
        } catch (Exception e) {
        }
        return null;
    }

    private int[][] combinePaths(int[][] path1, int[][] path2, int[][] path3) {
        int[][] result = new int[path1.length + path2.length + path3.length][3];
        System.arraycopy(path1, 0, result, 0, path1.length);
        System.arraycopy(path2, 0, result, path1.length, path2.length);
        System.arraycopy(path3, 0, result, path1.length + path2.length, path3.length);
        return result;
    }

    @Override
    public void resetVisuals() {
        Platform.runLater(() -> customers.clear());
    }

    @Override
    public void clearDisplay() {
        if (backgroundImage != null) {
            gc.drawImage(backgroundImage, 0, 0, this.getWidth(), this.getHeight());
        } else {
            gc.setFill(Color.LIGHTGRAY);
            gc.fillRect(0, 0, this.getWidth(), this.getHeight());
        }
    }

    @Override
    public void visualiseCustomer(int id, CustomerType type, String action) {
        Platform.runLater(() -> {
            if ("ARRIVED".equals(action)) {
                Image img = blueCustomerImage;
                Color col = Color.BLUE;
                int[][] chosenPath;

                boolean useOven1 = random.nextBoolean();

                if (type == CustomerType.ONLINE) {
                    img = greenCustomerImage;
                    col = Color.DARKGREEN;
                    chosenPath = useOven1
                            ? combinePaths(PATH_COMMON_ONLINE, START_OVEN1, END_ONLINE_COMMON)
                            : combinePaths(PATH_COMMON_ONLINE, START_OVEN2, END_ONLINE_COMMON);
                } else if (type == CustomerType.TAKEAWAY) {
                    img = redCustomerImage;
                    col = Color.RED;
                    chosenPath = useOven1
                            ? combinePaths(PATH_COMMON_WALK, START_OVEN1, END_TAKEAWAY_COMMON)
                            : combinePaths(PATH_COMMON_WALK, START_OVEN2, END_TAKEAWAY_COMMON);
                } else {
                    // DINEIN
                    int[][] ovenPath = useOven1 ? START_DINEIN_OVEN1 : START_DINEIN_OVEN2;
                    boolean takeDrink = random.nextBoolean();
                    int[][] drinkPath = takeDrink ? END_DINEIN_WITH_DRINK : END_DINEIN_NO_DRINK;

                    chosenPath = combinePaths(PATH_COMMON_WALK, ovenPath, drinkPath);
                }

                CustomerSprite newSprite = new CustomerSprite(chosenPath, img, col);
                customers.put(id, newSprite);
            }

            if ("FINISHED".equals(action)) {
                CustomerSprite sprite = customers.get(id);
                if (sprite != null) {
                    sprite.isLeaving = true;
                }
            }
        });
    }
    private void updateCustomerPositions() {
        for (Integer id : customers.keySet()) {
            CustomerSprite c = customers.get(id);
            if (c == null) continue;

            // Если клиент ждет в очереди — уменьшаем его таймер и никуда не двигаем
            if (c.isWaiting()) {
                continue;
            }

            if (c.currentStage < c.path.length) {
                int[] target = c.path[c.currentStage];
                double dx = target[0] - c.x;
                double dy = target[1] - c.y;
                double distance = Math.sqrt(dx * dx + dy * dy);

                if (distance < 4) {
                    // Проверяем наличие задержки на текущей точке
                    int delay = target[2];
                    if (delay > 0) {
                        c.startWaiting(delay);
                    }
                    c.currentStage++;
                } else {
                    c.x += (dx / distance) * currentBallSpeed;
                    c.y += (dy / distance) * currentBallSpeed;
                }
            } else {
                customers.remove(id);
            }
        }
    }

    private void startAnimationLoop() {
        Thread animationThread = new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(30);
                } catch (InterruptedException e) {
                    break;
                }

                Platform.runLater(() -> {
                    updateCustomerPositions();
                    clearDisplay();

                    for (CustomerSprite c : customers.values()) {
                        if (c.characterImage != null) {
                            gc.drawImage(c.characterImage, c.x - 12, c.y - 12, 24, 24);
                        } else {
                            gc.setFill(c.color);
                            gc.fillOval(c.x - 7, c.y - 7, 14, 14);
                            gc.setStroke(Color.BLACK);
                            gc.setLineWidth(1);
                            gc.strokeOval(c.x - 7, c.y - 7, 14, 14);
                        }
                    }
                });
            }
        });
        animationThread.setDaemon(true);
        animationThread.start();
    }
    @Override
    public void adjustAnimationSpeed(double speedMultiplier) {
        Platform.runLater(() -> {
            // Базовая скорость 3.5 умножается на входящий коэффициент (например, 1.0, 2.0 или 0.5)
            this.currentBallSpeed = 3.5 * speedMultiplier;
        });
    }

    private static class CustomerSprite {
        double x, y;
        int currentStage = 1;
        int[][] path;
        Image characterImage;
        Color color;
        boolean isLeaving = false;

        long waitTimeLeft = 0;
        long lastUpdateTime = 0;

        CustomerSprite(int[][] path, Image image, Color color) {
            this.path = path;
            this.characterImage = image;
            this.color = color;
            this.x = path[0][0];
            this.y = path[0][1];
        }
        boolean isWaiting() {
            if (waitTimeLeft > 0) {
                long now = System.currentTimeMillis();
                long elapsed = now - lastUpdateTime;
                lastUpdateTime = now;
                waitTimeLeft -= elapsed;
                if (waitTimeLeft <= 0) {
                    waitTimeLeft = 0;
                    return false;
                }
                return true;
            }
            return false;
        }
        void startWaiting(long ms) {
            this.waitTimeLeft = ms;
            this.lastUpdateTime = System.currentTimeMillis();
        }
    }
}