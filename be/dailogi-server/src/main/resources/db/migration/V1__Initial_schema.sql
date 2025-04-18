create table app_user (id bigint generated by default as identity, created_at TIMESTAMP WITH TIME ZONE not null, email varchar(255), name varchar(50) not null, is_special_user boolean not null, password_hash TEXT not null, updated_at TIMESTAMP WITH TIME ZONE not null, primary key (id));
create table character (id bigint generated by default as identity, avatar blob, created_at TIMESTAMP WITH TIME ZONE not null, description TEXT not null, is_global boolean not null, name varchar(100) not null, short_description TEXT not null, updated_at TIMESTAMP WITH TIME ZONE not null, default_llm_id bigint, user_id bigint not null, primary key (id));
create table dialogue (id bigint generated by default as identity, created_at TIMESTAMP WITH TIME ZONE not null, name varchar(255) not null, scene_description TEXT, status enum ('COMPLETED','FAILED') not null, updated_at TIMESTAMP WITH TIME ZONE not null, user_id bigint not null, primary key (id));
create table dialogue_character_config (character_id bigint not null, dialogue_id bigint not null, llm_id bigint not null, primary key (character_id, dialogue_id));
create table dialogue_message (id bigint generated by default as identity, content TEXT not null, turn_number integer not null, character_id bigint not null, dialogue_id bigint not null, primary key (id));
create table llm (id bigint generated by default as identity, name varchar(100) not null, openrouter_identifier varchar(100) not null, primary key (id));
alter table if exists app_user drop constraint if exists app_user_name_unique;
alter table if exists app_user add constraint app_user_name_unique unique (name);
alter table if exists character add constraint character_user_name_unique unique (user_id, name);
alter table if exists character add constraint character_default_llm_fk foreign key (default_llm_id) references llm;
alter table if exists character add constraint character_app_user_fk foreign key (user_id) references app_user;
alter table if exists dialogue add constraint dialogue_app_user_fk foreign key (user_id) references app_user;
alter table if exists dialogue_character_config add constraint dialogue_character_config_character_fk foreign key (character_id) references character;
alter table if exists dialogue_character_config add constraint dialogue_character_config_dialogue_fk foreign key (dialogue_id) references dialogue;
alter table if exists dialogue_character_config add constraint dialogue_character_config_llm_fk foreign key (llm_id) references llm;
alter table if exists dialogue_message add constraint dialogue_message_character_fk foreign key (character_id) references character;
alter table if exists dialogue_message add constraint dialogue_message_dialogue_fk foreign key (dialogue_id) references dialogue; 