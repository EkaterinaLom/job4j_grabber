CREATE TABLE post(
	id serial primary key,
	name varchar,
	text varchar,
	link varchar unique,
	created timestamp
);
