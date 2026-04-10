-- Food Delivery Platform: create one database per microservice
CREATE DATABASE customer_db;
CREATE DATABASE restaurant_db;
CREATE DATABASE order_db;
CREATE DATABASE delivery_db;

-- Grant the default postgres user access to each
GRANT ALL PRIVILEGES ON DATABASE customer_db  TO postgres;
GRANT ALL PRIVILEGES ON DATABASE restaurant_db TO postgres;
GRANT ALL PRIVILEGES ON DATABASE order_db     TO postgres;
GRANT ALL PRIVILEGES ON DATABASE delivery_db  TO postgres;
