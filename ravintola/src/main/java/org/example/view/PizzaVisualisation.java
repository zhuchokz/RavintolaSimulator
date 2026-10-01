package org.example.view;

import org.example.simu.model.CustomerType;

import javafx.application.Platform;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.List;
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

    private static final int[][] PATH_TAKEAWAY_OVEN1 = {
            {55, 125},   // Arrival
            {140, 125},  // Order Queue
            {230, 125},  // Ordering
            {325, 125},  // Payment Queue
            {415, 125},  // Payment
            {510, 125},  // Oven Queue
            {595, 85},   // Oven 1
            {665, 160},  // Pickup
            {745, 160}   // Exit Circle
    };

    // 2. Takeaway via Oven 2
    private static final int[][] PATH_TAKEAWAY_OVEN2 = {
            {55, 125},   // Arrival
            {140, 125},  // Order Queue
            {230, 125},  // Ordering
            {325, 125},  // Payment Queue
            {415, 125},  // Payment
            {510, 125},  // Oven Queue
            {595, 160},  // Oven 2
            {665, 160},  // Pickup
            {745, 160}   // Exit Circle
    };


    private static final int[][] PATH_DINEIN_OVEN1 = {
            {55, 125},   // Arrival
            {140, 125},  // Order Queue
            {230, 125},  // Ordering
            {325, 125},  // Payment Queue
            {415, 125},  // Payment
            {510, 125},  // Oven Queue
            {595, 75},   // Oven 1
            {670, 160},  // Pickup
            {670, 260},  // Vertical move down along the dashed line
            {595, 260},  // Drink kiosk
            {400, 260},  //  Table area
            {400, 290},  // Moving straight DOWN from Table area
            {745, 290},  // Moving right along the bottom arrow under the Exit circle
            {745, 230}   // Moving straight up into the green Exit circle
    };


    // 4. Dine-in via Oven 2
    private static final int[][] PATH_DINEIN_OVEN2 = {
            {55, 125},   // Arrival
            {140, 125},  // Order Queue
            {230, 125},  // Ordering
            {325, 125},  // Payment Queue
            {415, 125},  // Payment
            {510, 125},  // Oven Queue
            {595, 160},  // Oven 2
            {670, 160},  // Pickup
            {670, 260},  // Vertical move down along the dashed line
            {595, 260},  // Entering right into the Drink kiosk
            {400, 260},  // Moving left into the Table area
            {400, 290},  // Moving straight DOWN from Table area
            {745, 290},  // Moving right along the bottom arrow under the Exit circle
            {745, 230}   // Moving straight up into the green Exit circle
    };


    // 5. Online via Oven 1
    private static final int[][] PATH_ONLINE_OVEN1 = {
            {55, 140},   // Spawn on Arrival
            {55, 40},    // Ascending to the upper orange dashed line
            {510, 40},   // Flying right above the queues and counters
            {510, 140},  // Descending straight down into Oven Queue
            {595, 85},   // Oven 1
            {665, 85},   // Courier
            {745, 85}    // Delivered Circle
    };


    // 6. Online via Oven 2
    private static final int[][] PATH_ONLINE_OVEN2 = {
            {55, 140},   // Spawn on Arrival
            {55, 40},    // Ascending to the upper orange dashed line
            {510, 40},   // Flying right above the counters
            {510, 140},  // Descending straight down into Oven Queue
            {595, 160},  // Oven 2
            {665, 85},   // Courier
            {745, 85}    // Delivered Circle
    };

    public PizzaVisualisation(int w, int h) {
        super(w, h);
        gc = this.getGraphicsContext2D();
        try {
            URL imageUrl = PizzaVisualisation.class.getResource("/pizzeria2.png");
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

                // Select a random oven for this client (Oven 1 or Oven 2)
                boolean useOven1 = random.nextBoolean();

                if (type == CustomerType.ONLINE) {
                    img = greenCustomerImage;
                    col = Color.DARKGREEN;
                    chosenPath = useOven1 ? PATH_ONLINE_OVEN1 : PATH_ONLINE_OVEN2;
                } else if (type == CustomerType.TAKEAWAY) {
                    img = redCustomerImage;
                    col = Color.RED;
                    chosenPath = useOven1 ? PATH_TAKEAWAY_OVEN1 : PATH_TAKEAWAY_OVEN2;
                } else {
                    chosenPath = useOven1 ? PATH_DINEIN_OVEN1 : PATH_DINEIN_OVEN2;
                }

                // Create a sprite and immediately pass it the entire prepared PATH from the top of the file
                CustomerSprite newSprite = new CustomerSprite(chosenPath, img, col);
                customers.put(id, newSprite);
            }

            // If the engine sends FINISHED for Takeaway or Online, confirm the removal upon completion of the route.
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

            if (c.currentStage < c.path.length) {
                int[] target = c.path[c.currentStage];
                double dx = target[0] - c.x;
                double dy = target[1] - c.y;
                double distance = Math.sqrt(dx * dx + dy * dy);

                if (distance < 4) {
                    c.currentStage++; // Move to the next point in the PATH array
                } else {
                    c.x += (dx / distance) * currentBallSpeed;
                    c.y += (dy / distance) * currentBallSpeed;
                }
            } else {
                // If the ball has passed all points in the PATH array, remove it from the canvas.
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

    public void adjustAnimationSpeed(double factor) {
        this.currentBallSpeed *= factor;
        if (this.currentBallSpeed > 15.0) this.currentBallSpeed = 15.0;
        if (this.currentBallSpeed < 0.5) this.currentBallSpeed = 0.5;
    }

    private static class CustomerSprite {
        double x, y;
        int currentStage = 1;
        int[][] path;
        Image characterImage;
        Color color;
        boolean isLeaving = false;

        CustomerSprite(int[][] path, Image image, Color color) {
            this.path = path;
            this.characterImage = image;
            this.color = color;
            this.x = path[0][0];
            this.y = path[0][1];
        }
    }
}