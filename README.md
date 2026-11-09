# Route Management System (Java OOP Project)

A Java application that manages bus/train routes using a **custom doubly linked list**,
and lets passengers book tickets with automatic fare calculation.
It has a Swing GUI and a console version that share the same classes and the same data file.

## How to run
**GUI:**
```
java -jar RouteManagementSystem.jar
```
(or double-click `run-gui.bat` on Windows)

**Console version:**
```
java -cp RouteManagementSystem.jar rms.Main
```
(or `run-console.bat`)

**Build from source** (needs JDK 17 or newer):
- Windows (PowerShell): `.\build.bat`
- Linux/Mac: `./build.sh`

On the first run click **Load Sample Data** (option 12 in the console) to get a demo route, a bus and a train.
Use **Clear All Data** in the GUI to start from scratch.

## OOP concepts (for the viva)
| Concept | Where |
|---|---|
| Inheritance | `Vehicle` -> `Bus`, `Train` |
| Abstraction | `Vehicle` (abstract class), `Bookable` (interface) |
| Polymorphism | `calculateFare()` is overridden in each child and called through a `Vehicle` reference |
| Encapsulation | All fields are `private`, accessed through getters/setters |
| Composition | `TransportSystem` holds `ArrayList<Route/Vehicle/Ticket>`; `Route` holds a chain of `RouteNode`s |
| Linked list | `RouteNode` + `Route` (add, insertAfter, remove, search, reverse, display) |
| File I/O | `FileManager` uses serialization, data stored in `data.ser` |
| Exceptions | `InvalidStationException`, `EmptyRouteException`, `VehicleFullException`, `TicketNotFoundException` + input validation |

## Fare formulas
- Bus: 50 + 4 x km
- Train: 100 + 2.5 x km

## Project structure
```
src/rms/MainGUI.java    Swing GUI
src/rms/RoutePanel.java draws the linked list as boxes and arrows
src/rms/Main.java       console version
src/rms/*.java          model classes (Route, Vehicle, Bus, Train, Ticket, ...)
uml/                    class diagram (PlantUML)
build.sh / build.bat    build scripts
```
