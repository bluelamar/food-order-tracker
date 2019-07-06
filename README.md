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
```
  $ cp target/food-order-tracker.war <path to>/apache-tomee-plus-8.0.0-M3/webapps/fot.war
```

To build the client, ensure you have the *prerequisite* of *go* installed.
ie. https://golang.org/doc/install
Then perform this command from the root of the directory:
```
  $ cd client
  $ go build .
```

Run the client order driver:
```
  $ cd client
  $ ./client
```

## Description of How and Why to move orders from a shelf to the Over-flow shelf

