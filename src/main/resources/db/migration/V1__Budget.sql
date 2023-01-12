create table budget
(
    id     serial primary key,
    year   int  not null,
    month  int  not null,
    amount int  not null,
    type   text not null
);

ALTER TABLE budget
(
    ADD COLUMN author_id INTEGER
);


ALTER TABLE budget
(
    ADD CONSTRAINT fk_author
    FOREIGN KEY (author_id)
    REFERENCES Author(ID)
);
