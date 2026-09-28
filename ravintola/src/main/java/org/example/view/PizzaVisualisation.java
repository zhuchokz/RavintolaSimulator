package org.example.view;

import javafx.application.Platform;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.io.InputStream;

public class PizzaVisualisation extends Canvas implements IVisualisation {
    private GraphicsContext gc;
    private Image backgroundImage;
    private List<CustomerSprite> customers = new ArrayList<>();
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

    public void clearDisplay() {
        if (backgroundImage != null) {
            gc.drawImage(backgroundImage, 0, 0, this.getWidth(), this.getHeight());
        } else {
            gc.setFill(Color.LIGHTGRAY);
            gc.fillRect(0, 0, this.getWidth(), this.getHeight());
        }
    }

    @Override
    public void newCustomer() {
        Platform.runLater(() -> {
            int chance = random.nextInt(100);

            if (chance < 35) {
                // Delivery (Online) — Green character
                customers.add(new CustomerSprite(PATH_ONLINE, greenCustomerImage, Color.DARKGREEN, false));
            } else if (chance < 70) {
                // Takeaway customer — Red character
                customers.add(new CustomerSprite(PATH_TO_OVENS, redCustomerImage, Color.RED, false));
            } else {
                // Restaurant customer — Blue character, stays at tables
                customers.add(new CustomerSprite(PATH_TO_OVENS, blueCustomerImage, Color.BLUE, true));
            }
        });
    }

    private void updateCustomerPositions() {
        for (int i = customers.size() - 1; i >= 0; i--) {
            CustomerSprite c = customers.get(i);
            int[] target = c.path[c.currentStage];

            double dx = target[0] - c.x;
            double dy = target[1] - c.y;
            double distance = Math.sqrt(dx * dx + dy * dy);

            if (distance < 4) {
                if (c.currentStage < c.path.length - 1) {
                    c.currentStage++;
                } else {
                    if (c.path == PATH_TO_OVENS) {
                        c.currentStage = 0;

                        // Random assignment for restaurant clients: 50% tables, 50% exit
                        if (c.isRestaurantType && random.nextBoolean()) {
                            c.path = PATH_THROUGH_TABLES;
                        } else {
                            c.path = PATH_DIRECT_EXIT;
                        }
                    } else {
                        customers.remove(i);
                    }
                }
            } else {
                double speed = this.currentBallSpeed;
                c.x += (dx / distance) * speed;
                c.y += (dy / distance) * speed;
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
                    for (CustomerSprite c : customers) {
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
        int currentStage = 1;
        int[][] path;
        Image characterImage;
        Color color;
        boolean isRestaurantType;

        CustomerSprite(int[][] path, Image image, Color color, boolean isRestaurantType) {
            this.path = path;
            this.characterImage = image;
            this.color = color;
            this.isRestaurantType = isRestaurantType;
            this.x = path[0][0];
            this.y = path[0][1];
        }
    }

    public void adjustAnimationSpeed(double factor) {
        this.currentBallSpeed *= factor;

        if (this.currentBallSpeed > 15.0) this.currentBallSpeed = 15.0;
        if (this.currentBallSpeed < 0.5) this.currentBallSpeed = 0.5;
    }
}
