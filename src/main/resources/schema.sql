DROP TABLE IF EXISTS employee;

CREATE TABLE employee (
  id BIGINT NOT NULL,
  first_name VARCHAR(255) NOT NULL,
  last_name VARCHAR(255) NOT NULL,
  CONSTRAINT pk_employee PRIMARY KEY (id)
);
