create table public.republics
(
    uuid         uuid                     default gen_random_uuid() not null
        primary key,
    name         varchar(255)                                       not null,
    code         varchar(8)                                         not null
        unique,
    street       varchar(255)                                       not null,
    number       varchar(20)                                        not null,
    complement   varchar(255),
    neighborhood varchar(255)                                       not null,
    city         varchar(255)                                       not null,
    state        varchar(2)                                         not null,
    zip_code     varchar(10)                                        not null,
    created_at   timestamp with time zone default CURRENT_TIMESTAMP,
    updated_at   timestamp with time zone default CURRENT_TIMESTAMP,
    owner_id     uuid                                               not null
);

alter table public.republics
    owner to postgres;

create table public.budget_plans
(
    id             serial
        primary key,
    republic_id    uuid           not null
        references public.republics
            on delete cascade,
    year           integer        not null,
    month          integer        not null,
    category       varchar(100)   not null,
    planned_amount numeric(10, 2) not null,
    created_at     timestamp with time zone default CURRENT_TIMESTAMP,
    constraint uk_budget_plan_month
        unique (republic_id, year, month, category)
);

alter table public.budget_plans
    owner to postgres;

grant select, update, usage on sequence public.budget_plans_id_seq to anon;

grant select, update, usage on sequence public.budget_plans_id_seq to authenticated;

grant select, update, usage on sequence public.budget_plans_id_seq to service_role;

create index idx_budget_plans_republic_id
    on public.budget_plans (republic_id);

grant delete, insert, references, select, trigger, truncate, update on public.budget_plans to anon;

grant delete, insert, references, select, trigger, truncate, update on public.budget_plans to authenticated;

grant delete, insert, references, select, trigger, truncate, update on public.budget_plans to service_role;

create table public.events
(
    id          serial
        primary key,
    republic_id uuid                     not null
        constraint fk_event_republic
            references public.republics
            on delete cascade,
    title       varchar(255)             not null,
    description text,
    start_date  timestamp with time zone not null,
    end_date    timestamp with time zone not null,
    location    varchar(255),
    created_at  timestamp with time zone default CURRENT_TIMESTAMP,
    created_by  uuid
);

alter table public.events
    owner to postgres;

grant select, update, usage on sequence public.events_id_seq to anon;

grant select, update, usage on sequence public.events_id_seq to authenticated;

grant select, update, usage on sequence public.events_id_seq to service_role;

create index idx_event_republic_id
    on public.events (republic_id);

grant delete, insert, references, select, trigger, truncate, update on public.events to anon;

grant delete, insert, references, select, trigger, truncate, update on public.events to authenticated;

grant delete, insert, references, select, trigger, truncate, update on public.events to service_role;

create table public.inventory_items
(
    id          serial
        primary key,
    republic_id uuid         not null
        constraint fk_inventory_items_republic
            references public.republics
            on delete cascade,
    item_name   varchar(255) not null,
    quantity    integer      not null,
    condition   item_condition_type,
    created_at  timestamp with time zone default CURRENT_TIMESTAMP
);

alter table public.inventory_items
    owner to postgres;

grant select, update, usage on sequence public.inventory_items_id_seq to anon;

grant select, update, usage on sequence public.inventory_items_id_seq to authenticated;

grant select, update, usage on sequence public.inventory_items_id_seq to service_role;

create index idx_inventory_items_republic_id
    on public.inventory_items (republic_id);

grant delete, insert, references, select, trigger, truncate, update on public.inventory_items to anon;

grant delete, insert, references, select, trigger, truncate, update on public.inventory_items to authenticated;

grant delete, insert, references, select, trigger, truncate, update on public.inventory_items to service_role;

create table public.republic_finances
(
    id              serial
        primary key,
    republic_id     uuid                               not null
        constraint uk_republic_finances_republic_id
            unique
        references public.republics
            on delete cascade,
    current_balance numeric(12, 2)           default 0 not null,
    last_updated    timestamp with time zone default CURRENT_TIMESTAMP
);

alter table public.republic_finances
    owner to postgres;

grant select, update, usage on sequence public.republic_finances_id_seq to anon;

grant select, update, usage on sequence public.republic_finances_id_seq to authenticated;

grant select, update, usage on sequence public.republic_finances_id_seq to service_role;

create index idx_republic_finances_republic_id
    on public.republic_finances (republic_id);

grant delete, insert, references, select, trigger, truncate, update on public.republic_finances to anon;

grant delete, insert, references, select, trigger, truncate, update on public.republic_finances to authenticated;

grant delete, insert, references, select, trigger, truncate, update on public.republic_finances to service_role;

create index idx_republic_code
    on public.republics (code);

create index idx_republic_owner_id
    on public.republics (owner_id);

grant delete, insert, references, select, trigger, truncate, update on public.republics to anon;

grant delete, insert, references, select, trigger, truncate, update on public.republics to authenticated;

grant delete, insert, references, select, trigger, truncate, update on public.republics to service_role;

create table public.tasks
(
    id                  serial
        primary key,
    republic_id         uuid         not null
        constraint fk_task_republic
            references public.republics
            on delete cascade,
    title               varchar(255) not null,
    description         text,
    due_date            timestamp with time zone,
    category            varchar(50),
    status              varchar(50)              default 'pending'::character varying,
    created_at          timestamp with time zone default CURRENT_TIMESTAMP,
    updated_at          timestamp with time zone default CURRENT_TIMESTAMP,
    completed_at        timestamp,
    recurring        boolean                  default false,
    recurrence_type     varchar(10),
    recurrence_interval integer,
    recurrence_end_date timestamp,
    parent_task_id      bigint
        constraint fk_parent_task
            references public.tasks
            on delete set null
);

alter table public.tasks
    owner to postgres;

grant select, update, usage on sequence public.tasks_id_seq to anon;

grant select, update, usage on sequence public.tasks_id_seq to authenticated;

grant select, update, usage on sequence public.tasks_id_seq to service_role;

create index idx_task_republic_id
    on public.tasks (republic_id);

grant delete, insert, references, select, trigger, truncate, update on public.tasks to anon;

grant delete, insert, references, select, trigger, truncate, update on public.tasks to authenticated;

grant delete, insert, references, select, trigger, truncate, update on public.tasks to service_role;

create table public.users
(
    uuid                uuid                     default gen_random_uuid() not null
        constraint user_pkey
            primary key,
    name                varchar(255)                                       not null,
    email               varchar(255)                                       not null
        constraint user_email_key
            unique,
    phone_number        varchar(20),
    profile_picture_url varchar(255),
    current_republic_id uuid
        constraint fk_user_republic
            references public.republics
            on delete set null,
    status              varchar(255)             default 'active'::user_status,
    created_at          timestamp with time zone default CURRENT_TIMESTAMP,
    last_login          timestamp with time zone,
    provider            varchar(255)                                       not null,
    firebase_uid        varchar(255)                                       not null
        constraint user_firebase_uid_key
            unique,
    is_admin            boolean                  default false,
    entry_date          timestamp with time zone,
    departure_date      timestamp with time zone,
    nickname            varchar(255)
);

alter table public.users
    owner to postgres;

create table public.activity_log
(
    id          serial
        primary key,
    user_id     uuid
        constraint fk_activity_log_user
            references public.users
            on delete set null,
    republic_id uuid
        constraint fk_activity_log_republic
            references public.republics
            on delete set null,
    action      varchar(255) not null,
    details     text,
    created_at  timestamp with time zone default CURRENT_TIMESTAMP
);

alter table public.activity_log
    owner to postgres;

grant select, update, usage on sequence public.activity_log_id_seq to anon;

grant select, update, usage on sequence public.activity_log_id_seq to authenticated;

grant select, update, usage on sequence public.activity_log_id_seq to service_role;

create index idx_activity_log_user_id
    on public.activity_log (user_id);

grant delete, insert, references, select, trigger, truncate, update on public.activity_log to anon;

grant delete, insert, references, select, trigger, truncate, update on public.activity_log to authenticated;

grant delete, insert, references, select, trigger, truncate, update on public.activity_log to service_role;

create table public.event_invitations
(
    user_id  uuid                                              not null
        constraint fk_event_invitations_user
            references public.users
            on delete cascade,
    event_id integer                                           not null
        constraint fk_event_invitations_event
            references public.events
            on delete cascade,
    status   varchar(255) default 'INVITED'::character varying not null,
    primary key (user_id, event_id)
);

alter table public.event_invitations
    owner to postgres;

grant delete, insert, references, select, trigger, truncate, update on public.event_invitations to anon;

grant delete, insert, references, select, trigger, truncate, update on public.event_invitations to authenticated;

grant delete, insert, references, select, trigger, truncate, update on public.event_invitations to service_role;

create table public.expenses
(
    id                 serial
        primary key,
    republic_id        uuid           not null
        constraint fk_expenses_republic
            references public.republics
            on delete cascade,
    description        text           not null,
    amount             numeric(10, 2) not null,
    expense_date       date           not null,
    category           varchar(100),
    receipt_url        varchar(255),
    created_at         timestamp with time zone default CURRENT_TIMESTAMP,
    creator_id         uuid
        references public.users
            on delete cascade,
    status             varchar(255)             default 'PENDING'::character varying(255),
    approval_date      timestamp with time zone,
    reimbursement_date timestamp with time zone,
    rejection_reason   text
);

alter table public.expenses
    owner to postgres;

grant select, update, usage on sequence public.expenses_id_seq to anon;

grant select, update, usage on sequence public.expenses_id_seq to authenticated;

grant select, update, usage on sequence public.expenses_id_seq to service_role;

create table public.expense_splits
(
    expense_id integer        not null
        constraint fk_expense_splits_expense
            references public.expenses
            on delete cascade,
    user_id    uuid           not null
        constraint fk_expense_splits_user
            references public.users
            on delete cascade,
    amount     numeric(10, 2) not null,
    paid       boolean default false,
    paid_at    timestamp with time zone,
    primary key (expense_id, user_id)
);

alter table public.expense_splits
    owner to postgres;

create index idx_expense_splits_expense_id
    on public.expense_splits (expense_id);

create index idx_expense_splits_user_id
    on public.expense_splits (user_id);

grant delete, insert, references, select, trigger, truncate, update on public.expense_splits to anon;

grant delete, insert, references, select, trigger, truncate, update on public.expense_splits to authenticated;

grant delete, insert, references, select, trigger, truncate, update on public.expense_splits to service_role;

create index idx_expenses_creator_id
    on public.expenses (creator_id);

create index idx_expenses_republic_id
    on public.expenses (republic_id);

create index idx_expenses_status
    on public.expenses (status);

grant delete, insert, references, select, trigger, truncate, update on public.expenses to anon;

grant delete, insert, references, select, trigger, truncate, update on public.expenses to authenticated;

grant delete, insert, references, select, trigger, truncate, update on public.expenses to service_role;

create table public.incomes
(
    id             serial
        primary key,
    republic_id    uuid                     not null
        references public.republics
            on delete cascade,
    contributor_id uuid
                                            references public.users
                                                on delete set null,
    description    text                     not null,
    amount         numeric(10, 2)           not null,
    income_date    timestamp with time zone not null,
    source         varchar(100)             not null,
    created_at     timestamp with time zone default CURRENT_TIMESTAMP
);

alter table public.incomes
    owner to postgres;

grant select, update, usage on sequence public.incomes_id_seq to anon;

grant select, update, usage on sequence public.incomes_id_seq to authenticated;

grant select, update, usage on sequence public.incomes_id_seq to service_role;

create index idx_incomes_contributor_id
    on public.incomes (contributor_id);

create index idx_incomes_republic_id
    on public.incomes (republic_id);

grant delete, insert, references, select, trigger, truncate, update on public.incomes to anon;

grant delete, insert, references, select, trigger, truncate, update on public.incomes to authenticated;

grant delete, insert, references, select, trigger, truncate, update on public.incomes to service_role;

create table public.polls
(
    id          serial
        primary key,
    republic_id uuid                     not null
        constraint fk_polls_republic
            references public.republics
            on delete cascade,
    question    text                     not null,
    start_date  timestamp with time zone not null,
    end_date    timestamp with time zone not null,
    created_by  uuid                     not null
        constraint fk_polls_user
            references public.users
            on delete cascade
        references public.users
            on delete cascade
);

alter table public.polls
    owner to postgres;

grant select, update, usage on sequence public.polls_id_seq to anon;

grant select, update, usage on sequence public.polls_id_seq to authenticated;

grant select, update, usage on sequence public.polls_id_seq to service_role;

create table public.poll_options
(
    id          serial
        primary key,
    poll_id     integer      not null
        constraint fk_poll_options_poll
            references public.polls
            on delete cascade,
    option_text varchar(255) not null,
    vote_count  integer default 0
);

alter table public.poll_options
    owner to postgres;

grant select, update, usage on sequence public.poll_options_id_seq to anon;

grant select, update, usage on sequence public.poll_options_id_seq to authenticated;

grant select, update, usage on sequence public.poll_options_id_seq to service_role;

create index idx_poll_options_poll_id
    on public.poll_options (poll_id);

grant delete, insert, references, select, trigger, truncate, update on public.poll_options to anon;

grant delete, insert, references, select, trigger, truncate, update on public.poll_options to authenticated;

grant delete, insert, references, select, trigger, truncate, update on public.poll_options to service_role;

create table public.poll_votes
(
    user_id        uuid    not null
        constraint fk_poll_votes_user
            references public.users
            on delete cascade,
    poll_option_id integer not null
        constraint fk_poll_votes_poll_option
            references public.poll_options
            on delete cascade,
    primary key (user_id, poll_option_id)
);

alter table public.poll_votes
    owner to postgres;

create index idx_poll_votes_poll_option_id
    on public.poll_votes (poll_option_id);

create index idx_poll_votes_user_id
    on public.poll_votes (user_id);

grant delete, insert, references, select, trigger, truncate, update on public.poll_votes to anon;

grant delete, insert, references, select, trigger, truncate, update on public.poll_votes to authenticated;

grant delete, insert, references, select, trigger, truncate, update on public.poll_votes to service_role;

create index idx_polls_created_by
    on public.polls (created_by);

create index idx_polls_republic_id
    on public.polls (republic_id);

grant delete, insert, references, select, trigger, truncate, update on public.polls to anon;

grant delete, insert, references, select, trigger, truncate, update on public.polls to authenticated;

grant delete, insert, references, select, trigger, truncate, update on public.polls to service_role;

alter table public.republics
    add constraint fk_republic_owner
        foreign key (owner_id) references public.users
            on delete restrict;

create table public.user_tasks
(
    user_id uuid    not null
        constraint fk_user_tasks_user
            references public.users
            on delete cascade,
    task_id integer not null
        constraint fk_user_tasks_task
            references public.tasks
            on delete cascade,
    primary key (user_id, task_id)
);

alter table public.user_tasks
    owner to postgres;

grant delete, insert, references, select, trigger, truncate, update on public.user_tasks to anon;

grant delete, insert, references, select, trigger, truncate, update on public.user_tasks to authenticated;

grant delete, insert, references, select, trigger, truncate, update on public.user_tasks to service_role;

create index idx_user_current_republic_id
    on public.users (current_republic_id);

create index idx_user_email
    on public.users (email);

create index idx_user_firebase_uid
    on public.users (firebase_uid);

grant delete, insert, references, select, trigger, truncate, update on public.users to anon;

grant delete, insert, references, select, trigger, truncate, update on public.users to authenticated;

grant delete, insert, references, select, trigger, truncate, update on public.users to service_role;

