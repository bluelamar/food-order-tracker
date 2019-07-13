# food-order-tracker
Track incoming and outgoing food orders

This is a study in how to manage food orders where orders are classified
as one of Hot, Cold, or Frozen. The order is placed on the appropriate
shelf according to that temperature classification.

Food must be delivered within a certain time period and will be evaluated
periodically with a rate of decay. Once the decay value is <= 0,
quality control requires the food be thrown away.

A Poisson rate is used by the client to send orders to the server.
It uses a lambda of 3.25 per second.

The server receives an order and immediately dispatches a driver to pickup
the order. Drivers arrive between 2 and 10 seconds to pickup an order.

## How to run and test the service

To build the server, ensure you have the *prerequisite* of *Maven* installed.
ie. https://maven.apache.org/install.html
Then perform this command from the root of the directory:
```
  $ mvn clean package install
```

Copy the *war* file to the *webapps* directory of your ee web service, ex: tomee-plus8.0.0
I downloaded TomEE plus 8.0.0-M3 from https://tomee.apache.org/download-ng.html
```
  $ cp target/food-order-tracker.war <path to>/apache-tomee-plus-8.0.0-M3/webapps/fot.war
```

To build the client order driver, ensure you have the *prerequisite* of *go* installed.
ie. https://golang.org/doc/install
Build the client order driver:
```
  $ cd client
  $ go build .
```

Run the client order driver: Note that it picks up the local orderdata.json
in that directory:
```
  $ cd client
  $ ./client
```

## How and Why to move orders from a shelf to the Over-flow shelf

When the service receives the order it attempts to place it on the appropriate
temperature shelf. If that shelf is full, it will attempt to place it on
the over-flow shelf.

If it fails to place the order on either shelf, it will return an
Unavailable(503) error to the calling client when the shelves are found
to be full.
Other errors will be treated as Internal(500) errors.

A background thread is spawned when the service starts up. This task is scheduled
to run once a second. It performs several jobs:

1. Process the drivers queue.
The drivers queue is a min-heap whereby the orders are ordered by the arrival
time of their scheduled driver.
As a min-heap this means the orders with a short delivery time are closer to
the root, and those with longer delivery time are farther from the root.
Orders are processed base on the current time and the driver pickup time.
Once the pickup time is in the future, processing stops.

2. Process the decay of the over-flow shelf.
This will run the decay algorithm for the over-flow orders and dispose of them
accordingly.
In the process, those orders that are still good are placed into temperature
heaps(hot,cold,frozen) in case they can be moved from the over-flow shelf to the appropriate
temperature shelf. They are sorted by decay value - those that will decay
sooner are closer to the top, and those that can last longer will be farther
from the root.
The idea being that since they decay faster on the over-flow shelf their life
may be extended once placed back on their temperature shelf.

3. Process the decay of the temperature shelves.
This will run the decay algorithm for each of the orders of each temperature
shelf and dispose of them accordingly.
Once that processing has been done for a termperature shelf, if there is space
and there are over-flow orders available from step 2 above, then those over-flow
orders will be moved to the temperature shelf until either it is full or
there are no more over-flow orders of that temperature.

