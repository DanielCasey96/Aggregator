
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
pg_ctl start -D "C:\Users\danca\Development\postGres\17.2\Data"

Success. You can now start the database server using:

    ^"C^:^\Program^ Files^\PostgreSQL^\17^\bin^\pg^_ctl^" -D ^"C^:^\Users^\danca^\Development^\postGres^\17^.2^\Data^" -l logfile start

    Start your new PostgreSQL instance on a different port (e.g., 5433):
pg_ctl start -D "C:\Users\danca\Development\postGres\17.2\Data" -o "-p 5433"

Connect to it:
psql -U postgres -p 5433