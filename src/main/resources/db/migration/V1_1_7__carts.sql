-- Table: hook.cart_position
CREATE TABLE hook.cart_position
(
    id bigserial not null,
    message_id bigint not null,
    product character varying  not null,
    price bigint not null,
    quantity int not null,
    cost bigint not null,
    rate character varying,
    CONSTRAINT pk_cart_position PRIMARY KEY (id),
    CONSTRAINT fk_cart_to_message FOREIGN KEY (message_id) REFERENCES hook.message(id)
);
