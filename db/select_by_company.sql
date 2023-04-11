create table company(
	id integer not null,
	name character varying,
	CONSTRAINT company_pkey primary key (id)
);

create table person (
	id integer not null,
	name character varying,
	company_id integer references company(id),
	CONSTRAINT person_pkey primary key (id)
);

insert into company (id, name) values (1, 'Tor');
insert into company (id, name) values (2, 'Arcada');
insert into company (id, name) values (3, 'Sakura');
insert into company (id, name) values (4, 'Mishlen');
insert into company (id, name) values (5, 'Saido');

insert into person (id, name, company_id) values (1, 'Ivan', 1);
insert into person (id, name, company_id) values (2, 'Oleg', 1);
insert into person (id, name, company_id) values (3, 'Denis', 2);
insert into person (id, name, company_id) values (4, 'Kirill', 3);
insert into person (id, name, company_id) values (5, 'Petr', 3);
insert into person (id, name, company_id) values (6, 'Olga', 4);
insert into person (id, name, company_id) values (7, 'Maria', 5);

select p.name, c.name from person as p
join company as c on c.id = p.company_id
where c.id != 5;

create view count_person_in_company
as select c.name, count(p.name)
as count_person
from company as c
join person as p on c.id = p.company_id
group by c.name;

select name, count_person
from count_person_in_company
where count_person = (select max(count_person) 
from count_person_in_company);
