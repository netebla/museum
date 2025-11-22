alter table users
    add column if not exists full_name varchar(100);

alter table tickets
    add column if not exists user_id bigint;

alter table tickets
    add constraint fk_tickets_user
        foreign key (user_id) references users(id) on delete set null;

