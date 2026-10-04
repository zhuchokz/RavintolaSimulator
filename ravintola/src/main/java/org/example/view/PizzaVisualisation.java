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

    private static final int[] POS_ARRIVAL = {90, 200};
    private static final int[] POS_ONLINE_SPAWN = {90, 200};
    private static final int[] POS_ONLINE_FLY = {90, 90};
    private static final int[] POS_ONLINE_BEFORE_OVEN = {780, 90};

    private static final int[] POS_ORDER_QUEUE = {230, 200};
    private static final int[] POS_PAYMENT_QUEUE = {500, 200};
    private static final int[] POS_OVEN_QUEUE = {780, 200};

    private static final int[] POS_OVEN1 = {915, 140};
    private static final int[] POS_OVEN2 = {915, 260};

    private static final int[] POS_PICKUP = {1050, 260};
    private static final int[] POS_COURIER = {1080, 140};

    private static final int[] POS_DINEIN_DESCEND = {1050, 400};
    private static final int[] POS_TABLE = {630, 400};
    private static final int[] POS_DRINK = {630, 330};

    private static final int[] POS_DINEIN_EXIT_DOWN = {630, 450};
    private static final int[] POS_EXIT_ARROW = {1180, 450};
    private static final int[] POS_EXIT_CIRCLE = {1180, 260};

    private static final int[] POS_TAKEAWAY_EXIT = {1180, 260};
    private static final int[] POS_ONLINE_EXIT = {1180, 140};

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

    @Override
    public void resetVisuals() {
        Platform.runLater(() -> customers.clear());
    }

    @Override
    public void adjustAnimationSpeed(double speedMultiplier) {
        Platform.runLater(() -> {
            this.currentBallSpeed = 3.5 * speedMultiplier;
        });
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
                int[] startPos = (type == CustomerType.ONLINE) ? POS_ONLINE_SPAWN : POS_ARRIVAL;
                Image img = (type == CustomerType.ONLINE) ? greenCustomerImage :
                        (type == CustomerType.TAKEAWAY) ? redCustomerImage : blueCustomerImage;
                Color col = (type == CustomerType.ONLINE) ? Color.DARKGREEN :
                        (type == CustomerType.TAKEAWAY) ? Color.RED : Color.BLUE;

                CustomerSprite sprite = new CustomerSprite(startPos, img, col);
                customers.put(id, sprite);
                return;
            }

            CustomerSprite sprite = customers.get(id);
            if (sprite == null) return;

            switch (action) {
                case "ORDER":
                    sprite.addTarget(POS_ORDER_QUEUE);
                    break;
                case "PAY":
                    sprite.addTarget(POS_PAYMENT_QUEUE);
                    break;
                case "OVEN1":
                    if (type == CustomerType.ONLINE) {
                        sprite.addTarget(POS_ONLINE_FLY);
                        sprite.addTarget(POS_ONLINE_BEFORE_OVEN);
                    }
                    sprite.addTarget(POS_OVEN_QUEUE);
                    sprite.assignedOven = 1;
                    break;
                case "OVEN2":
                    if (type == CustomerType.ONLINE) {
                        sprite.addTarget(POS_ONLINE_FLY);
                        sprite.addTarget(POS_ONLINE_BEFORE_OVEN);
                    }
                    sprite.addTarget(POS_OVEN_QUEUE);
                    sprite.assignedOven = 2;
                    break;
                case "COURIER":
                    sprite.addTarget(sprite.assignedOven == 1 ? POS_OVEN1 : POS_OVEN2);
                    sprite.addTarget(POS_COURIER);
                    break;
                case "PICKUP":
                    sprite.addTarget(sprite.assignedOven == 1 ? POS_OVEN1 : POS_OVEN2);
                    sprite.addTarget(POS_PICKUP);
                    break;
                case "DRINK":
                    sprite.wentToDrink = true;
                    sprite.addTarget(POS_DINEIN_DESCEND);
                    sprite.addTarget(POS_TABLE);
                    sprite.addTarget(POS_DRINK);
                    break;

                case "TABLE":
                    if (sprite.wentToDrink) {
                        sprite.addTarget(POS_TABLE);
                    } else {
                        sprite.addTarget(POS_DINEIN_DESCEND);
                        sprite.addTarget(POS_TABLE);
                    }
                    break;
                case "FINISHED":
                    sprite.isLeaving = true;
                    if (type == CustomerType.ONLINE) {
                        sprite.addTarget(POS_ONLINE_EXIT);
                    } else if (type == CustomerType.TAKEAWAY) {
                        sprite.addTarget(POS_TAKEAWAY_EXIT);
                    } else {
                        sprite.addTarget(POS_DINEIN_EXIT_DOWN);
                        sprite.addTarget(POS_EXIT_ARROW);
                        sprite.addTarget(POS_EXIT_CIRCLE);
                    }
                    break;
            }
        });
    }

    private void updateCustomerPositions() {
        for (Integer id : customers.keySet()) {
            CustomerSprite c = customers.get(id);
            if (c == null) continue;

// If there are tasks in the movement queue, move toward the current target.
            if (!c.targetPoints.isEmpty()) {
                int[] target = c.targetPoints.peek();
                double dx = target[0] - c.x;
                double dy = target[1] - c.y;
                double distance = Math.sqrt(dx * dx + dy * dy);

                if (distance < 4) {
                    c.x = target[0];
                    c.y = target[1];
                    c.targetPoints.poll(); // Point reached, remove it from the task queue
                } else {
                    c.x += (dx / distance) * currentBallSpeed;
                    c.y += (dy / distance) * currentBallSpeed;
                }
            } else {
                // If there are no more targets and the engine has issued the FINISHED command, remove from the screen

                if (c.isLeaving) {
                    customers.remove(id);
                }
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
    private static class CustomerSprite {
        double x, y;
        java.util.Queue<int[]> targetPoints = new java.util.LinkedList<>();
        Image characterImage;
        Color color;
        boolean isLeaving = false;
        boolean wentToDrink = false;
        int assignedOven = 1; // Variable for synchronizing the furnace with the engine
        CustomerSprite(int[] startPos, Image image, Color color) {
            this.characterImage = image;
            this.color = color;
            this.x = startPos[0];
            this.y = startPos[1];
        }
        void addTarget(int[] point) {
            targetPoints.add(point);
        }
    }
}