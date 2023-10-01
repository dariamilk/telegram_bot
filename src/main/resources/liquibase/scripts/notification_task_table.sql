-- liquibase formatted sql

-- changeset dchulpanova:1
create table if not exists notification_task (
    id              serial               primary key,
    chat_id        varchar(36)           not null,
    task           varchar               not null,
    date           timestamp             not null,
    send_status    boolean
);