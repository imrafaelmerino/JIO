CREATE TABLE customer (
                          id SERIAL PRIMARY KEY,
                          name VARCHAR(255) NOT NULL
);


CREATE TABLE address (
                         id SERIAL PRIMARY KEY,
                         street VARCHAR(255) NOT NULL,
                         customer_id INT REFERENCES customer(id) ON DELETE CASCADE
);


CREATE TABLE email (
                       id SERIAL PRIMARY KEY,
                       email_address VARCHAR(255) NOT NULL,
                       customer_id INT REFERENCES customer(id) ON DELETE CASCADE
);