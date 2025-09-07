
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
pg_ctl start -D "C:\Users\danca\Development\postGres\17.2\Data" -o "-p 5433"

### Connect to it:
psql -U postgres -p 5433

### Get products
Invoke-WebRequest -Uri "http://localhost:8080/accounts" -Headers @{"Content-Type"="application/json"}
### Update Value of a Product
Invoke-WebRequest -Uri "http://localhost:8080/update-value" `
>>     -Method POST `                                                                                                                                                                                                                                                                                                                                                                                                             
>>     -Headers @{"Content-Type"="application/json"} `                                                                                                                                                                                                                                                                                                                                                                            
>>     -Body '{"value":12345.67}'   
### Add new Product
Invoke-WebRequest -Uri "http://localhost:8080/add-product" `
>>     -Method POST `                                                                                                                                                                                                                                                                                                                                                                                                             
>>     -Headers @{"Content-Type"="application/json"} `                                                                                                                                                                                                                                                                                                                                                                            
>>     -Body '{                                                                                                                                                                                                                                                                                                                                                                                                                   
>>         "name": "Example Name",                                                                                                                                                                                                                                                                                                                                                                                                
>>         "type": "Example Type",                                                                                                                                                                                                                                                                                                                                                                                                
>>         "provider": "Example Provider",                                                                                                                                                                                                                                                                                                                                                                                        
>>         "category": null,                                                                                                                                                                                                                                                                                                                                                                                                      
>>         "value": 12345.67,                                                                                                                                                                                                                                                                                                                                                                                                     
>>         "updatedAt": "2024-06-10T12:34:56.000Z"                                                                                                                                                                                                                                                                                                                                                                                
>>     }'       
