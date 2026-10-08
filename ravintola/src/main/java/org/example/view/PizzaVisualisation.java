package org.example.view;

import org.example.simu.model.CustomerType;

import javafx.application.Platform;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import java.util.Map;
import java.util.Queue;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentHashMap;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.io.InputStream;

public class PizzaVisualisation extends Canvas implements IVisualisation {
    private GraphicsContext gc;
    private Image backgroundImage;
    private Map<Integer, CustomerSprite> customers = new ConcurrentHashMap<>();
    private double currentBallSpeed = 3.5;

    private volatile boolean isPaused = false;
    private volatile boolean isRunning = false;

    // Initial capacity values (they change corresponding to GUI)
    private int oven1Capacity = 4;
    private int oven2Capacity = 4;
    private int tableCapacity = 10;
    private int courierCapacity = 3;

    private int oven1Count = 0;
    private int oven2Count = 0;
    private int currentSeatedCount = 0;
    private int courierCount = 0;

    // Waiting queue lists
    private Queue<CustomerSprite> oven1WaitingQueue = new LinkedList<>();
    private Queue<CustomerSprite> oven2WaitingQueue = new LinkedList<>();
    private Queue<CustomerSprite> tableWaitingQueue = new LinkedList<>();
    private Queue<CustomerSprite> courierWaitingQueue = new LinkedList<>();

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
            URL imageUrl = PizzaVisualisation.class.getResource("/pizzeria.png");
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

    public void setStationCapacities(int o1Cap, int o2Cap, int tableCap, int courierCap) {
        this.oven1Capacity = o1Cap;
        this.oven2Capacity = o2Cap;
        this.tableCapacity = tableCap;
        this.courierCapacity = courierCap;
    }

    @Override
    public void startVisuals() {
        this.isRunning = true;
        this.isPaused = false;
    }

    @Override
    public void setPaused(boolean paused) {
        this.isPaused = paused;
    }

    @Override
    public void resetVisuals() {
        this.isRunning = false;
        this.isPaused = false;
        Platform.runLater(() -> {
            customers.clear();
            oven1WaitingQueue.clear();
            oven2WaitingQueue.clear();
            tableWaitingQueue.clear();
            courierWaitingQueue.clear();

            oven1Count = 0;
            oven2Count = 0;
            currentSeatedCount = 0;
            courierCount = 0;

            clearDisplay();
        });
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

    private void freeOvenSlotAndAdvanceQueue(CustomerSprite sprite) {
        if (sprite.assignedOven == 1) {
            if (sprite.isBaking) {
                sprite.isBaking = false;
                oven1Count--;
            }
            if (oven1Count < oven1Capacity && !oven1WaitingQueue.isEmpty()) {
                CustomerSprite next = oven1WaitingQueue.poll();
                oven1Count++;
                next.isBaking = true;
                next.addTarget(POS_OVEN1);
            }
        } else if (sprite.assignedOven == 2) {
            if (sprite.isBaking) {
                sprite.isBaking = false;
                oven2Count--;
            }
            if (oven2Count < oven2Capacity && !oven2WaitingQueue.isEmpty()) {
                CustomerSprite next = oven2WaitingQueue.poll();
                oven2Count++;
                next.isBaking = true;
                next.addTarget(POS_OVEN2);
            }
        }
    }

    private void routeCustomerToSeated(CustomerSprite sprite) {
        sprite.isSeated = true;
        sprite.addTarget(POS_DINEIN_DESCEND);
        sprite.addTarget(POS_TABLE);
        if (sprite.wantsDrink) {
            sprite.addTarget(POS_DRINK);
            sprite.addTarget(POS_TABLE);
        }
    }

    @Override
    public void visualiseCustomer(int id, CustomerType type, String action) {
        Platform.runLater(() -> {
            if (!isRunning) return;

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

                    if (oven1Count < oven1Capacity) {
                        oven1Count++;
                        sprite.isBaking = true;
                        sprite.addTarget(POS_OVEN1);
                    } else {
                        oven1WaitingQueue.add(sprite);
                    }
                    break;

                case "OVEN2":
                    if (type == CustomerType.ONLINE) {
                        sprite.addTarget(POS_ONLINE_FLY);
                        sprite.addTarget(POS_ONLINE_BEFORE_OVEN);
                    }
                    sprite.addTarget(POS_OVEN_QUEUE);
                    sprite.assignedOven = 2;

                    if (oven2Count < oven2Capacity) {
                        oven2Count++;
                        sprite.isBaking = true;
                        sprite.addTarget(POS_OVEN2);
                    } else {
                        oven2WaitingQueue.add(sprite);
                    }
                    break;

                case "COURIER":
                    if (!sprite.isBaking) {
                        sprite.addTarget(sprite.assignedOven == 1 ? POS_OVEN1 : POS_OVEN2);
                    }
                    freeOvenSlotAndAdvanceQueue(sprite);
                    sprite.addTarget(POS_COURIER); // Always move to courier point first

                    if (courierCount < courierCapacity) {
                        courierCount++;
                        sprite.isAtCourier = true;
                    } else {
                        courierWaitingQueue.add(sprite); // Wait at courier point if capacity full
                    }
                    break;

                case "PICKUP":
                    if (!sprite.isBaking) {
                        sprite.addTarget(sprite.assignedOven == 1 ? POS_OVEN1 : POS_OVEN2);
                    }
                    freeOvenSlotAndAdvanceQueue(sprite);
                    sprite.addTarget(POS_PICKUP);
                    break;

                case "DRINK":
                    sprite.wantsDrink = true;
                    if (currentSeatedCount < tableCapacity) {
                        currentSeatedCount++;
                        routeCustomerToSeated(sprite);
                    } else if (!tableWaitingQueue.contains(sprite)) {
                        tableWaitingQueue.add(sprite);
                    }
                    break;

                case "TABLE":
                    if (!sprite.isSeated && !sprite.wantsDrink) {
                        if (currentSeatedCount < tableCapacity) {
                            currentSeatedCount++;
                            routeCustomerToSeated(sprite);
                        } else if (!tableWaitingQueue.contains(sprite)) {
                            tableWaitingQueue.add(sprite); // Wait at POS_PICKUP
                        }
                    }
                    break;

                case "FINISHED":
                    sprite.isLeaving = true;

                    // Free table spot & advance table waiting queue from POS_PICKUP
                    if (sprite.isSeated) {
                        sprite.isSeated = false;
                        currentSeatedCount--;

                        CustomerSprite nextWaitingTable = tableWaitingQueue.poll();
                        if (nextWaitingTable != null) {
                            currentSeatedCount++;
                            routeCustomerToSeated(nextWaitingTable);
                        }
                    }

                    // Free courier spot & advance courier waiting queue
                    if (sprite.isAtCourier) {
                        sprite.isAtCourier = false;
                        courierCount--;

                        CustomerSprite nextWaitingCourier = courierWaitingQueue.poll();
                        if (nextWaitingCourier != null) {
                            courierCount++;
                            nextWaitingCourier.isAtCourier = true;
                        }
                    }

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

            if (!c.targetPoints.isEmpty()) {
                int[] target = c.targetPoints.peek();
                double dx = target[0] - c.x;
                double dy = target[1] - c.y;
                double distance = Math.sqrt(dx * dx + dy * dy);

                if (distance < 4) {
                    c.x = target[0];
                    c.y = target[1];
                    c.targetPoints.poll();
                } else {
                    c.x += (dx / distance) * currentBallSpeed;
                    c.y += (dy / distance) * currentBallSpeed;
                }
            } else {
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
                    if (!isPaused) {
                        updateCustomerPositions();
                    }
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
        Queue<int[]> targetPoints = new LinkedList<>();
        Image characterImage;
        Color color;
        boolean isLeaving = false;
        boolean wantsDrink = false;
        boolean isBaking = false;
        boolean isSeated = false;
        boolean isAtCourier = false;
        int assignedOven = 1;

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