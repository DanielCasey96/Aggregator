### Service Description
This service acts as an aggregator of funds to allow for the creation, deletion, and updating of various accounts a person may have.
Due to the contractual issues of connecting to multiple banks and finance institutions this acts as an offline type net worth tracking account.
The user can create an account and add each of their investments and savings to then see a total net worth value.

### Service impl
This service intentionally doesn't use Spring for improved size, speed and the learning opportunity.

### TODO
Add DH exchange for password encryption
Add decryption and hashing for the password to enter the DB
Add a recovery endpoint of username, password and email

#### Check the data of a user
SELECT * FROM accounts WHERE id = 1;
#### End DB process
net stop postgresql-x64-15
### Start DB process
pg_ctl start -D "C:\Users\danca\Development\postGres\17.2\Data" -o "-p 5433"
### Connect to it:
psql -U postgres -p 5433
