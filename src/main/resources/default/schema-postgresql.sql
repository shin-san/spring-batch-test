CREATE TABLE record (
	id serial PRIMARY KEY,
	date VARCHAR ( 255 ),
	impression VARCHAR ( 50 ) NOT NULL,
	clicks VARCHAR ( 255 ) UNIQUE NOT NULL,
	earning VARCHAR ( 255 ) UNIQUE NOT NULL
);

CREATE TABLE record1 (
	id serial PRIMARY KEY,
	date VARCHAR ( 255 ),
	impression VARCHAR ( 50 ) NOT NULL,
	clicks VARCHAR ( 255 ) UNIQUE NOT NULL,
	earning VARCHAR ( 255 ) UNIQUE NOT NULL
);