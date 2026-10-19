# Pizzeria Simulator (3-phase discrete event simulation)

This is a Maven-based Java application that simulates the daily operations of a pizzeria. It models how dine-in, takeaway and online (delivery) customers move through ordering, payment, baking, pickup, drinks, dining tables and couriers, using the 3-phase method of discrete event simulation. Customer arrivals and service times are generated with the <a href="https://users.metropolia.fi/~kirillsa/G6_Project_Javadoc/eduni/distributions/package-summary.html">distribution package from Metropolia University</a>.

The simulator shows the restaurant as a live animation and reports how busy each service point was, how long queues grew and how long customers stayed. This helps to find bottlenecks and to decide how many ovens, tables and couriers a pizzeria needs. Finished runs, per-customer logs and favorite settings are stored in a MariaDB database.


## Features

- Three customer types with their own routes: dine-in, takeaway and online
- Two ovens with configurable capacity, plus configurable tables and couriers
- Adjustable arrival pace, customer mix and drink probability
- Animated canvas showing customers moving through the pizzeria
- Results report for every service point: total serviced, utilization, average and maximum queue length
- Run history window and saved preset configurations stored in MariaDB
- Pause, resume, stop and speed controls during a run
- Reproducible customer stream through fixed random seeds

## Technical stack

- **Java 17**: the main programming language
- **Maven**: dependency management and build tool
- **JavaFX 21**: the graphical user interface and the animated canvas
- **MariaDB**: relational database for run history, customer logs and presets
- **MariaDB JDBC driver**: connects the application to the database
- **eduni.distributions**: statistical distributions for arrival and service times
- **MySQL**: database programming language used to interact with MariaDB

## Installation

1. Clone the project repository from GitHub and open the project folder:
```
git clone https://github.com/<your-account>/<your-repository>.git
cd <your-repository>
```

2. Build the project with Maven (or open the folder in IntelliJ IDEA and let it load the Maven project):
```
mvn clean install
```

3. Set up MariaDB:
   - Install MariaDB Server from https://mariadb.org/download/. HeidiSQL is a handy tool for managing the database.
   - Create the database user the application expects:
     ```sql
     CREATE USER 'appuser'@'localhost' IDENTIFIED BY 'password';
     ```
   - Run the script `src/queries.sql` (or `src/main/java/queries.sql`). It creates the database `pizzeria_db` with the tables `simulation_runs`, `preset_configs` and `customer_logs`.
   - Give the user access to the database:
     ```sql
     GRANT ALL PRIVILEGES ON pizzeria_db.* TO 'appuser'@'localhost';
     FLUSH PRIVILEGES;
     ```

The application connects to `jdbc:mariadb://localhost:3306/pizzeria_db` with the user `appuser` and the password `password`. These values are set in `src/main/java/org/example/datasource/MariaDbConnection.java`.

## Execution

In your IDE (e.g., IntelliJ IDEA), right-click the `Launcher` class (`src/Launcher.java`) or `Main` (`src/main/java/org/example/Main.java`) and choose Run. Start the `Launcher` or `Main` class rather than `SimulatorGUI`, so that JavaFX starts correctly.

## Using the simulator

1. Set the inputs in the left sidebar: station capacities, arrival pace, customer mix, drink chance, simulation time and delay.
2. Press **Start Simulation** and watch the pizzeria on the canvas. Use **Pause**, **Slow Down**, **Speed Up** or **Stop Simulation** during the run.
3. When the run ends, read the results report in the right sidebar.
4. Press **View Run History** to compare earlier runs, and use **Save** next to the preset list to store your favorite settings.

### Configurable simulation settings

| Setting | Range | Default |
| --- | --- | --- |
| Oven 1 capacity | 2 to 12 | 4 |
| Oven 2 capacity | 2 to 12 | 4 |
| Tables capacity | 5 to 15 | 10 |
| Couriers capacity | 1 to 5 | 3 |
| Arrival pace | Fast (2.5), Normal (5.0), Slow (10) | Normal |
| Dine-in drink chance | 0, 50 or 100 % | 50 % |
| Customer mix (dine-in / takeaway / online) | relative weights, 0 to 100 | 40 / 40 / 20 |
| Simulation time | any positive number | 100 |
| Delay (ms) | 100 or more | 1000 |

## Simulation metrics

For every service point (Order Register, Payment Counter, Dining Tables, Drink Kiosk, Oven 1, Oven 2, Pickup Counter and Couriers) the results report shows:

- **Total serviced**: customers served by the station
- **Utilization rate**: share of the station's capacity that was busy during the run
- **Average queue length**: average number of customers waiting
- **Maximum queue length**: the longest waiting line during the run

The run history also stores the total duration, the number of customers who left, their average stay time and the number of customers of each type.


## Project structure

```
src/main/java/org/example
├── Main.java            Entry point
├── view/                JavaFX user interface and canvas animation
├── controller/          Controller and the interfaces between view, controller and model
├── simu/framework/      Simulation framework: Engine, Clock, Event, EventList, ArrivalProcess
├── simu/model/          Pizzeria model: MyEngine, ServicePoint, Customer, event and customer types
├── model/               Data classes: SimulationRun, CustomerLog, PresetConfig
├── dao/                 Database access classes
├── datasource/          MariaDB connection
└── eduni/distributions/ Statistical distributions (University of Edinburgh)
```

## Authors

- [Illia V](https://github.com/jxckzy)
- [Daniel M](https://github.com/Danielnm1)
- [Niklas W](https://github.com/niqutinho)
- [Anna Z](https://github.com/zhuchokz)
