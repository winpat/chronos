create type status as enum ('todo', 'done');
create table todos(
  id bigserial primary key,
  title text not null,
  description text,
  status status,
  created_at timestamp with time zone not null default now(),
  completed_at timestamp with time zone,
  archived_at timestamp with time zone
);
