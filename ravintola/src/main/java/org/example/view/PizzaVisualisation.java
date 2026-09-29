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
    // private List<CustomerSprite> customers = new ArrayList<>();
    private Map<Integer, CustomerSprite> customers = new ConcurrentHashMap<>();
    private Random random = new Random();
    private double currentBallSpeed = 3.5;

    private Image redCustomerImage;
    private Image blueCustomerImage;
    private Image greenCustomerImage;

    // 1. Shared initial path for Takeaway and Restaurant customers up to the Ovens
    private static final int[][] PATH_TO_OVENS = {
            {55, 185},   // Arrival
            {145, 185},  // Order Queue
            {245, 185},  // Ordering
            {355, 185},  // Payment Queue
            {455, 185},  // Payment
            {565, 185},  // Oven Queue
            {690, 185}   // Ovens (Entering the oven station)
    };

    // 2. Path segment: Directly from Ovens to Exit (Takeaway or Restaurant without dining in)
    private static final int[][] PATH_DIRECT_EXIT = {
            {585, 305},  // Diagonal move down-left to Pickup
            {585, 420}   // Vertical move down to the green Exit circle
    };

    // 3. Path segment: From Ovens THROUGH TABLES AND DRINKS (For customers dining in the hall)
    private static final int[][] PATH_THROUGH_TABLES = {
            {585, 305},  // Moving down to Pickup
            {470, 305},  // Moving left to Tables
            {350, 305},  // Moving left to Drink kiosk
            {470, 360},  // Following the arc towards the exit
            {585, 380}   // Entering the Exit circle
    };

    // 4. Complete path for ONLINE orders (Delivery)
    private static final int[][] PATH_ONLINE = {
            {455, 185},  // Spawning at Payment
            {565, 185},  // Oven Queue
            {715, 185},  // Ovens
            {690, 305},  // Vertical move down to Courier
            {690, 420}   // Vertical move down to the green Delivered circle
    };


    public PizzaVisualisation(int w, int h) {
        super(w, h);
        gc = this.getGraphicsContext2D();

        // 1. Loading the background image
        try {
            URL imageUrl = PizzaVisualisation.class.getResource("/pizzeria.png");
            if (imageUrl != null) {
                backgroundImage = new Image(imageUrl.toExternalForm());
                System.out.println("The pizzeria background has been successfully loaded.");
            } else {
                Path imagePath = Path.of("ravintola", "src", "main", "resources", "pizzeria.png");
                if (!Files.exists(imagePath)) {
                    imagePath = Path.of("src", "main", "resources", "pizzeria.png");
                }
                backgroundImage = Files.exists(imagePath)
                        ? new Image(imagePath.toUri().toString())
                        : null;
                if (backgroundImage == null) {
                    System.out.println("File pizzeria.png not found in the classpath or resource folder.");
                }
            }
        } catch (Exception e) {
            backgroundImage = null;
            System.err.println("Failed to load pizzeria.png: " + e.getMessage());
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
                if (!image.isError()) {
                    System.out.println("Loaded sprite: " + fileName);
                    return image;
                }
                System.err.println("Invalid image resource: " + resourceName);
            } else {
                System.err.println("Sprite resource not found: " + resourceName);
            }
        } catch (Exception e) {
            System.err.println("Failed to load sprite " + fileName + ": " + e.getMessage());
        }

        Path imagePath = Path.of("ravintola", "src", "main", "resources", fileName);
        if (!Files.exists(imagePath)) {
            imagePath = Path.of("src", "main", "resources", fileName);
        }
        if (Files.exists(imagePath)) {
            Image image = new Image(imagePath.toUri().toString());
            if (!image.isError()) {
                System.out.println("Loaded sprite from file: " + imagePath);
                return image;
            }
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
            // 1. Spawning
            if ("ARRIVED".equals(action)) {
                Image img = blueCustomerImage;
                Color col = Color.BLUE;
                if (type == CustomerType.ONLINE) {
                    img = greenCustomerImage;
                    col = Color.DARKGREEN;
                } else if (type == CustomerType.TAKEAWAY) {
                    img = redCustomerImage;
                    col = Color.RED;
                }
                customers.put(id, new CustomerSprite(img, col, type));
                return;
            }

            CustomerSprite sprite = customers.get(id);
            if (sprite == null) return;

            // 2. Customer completed order -> Smooth exit path
            if ("FINISHED".equals(action)) {
                sprite.isLeaving = true;
                if (sprite.type == CustomerType.ONLINE) {
                    sprite.addWaypoint(690, 420); // Delivery door exit
                } else {
                    sprite.addWaypoint(585, 420); // Main exit door
                }
                return;
            }

            // 3. Station actions with L-shaped hallway waypoints
            switch (action) {
                case "ORDER":
                    sprite.addWaypoint(145, 185); // Queue
                    sprite.addWaypoint(245, 185); // Counter
                    break;

                case "PAY":
                    sprite.addWaypoint(355, 185); // Queue
                    sprite.addWaypoint(455, 185); // Counter
                    break;

                case "OVEN":
                    if (sprite.type == CustomerType.ONLINE) {
                        sprite.addWaypoint(565, 185);
                        sprite.addWaypoint(715, 185);
                    } else {
                        sprite.addWaypoint(565, 185);
                        sprite.addWaypoint(690, 185);
                    }
                    break;

                case "PICKUP":
                    sprite.addWaypoint(585, 305); // Move down to Pickup
                    break;

                case "COURIER":
                    sprite.addWaypoint(690, 305); // Move down to Courier station
                    break;

                case "DRINK":
                    sprite.addWaypoint(350, 305); // Drink kiosk
                    break;

                case "TABLE":
                    sprite.addWaypoint(470, 305); // Dining table
                    break;
            }
        });
    }

    private void updateCustomerPositions() {
        for (Map.Entry<Integer, CustomerSprite> entry : customers.entrySet()) {
            CustomerSprite c = entry.getValue();

            if (c.hasWaypoints()) {
                double[] target = c.getCurrentTarget();
                double dx = target[0] - c.x;
                double dy = target[1] - c.y;
                double distance = Math.sqrt(dx * dx + dy * dy);

                if (distance > 2.0) {
                    // Clamp speed so sprite does not overshoot or shake
                    double speed = Math.min(this.currentBallSpeed, distance);
                    c.x += (dx / distance) * speed;
                    c.y += (dy / distance) * speed;
                } else {
                    // Reached waypoint: snap position and proceed to next waypoint
                    c.x = target[0];
                    c.y = target[1];
                    c.advanceWaypoint();
                }
            } else if (c.isLeaving) {
                // Reached final exit circle -> remove sprite from canvas
                customers.remove(entry.getKey());
            }
        }
    }

    private void startAnimationLoop() {
        Thread animationThread = new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(30); // 30 frames per second
                } catch (InterruptedException e) {
                    break;
                }

                Platform.runLater(() -> {
                    updateCustomerPositions();
                    clearDisplay();

                    // Rendering all the customers on the canvas
                    for (CustomerSprite c : customers.values()) {
                        if (c.characterImage != null) {
                            // Draws the character icon, centering it. Size is set to 24x24 px.
                            gc.drawImage(c.characterImage, c.x - 12, c.y - 12, 24, 24);
                        } else {
                            // Fallback: If image fails to load, draw the original circle
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

    private static class CustomerSprite {
        double x, y;
        Image characterImage;
        Color color;
        CustomerType type;
        boolean isLeaving = false;
        private final List<double[]> path = new ArrayList<>();

        CustomerSprite(Image img, Color color, CustomerType type) {
            this.characterImage = img;
            this.color = color;
            this.type = type;

            // Correct initial spawn locations
            if (type == CustomerType.ONLINE) {
                this.x = 455; // Payment/kitchen area
                this.y = 185;
            } else {
                this.x = 55;  // Entrance door
                this.y = 185;
            }
        }

        void addWaypoint(double x, double y) {
            path.add(new double[]{x, y});
        }

        boolean hasWaypoints() {
            return !path.isEmpty();
        }

        double[] getCurrentTarget() {
            return path.get(0);
        }

        void advanceWaypoint() {
            if (!path.isEmpty()) {
                path.remove(0);
            }
        }
    }

    public void adjustAnimationSpeed(double factor) {
        this.currentBallSpeed *= factor;

        if (this.currentBallSpeed > 15.0) this.currentBallSpeed = 15.0;
        if (this.currentBallSpeed < 0.5) this.currentBallSpeed = 0.5;
    }
}
