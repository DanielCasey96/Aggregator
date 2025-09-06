This is an API to mimic the processing of a finance company. The initial goal was to create a system that would allow for aggregation of net worth from various accounts, however when looking into that, it became clear that would require not only too much time and technical investment, but also monetary investment and contracting.

Hence the target has moved to essentially creating a finance system where that target is still possible but via personally created mini systems/ APIs/ stubs etc.

As this will only be added to by me you will notice the architecture is significantly slimmed down. But there is potential to expand it in the future as I learn new aspects.

## Plan
### Part 1 - We are here
Create a basic API without the use of Spring Boot - Gain an appreciation of what Spring gives for free.
### Part 2
Create a DB for the API to connect to - Learn about DBs and implementation as well as having an integration for the service.
### Part 3 
Add Spring Boot to the service - Learn about Spring etc
### Part 4
Update the DB for new Account types - Learn how to migrate a DB or update the schema etc.
### Part 5
Update to consume the multiple accounts as a single account for the aggregator.
### Part 6
Create a front end to visualize the data and provide an extra integration - Learn frontend, potentially host the services for consumption.
### Part 7
Mimic calling to other services with authorization + generally add auth


## Notes/ Learnings
### Base API
In creating a service without Spring the main thing that needed to be done was spinning up the server and handing the connections. The general structure this has been completed with is a controller to handle the incomming request, this starts the service on local 8080.
It then initalises the Handler class where by also utilising a service class we check incoming body, headers etc, and create the response, with an integrated call to a DB or service in between.

The formatting and checking of the response and request are abstracted out to Models. And finally we have unit tests where the only handler specific tests are actually the parsing aspects of the body. The content of the request body etc is handled by the service as business logic and hence live there.

### BD Choice
We have two options, SQL and noSQL. Although DynamoBD is a heavily used noSQL type for spaces in the AWS ecosystem, for the use of payments data, SQL is probably a better choice, mainly due to its relational capabilities.

PostgreSQL is the front running choice over a basic SQL or SQLite as this is intended to mimic a company, however for a personal project I am aware SQLite is probably better.
The Concurrent requests of PostgreSQL may also be a strong benefit down the line.
#### Connect to the DB on command line
psql -U postgres -d customer
#### Insert a new user
INSERT INTO accounts (id, balance, debt, currency, lastUpdated)
VALUES (1, 1000.00, 0.00, 'GBP', NOW());
#### Check the data of a user
SELECT * FROM accounts WHERE id = 1;
#### End DB process
net stop postgresql-x64-15
### Start DB process
pg_ctl start -D "C:\Program Files\PostgreSQL\17\data"

C4p1t41nM0d3rn1s3