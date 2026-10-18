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


/**
 * Custom JavaFX {@link Canvas} component rendering dynamic 2D animations of the pizzeria simulation.
 * <p>
 * Implements {@link IVisualisation} to visually move customer sprites along station path waypoints,
 * manages sprite queuing logic for capacity-constrained service points (ovens, dining tables, couriers),
 * handles image assets loading, and runs an asynchronous JavaFX UI animation thread.
 * </p>
 *
 * @author Anja, Niklas, Illia
 * @version 3.0
 */
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

    /**
     * Constructs a new simulation canvas with specified dimensions, loads sprite resources, and starts the animation loop.
     *
     * @param w Desired canvas width in pixels.
     * @param h Desired canvas height in pixels.
     */
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

    /**
     * Safely loads an image asset from the system resources directory.
     *
     * @param fileName File name of the resource image to load.
     * @return The loaded {@link Image} object, or {@code null} if loading fails.
     */
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

    /**
     * Updates parallel workstation capacity thresholds for visual queue and seating management.
     *
     * @param o1Cap    Capacity limit for Oven 1.
     * @param o2Cap    Capacity limit for Oven 2.
     * @param tableCap Seating capacity limit for dining tables.
     * @param courierCap Active capacity limit for delivery couriers.
     */
    public void setStationCapacities(int o1Cap, int o2Cap, int tableCap, int courierCap) {
        this.oven1Capacity = o1Cap;
        this.oven2Capacity = o2Cap;
        this.tableCapacity = tableCap;
        this.courierCapacity = courierCap;
    }

    /**
     * Starts or resumes visual animation loop processing.
     */
    @Override
    public void startVisuals() {
        this.isRunning = true;
        this.isPaused = false;
    }

    /**
     * Toggles animation playback pause state.
     *
     * @param paused {@code true} to pause sprite movements; {@code false} to resume.
     */
    @Override
    public void setPaused(boolean paused) {
        this.isPaused = paused;
    }

    /**
     * Halts animation processing and clears all active customer sprites and queues from the canvas.
     */
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

    /**
     * Scales sprite movement speed using the specified multiplier ratio.
     *
     * @param speedMultiplier Speed scaling multiplier.
     */
    @Override
    public void adjustAnimationSpeed(double speedMultiplier) {
        Platform.runLater(() -> {
            this.currentBallSpeed = 3.5 * speedMultiplier;
        });
    }

    /**
     * Clears canvas graphics context and redraws background floor plan or fallback background color.
     */
    @Override
    public void clearDisplay() {
        if (backgroundImage != null) {
            gc.drawImage(backgroundImage, 0, 0, this.getWidth(), this.getHeight());
        } else {
            gc.setFill(Color.LIGHTGRAY);
            gc.fillRect(0, 0, this.getWidth(), this.getHeight());
        }
    }

    /**
     * Releases an oven baking slot upon completion and advances the next waiting sprite in line.
     *
     * @param sprite The customer sprite finishing oven baking.
     */
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

    /**
     * Routes a dine-in customer sprite to table seating and optional drink fetching path waypoints.
     *
     * @param sprite The customer sprite transitioning to dine-in seating.
     */
    private void routeCustomerToSeated(CustomerSprite sprite) {
        sprite.isSeated = true;
        sprite.addTarget(POS_DINEIN_DESCEND);
        sprite.addTarget(POS_TABLE);
        if (sprite.wantsDrink) {
            sprite.addTarget(POS_DRINK);
            sprite.addTarget(POS_TABLE);
        }
    }

    /**
     * Updates or creates customer sprite movement waypoints based on state transition events.
     *
     * @param id     Unique customer identifier.
     * @param type   Category type of customer.
     * @param action Target workstation state label string.
     */
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

    /**
     * Updates $(x, y)$ coordinate positions for all active sprites toward their next target waypoints.
     */
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

    /**
     * Launches a daemon thread executing a recurring animation loop to update and repaint sprites.
     */
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

                    double scaleX = getWidth() / 1242.0;
                    double scaleY = getHeight() / 700.0;

                    double spriteScale = Math.min(scaleX, scaleY);
                    double spriteSize = 24 * spriteScale;
                    double offset = spriteSize / 2;

                    for (CustomerSprite c : customers.values()) {
                        double renderX = c.x * scaleX;
                        double renderY = c.y * scaleY;

                        if (c.characterImage != null) {
                            gc.drawImage(c.characterImage, renderX - offset, renderY - offset, spriteSize, spriteSize);
                        } else {
                            double circleRadius = 7 * spriteScale;
                            double circleDiameter = circleRadius * 2;

                            gc.setFill(c.color);
                            gc.fillOval(renderX - circleRadius, renderY - circleRadius, circleDiameter, circleDiameter);
                            gc.setStroke(Color.BLACK);
                            gc.setLineWidth(1);
                            gc.strokeOval(renderX - circleRadius, renderY - circleRadius, circleDiameter, circleDiameter);
                        }
                    }
                });
            }
        });
        animationThread.setDaemon(true);
        animationThread.start();
    }


    /**
     * Internal container class representing an animated customer sprite on the canvas.
     */
    private static class CustomerSprite {
        /** Current X and Y-coordinate canvas pixel position. */
        double x, y;
        /** Queue of $[x, y]$ waypoint coordinate arrays defining movement paths. */
        Queue<int[]> targetPoints = new LinkedList<>();
        /** Image graphic asset associated with this customer sprite. */
        Image characterImage;
        /** Fallback color rendered when image assets are unavailable. */
        Color color;
        /** Flag indicating whether the customer sprite is currently navigating toward an exit. */
        boolean isLeaving = false;
        /** Flag indicating whether the customer sprite needs to visit the drink kiosk. */
        boolean wantsDrink = false;
        /** Flag indicating whether the customer sprite is currently undergoing oven baking. */
        boolean isBaking = false;
        /** Flag indicating whether the customer sprite is currently seated at a dining table. */
        boolean isSeated = false;
        /** Flag indicating whether the customer sprite is currently undergoing courier dispatch. */
        boolean isAtCourier = false;
        /** Numerical identifier indicating assigned oven station (1 or 2). */
        int assignedOven = 1;

        /**
         * Constructs a new customer sprite instance at the specified starting position.
         *
         * @param startPos Array containing starting $[x, y]$ canvas coordinates.
         * @param image    Image graphic asset to represent the sprite.
         * @param color    Fallback color if image rendering fails.
         */
        CustomerSprite(int[] startPos, Image image, Color color) {
            this.characterImage = image;
            this.color = color;
            this.x = startPos[0];
            this.y = startPos[1];
        }

        /**
         * Adds a destination waypoint coordinate pair to the sprite path queue.
         *
         * @param point Target $[x, y]$ coordinate array.
         */
        void addTarget(int[] point) {
            targetPoints.add(point);
        }
    }
}